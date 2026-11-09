package com.yurak.agent.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager

/** One closed foreground segment, ready to become an `app_usage` EventDto. */
data class ClosedSegment(
    val packageName: String,
    val displayName: String?,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val isSessionStart: Boolean,
)

/** A lock/unlock instant, ready to become a `system` EventDto. */
data class SystemTransition(val kind: String, val atMs: Long) // kind: "lock" | "unlock"

data class ReadResult(
    val closedSegments: List<ClosedSegment>,
    val systemTransitions: List<SystemTransition>,
    val newCheckpointMs: Long,
    val newOpenSegment: OpenSegment?,
)

/** Segments shorter than this aren't worth a row — mirrors MIN_SEGMENT_SECONDS on the
 * Windows agent and the extension. */
private const val MIN_SEGMENT_MS = 1_000L

/** If a segment has been open this long without closing (an app left running for hours,
 * or several worker runs in a row that never saw it end), force a checkpoint split —
 * same idea as the Windows agent's ForegroundWindowTracker.MaxSegmentDuration, just
 * evaluated once per periodic read instead of continuously. Bounds how much a single
 * "session" can silently span (a midnight boundary, a crash) rather than how often a
 * genuinely continuous stretch gets chopped up — 30 min is generous on purpose. */
private const val MAX_SEGMENT_MS = 30 * 60 * 1000L

/**
 * Reconstructs foreground-app segments and lock/unlock transitions from Android's own
 * usage-event log — the Android equivalent of the Windows agent's ForegroundWindowTracker,
 * but reading a log the OS already kept instead of polling in real time (see the Android
 * README for why this needs no foreground service). Pure-ish: takes the previous run's
 * state in, returns the next state out; SyncWorker is the only thing that actually persists
 * it (TrackingState) or enqueues the result (PendingEventDao).
 */
class UsageEventReader(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
) {
    private val launcherPackage: String? by lazy {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
    }

    fun read(previousOpen: OpenSegment?, sinceMs: Long, nowMs: Long): ReadResult {
        val events = usageStatsManager.queryEvents(sinceMs, nowMs)
        val event = UsageEvents.Event()

        val closed = mutableListOf<ClosedSegment>()
        val transitions = mutableListOf<SystemTransition>()

        var open = previousOpen
        var checkpoint = sinceMs

        fun closeOpen(atMs: Long) {
            val current = open ?: return
            if (atMs - current.startedAtMs >= MIN_SEGMENT_MS) {
                closed += ClosedSegment(
                    packageName = current.packageName,
                    displayName = displayNameFor(current.packageName),
                    startedAtMs = current.startedAtMs,
                    endedAtMs = atMs,
                    isSessionStart = current.isSessionStart,
                )
            }
            open = null
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            checkpoint = event.timeStamp

            when (event.eventType) {
                // MOVE_TO_FOREGROUND (not the newer ACTIVITY_RESUMED alias, same underlying
                // value): available since API 21, comfortably below this app's minSdk 28,
                // where ACTIVITY_RESUMED wasn't introduced until API 29.
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    val pkg = normalizePackage(event.packageName)
                    if (open?.packageName != pkg) {
                        closeOpen(event.timeStamp)
                        // Always a fresh session here: either a genuinely different app, or
                        // the same app resuming after `open` was cleared by a screen-off
                        // below — either way this is a new open, never a continuation (the
                        // "continuation" case — the same app, no lock in between — is the
                        // branch that's skipped entirely, just below).
                        open = OpenSegment(pkg, event.timeStamp, isSessionStart = true)
                    }
                    // Same package resuming again (an internal activity-to-activity
                    // navigation within one app) is a no-op — the segment just continues;
                    // ACTIVITY_PAUSED is deliberately not handled at all, for this reason.
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    closeOpen(event.timeStamp)
                    transitions += SystemTransition("lock", event.timeStamp)
                }
                UsageEvents.Event.KEYGUARD_HIDDEN -> {
                    transitions += SystemTransition("unlock", event.timeStamp)
                }
                // SCREEN_INTERACTIVE and KEYGUARD_SHOWN are deliberately not separately
                // emitted: "unlock" means genuinely usable again (keyguard dismissed, not
                // just the screen waking up to a lock screen), and "lock" already covers
                // screen-off regardless of whether the keyguard was ever shown.
            }
        }

        // A segment left open past the whole read window: still genuinely open (nothing
        // closed it), but checkpoint it if it's been going long enough that this is really
        // a crash/midnight-boundary concern, not just "still using the same app".
        open?.let { current ->
            if (nowMs - current.startedAtMs >= MAX_SEGMENT_MS) {
                closeOpen(nowMs)
                open = OpenSegment(current.packageName, nowMs, isSessionStart = false)
            }
        }

        return ReadResult(closed, transitions, checkpoint, open)
    }

    private fun normalizePackage(packageName: String): String =
        if (packageName == launcherPackage) "home" else packageName

    private fun displayNameFor(packageName: String): String? {
        if (packageName == "home") return "Home screen"
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info)?.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null // uninstalled since, or a system component with no launchable app info
        }
    }
}
