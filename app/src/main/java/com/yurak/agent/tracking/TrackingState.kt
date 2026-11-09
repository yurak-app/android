package com.yurak.agent.tracking

import android.content.Context
import com.yurak.agent.util.TashkentTime

/** The foreground segment [UsageEventReader] hadn't seen the end of yet, as of the last
 * checkpoint — persisted so a later worker run (a fresh process, most likely) can pick up
 * exactly where the last one left off instead of losing track of "what's still open". */
data class OpenSegment(
    val packageName: String,
    val startedAtMs: Long,
    val isSessionStart: Boolean,
)

/**
 * Cross-run state for the usage-event reader — SyncWorker's process is not long-lived like
 * the Windows agent's, so anything that needs to survive between one 15-minute run and the
 * next (where the event log's next read should resume, and whatever segment was still open
 * when the last run ended) has to be written to disk, not just held in memory.
 */
class TrackingState(context: Context) {
    private val prefs = context.getSharedPreferences("yurak_tracking_state", Context.MODE_PRIVATE)

    /** The last usage-event timestamp actually consumed. Defaults to start of today (UTC+5) on
     * a fresh install so today's full usage is captured. */
    fun lastCheckpointMs(): Long {
        val defaultStart = TashkentTime.startOfDayMs(TashkentTime.today())
        return prefs.getLong(KEY_CHECKPOINT, defaultStart)
    }

    fun setLastCheckpointMs(value: Long) {
        prefs.edit().putLong(KEY_CHECKPOINT, value).apply()
    }

    /** The last traffic query timestamp consumed. Defaults to start of today (UTC+5). */
    fun lastTrafficCheckpointMs(): Long {
        val defaultStart = TashkentTime.startOfDayMs(TashkentTime.today())
        return prefs.getLong(KEY_TRAFFIC_CHECKPOINT, defaultStart)
    }

    fun setLastTrafficCheckpointMs(value: Long) {
        prefs.edit().putLong(KEY_TRAFFIC_CHECKPOINT, value).apply()
    }

    fun openSegment(): OpenSegment? {
        val pkg = prefs.getString(KEY_OPEN_PACKAGE, null) ?: return null
        val start = prefs.getLong(KEY_OPEN_STARTED_AT, -1L)
        if (start < 0) return null
        return OpenSegment(pkg, start, prefs.getBoolean(KEY_OPEN_SESSION_START, false))
    }

    fun setOpenSegment(segment: OpenSegment?) {
        val editor = prefs.edit()
        if (segment == null) {
            editor.remove(KEY_OPEN_PACKAGE).remove(KEY_OPEN_STARTED_AT).remove(KEY_OPEN_SESSION_START)
        } else {
            editor
                .putString(KEY_OPEN_PACKAGE, segment.packageName)
                .putLong(KEY_OPEN_STARTED_AT, segment.startedAtMs)
                .putBoolean(KEY_OPEN_SESSION_START, segment.isSessionStart)
        }
        editor.apply()
    }

    /** Wipes everything — used on logout. */
    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_CHECKPOINT = "last_checkpoint_ms"
        const val KEY_TRAFFIC_CHECKPOINT = "last_traffic_checkpoint_ms"
        const val KEY_OPEN_PACKAGE = "open_package"
        const val KEY_OPEN_STARTED_AT = "open_started_at_ms"
        const val KEY_OPEN_SESSION_START = "open_session_start"
    }
}
