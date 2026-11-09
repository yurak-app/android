package com.yurak.agent.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yurak.agent.AppContainer
import com.yurak.agent.YurakApplication
import com.yurak.agent.data.ApiClient
import com.yurak.agent.data.ApiResult
import com.yurak.agent.data.EventDto
import com.yurak.agent.data.StoredCredentials
import com.yurak.agent.db.AppDatabase
import com.yurak.agent.db.PendingEventEntity
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

// POST /events rejects a batch bigger than this outright (backend's events.schemas.ts,
// .max(500)) — matches the same cap the Windows agent and the extension both chunk to.
private const val SYNC_BATCH_SIZE = 500

// Bounds how large the local queue can get during an extended outage — same backstop as
// OfflineEventStore's MaxPendingEvents on Windows.
private const val MAX_PENDING_EVENTS = 5000

private const val WORK_NAME = "yurak-sync"

/**
 * The one periodic job: read the usage-event log since the last checkpoint (see
 * UsageEventReader), queue whatever it found, flush the queue to the backend in
 * ≤500-event batches, and report battery + timezone alongside — everything the Windows
 * agent's 15-minute sync tick does, just on Android's own periodic-work model instead of
 * a continuously-running app with an in-process timer.
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as YurakApplication).container
        val credentials = container.credentialStore.load() ?: return Result.success() // logged out — nothing to do

        if (UsageAccess.hasAccess(applicationContext)) {
            trackUsage(container.database, container.trackingState)
        }
        // If Usage Access isn't granted yet, there's simply nothing to read — not an error,
        // just nothing to do this tick. The status screen surfaces that separately.

        val synced = flushQueue(container.database, container.apiClient, credentials.apiToken)
        if (!synced) {
            // Token rejected — revoked from another device, most likely. Clear the local
            // session so the app shows the login screen again; don't keep retrying with a
            // dead token.
            container.credentialStore.clear()
            container.trackingState.clear()
            return Result.success()
        }

        reportBatteryStatus(container, credentials)
        reportTimezone(container, credentials)

        return Result.success()
    }

    private suspend fun trackUsage(database: AppDatabase, state: TrackingState) {
        val usageStatsManager =
            applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val reader = UsageEventReader(usageStatsManager, applicationContext.packageManager)

        val now = System.currentTimeMillis()
        // +1: queryEvents' range is treated as inclusive of its start, so re-querying from
        // exactly the last consumed event's own timestamp would risk processing it twice.
        val since = state.lastCheckpointMs() + 1
        val result = reader.read(state.openSegment(), since, now)

        state.setLastCheckpointMs(result.newCheckpointMs)
        state.setOpenSegment(result.newOpenSegment)

        // Read network traffic deltas since last traffic checkpoint
        val trafficReader = NetworkTrafficReader(applicationContext, applicationContext.packageManager)
        val trafficSince = state.lastTrafficCheckpointMs()
        val trafficStats = trafficReader.readTraffic(trafficSince, now)
        state.setLastTrafficCheckpointMs(now)

        val charging = readBatteryStatus(applicationContext)?.second

        val entities = buildList {
            for (segment in result.closedSegments) {
                add(
                    PendingEventEntity(
                        eventType = "app_usage",
                        appIdentifier = segment.packageName,
                        appDisplayName = segment.displayName,
                        startedAt = Instant.ofEpochMilli(segment.startedAtMs).toString(),
                        endedAt = Instant.ofEpochMilli(segment.endedAtMs).toString(),
                        durationSeconds = ((segment.endedAtMs - segment.startedAtMs) / 1000).toInt(),
                        isCharging = charging,
                        systemEventType = null,
                        sessionStart = segment.isSessionStart,
                    )
                )
            }
            for (traffic in trafficStats) {
                add(
                    PendingEventEntity(
                        eventType = "app_usage",
                        appIdentifier = traffic.packageName,
                        appDisplayName = traffic.appDisplayName,
                        startedAt = Instant.ofEpochMilli(trafficSince).toString(),
                        endedAt = Instant.ofEpochMilli(now).toString(),
                        durationSeconds = 0,
                        isCharging = charging,
                        systemEventType = null,
                        sessionStart = false,
                        wifiBytes = traffic.wifiBytes,
                        cellularBytes = traffic.mobileBytes,
                        hotspotBytes = traffic.hotspotBytes,
                        totalBytes = traffic.totalBytes,
                    )
                )
            }
            for (transition in result.systemTransitions) {
                add(
                    PendingEventEntity(
                        eventType = "system",
                        appIdentifier = null,
                        appDisplayName = null,
                        startedAt = Instant.ofEpochMilli(transition.atMs).toString(),
                        endedAt = null,
                        durationSeconds = null,
                        isCharging = null,
                        systemEventType = transition.kind,
                        sessionStart = null,
                    )
                )
            }
        }

        if (entities.isNotEmpty()) {
            val dao = database.pendingEvents()
            dao.insertAll(entities)

            val excess = dao.count() - MAX_PENDING_EVENTS
            if (excess > 0) dao.deleteByIds(dao.oldestIds(excess))
        }
    }

    /** @return false only if the api_token itself was rejected. A network/offline failure
     * (or the backend rejecting a batch) returns true and stops — nothing is lost, every
     * event not yet confirmed is still sitting in the queue for next time. */
    private suspend fun flushQueue(
        database: AppDatabase,
        apiClient: ApiClient,
        apiToken: String,
    ): Boolean {
        val dao = database.pendingEvents()
        val pending = dao.listAll()
        if (pending.isEmpty()) return true

        for (batch in pending.chunked(SYNC_BATCH_SIZE)) {
            when (apiClient.postEvents(apiToken, batch.map { it.toEventDto() })) {
                is ApiResult.Ok -> dao.delete(batch)
                is ApiResult.Unauthorized -> return false
                is ApiResult.Failure -> return true // stop here; this batch + the rest retry next tick
            }
        }
        return true
    }

    private suspend fun reportBatteryStatus(container: AppContainer, credentials: StoredCredentials) {
        val (level, charging) = readBatteryStatus(applicationContext) ?: return
        container.apiClient.putBattery(credentials.apiToken, level, charging)
    }

    private suspend fun reportTimezone(container: AppContainer, credentials: StoredCredentials) {
        val offsetMinutes = ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 60
        container.apiClient.putTimezone(credentials.apiToken, offsetMinutes)
    }

    private fun readBatteryStatus(context: Context): Pair<Int, Boolean>? {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        // FULL counts as "charging" too — matches the Windows agent's BatteryMonitor:
        // "charging" means "on AC power", not strictly "gaining charge right now".
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        return (level * 100 / scale) to charging
    }

    companion object {
        /** Registers the periodic sync job — call once after login (and once at app start if
         * already logged in). KEEP, not REPLACE: re-calling this on every app launch shouldn't
         * reset an already-scheduled job's timing. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /** User-triggered "Sync now" — a one-off request that runs (approximately)
         * immediately rather than waiting for the next periodic tick. */
        fun runOnce(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

private fun PendingEventEntity.toEventDto() = EventDto(
    eventType = eventType,
    appIdentifier = appIdentifier,
    appDisplayName = appDisplayName,
    startedAt = startedAt,
    endedAt = endedAt,
    durationSeconds = durationSeconds,
    isCharging = isCharging,
    systemEventType = systemEventType,
    sessionStart = sessionStart,
    wifiBytes = wifiBytes,
    cellularBytes = cellularBytes,
    hotspotBytes = hotspotBytes,
    totalBytes = totalBytes,
)
