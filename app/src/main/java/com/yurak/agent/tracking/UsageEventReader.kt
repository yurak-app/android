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

/** Segments shorter than this aren't worth a row. */
private const val MIN_SEGMENT_MS = 1_000L

/** Max segment duration before forcing a checkpoint split. */
private const val MAX_SEGMENT_MS = 30 * 60 * 1000L

/**
 * Reconstructs foreground-app segments and lock/unlock transitions from Android's own
 * usage-event log, exactly mirroring Digital Wellbeing and LocalUsageStatsManager:
 * - Excludes keyboards, system UI, launchers, and system packages from app usage.
 * - Handles MOVE_TO_BACKGROUND / ACTIVITY_PAUSED to cleanly close segments.
 * - Emits discrete lock/unlock transitions on screen-off / lock.
 */
class UsageEventReader(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
) {
    private val launcherPackage: String? by lazy {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
    }

    /**
     * System packages that are NOT user applications (keyboards, system UI, launchers).
     * Excluded to match Android Digital Wellbeing exactly.
     */
    private val excludedPackages = setOf(
        "com.android.systemui",
        "com.google.android.inputmethod.latin",
        "com.touchtype.swiftkey",
        "com.google.android.apps.nexuslauncher",
        "com.miui.home",
        "com.android.launcher3",
        "com.google.android.gms",
        "com.google.android.gsf",
        "android",
    )

    fun read(previousOpen: OpenSegment?, sinceMs: Long, nowMs: Long): ReadResult {
        val events = usageStatsManager.queryEvents(sinceMs, nowMs)
        val event = UsageEvents.Event()

        val closed = mutableListOf<ClosedSegment>()
        val transitions = mutableListOf<SystemTransition>()

        var open = previousOpen
        var checkpoint = sinceMs

        fun addTransition(kind: String, atMs: Long) {
            if (transitions.lastOrNull()?.kind != kind) {
                transitions += SystemTransition(kind, atMs)
            }
        }

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
                // MOVE_TO_FOREGROUND / ACTIVITY_RESUMED (same value: 1)
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    val pkg = event.packageName
                    if (pkg != null && pkg != launcherPackage && pkg !in excludedPackages) {
                        if (open?.packageName != pkg) {
                            closeOpen(event.timeStamp)
                            open = OpenSegment(pkg, event.timeStamp, isSessionStart = true)
                        }
                    } else if (pkg == launcherPackage || pkg in excludedPackages) {
                        closeOpen(event.timeStamp)
                    }
                }
                // MOVE_TO_BACKGROUND / ACTIVITY_PAUSED (same value: 2)
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val pkg = event.packageName
                    if (pkg != null && pkg == open?.packageName) {
                        closeOpen(event.timeStamp)
                    }
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_SHOWN -> {
                    closeOpen(event.timeStamp)
                    addTransition("lock", event.timeStamp)
                }
                UsageEvents.Event.SCREEN_INTERACTIVE,
                UsageEvents.Event.KEYGUARD_HIDDEN -> {
                    addTransition("unlock", event.timeStamp)
                }
            }
        }

        open?.let { current ->
            if (nowMs - current.startedAtMs >= MAX_SEGMENT_MS) {
                closeOpen(nowMs)
                open = OpenSegment(current.packageName, nowMs, isSessionStart = false)
            }
        }

        return ReadResult(closed, transitions, checkpoint, open)
    }

    private fun displayNameFor(packageName: String): String? =
        try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info)?.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
}
