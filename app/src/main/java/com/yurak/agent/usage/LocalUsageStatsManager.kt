package com.yurak.agent.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.yurak.agent.util.DrawableUtils
import com.yurak.agent.util.TashkentTime
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

class LocalUsageStatsManager(private val context: Context) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager: PackageManager = context.packageManager

    private val launcherPackage: String? by lazy {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
    }

    /**
     * System packages that are NOT user applications (keyboards, system UI, launchers).
     * Filtered out to match Android Digital Wellbeing exactly.
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

    /**
     * Retrieves genuine system app usage stats for a specific calendar date in Tashkent timezone (UTC+5).
     * Follows Android Digital Wellbeing architecture:
     * - Only measures active foreground Activities when the screen is interactive.
     * - Does NOT include background audio playback or foreground services (preventing 3h+ background audio bug).
     * - Bounded strictly to <= 24 hours per day.
     */
    fun getDayStats(date: LocalDate): DayUsageSummary {
        val today = TashkentTime.today()
        val isFuture = date.isAfter(today)
        val isToday = date.isEqual(today)

        val dayOfWeekLabel = TashkentTime.shortDayLabel(date)
        val fullDayName = TashkentTime.fullDayName(date)

        if (isFuture) {
            return DayUsageSummary(
                date = date,
                dayOfWeekLabel = dayOfWeekLabel,
                fullDayName = fullDayName,
                totalSeconds = 0L,
                isToday = false,
                isFuture = true,
                apps = emptyList(),
            )
        }

        val startMs = TashkentTime.startOfDayMs(date)
        val endMs = if (isToday) System.currentTimeMillis() else TashkentTime.endOfDayMs(date)

        val appDurationsMs = mutableMapOf<String, Long>()
        val appSessionCounts = mutableMapOf<String, Int>()

        var totalScreenOnMs = 0L
        var screenOnTime: Long? = null
        var isScreenOn = false
        var firstScreenEventSeen = false

        var currentApp: String? = null
        var currentAppStart: Long = 0L

        fun closeAppSegment(pkg: String, toMs: Long) {
            val fromMs = max(currentAppStart, startMs)
            val clampedTo = min(toMs, endMs)
            val dur = clampedTo - fromMs
            if (dur > 500L) {
                appDurationsMs[pkg] = (appDurationsMs[pkg] ?: 0L) + dur
            }
        }

        var earliestAppActivityTime = Long.MAX_VALUE

        try {
            val events = usageStatsManager.queryEvents(startMs, endMs)
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val time = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_HIDDEN -> {
                        firstScreenEventSeen = true
                        if (!isScreenOn) {
                            isScreenOn = true
                            screenOnTime = time
                            if (currentApp != null) {
                                currentAppStart = time
                            }
                        }
                    }

                    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_SHOWN -> {
                        if (!firstScreenEventSeen) {
                            firstScreenEventSeen = true
                            totalScreenOnMs += max(0L, time - startMs)
                        } else if (isScreenOn) {
                            if (currentApp != null) {
                                closeAppSegment(currentApp!!, time)
                            }
                            if (screenOnTime != null) {
                                totalScreenOnMs += max(0L, time - screenOnTime!!)
                                screenOnTime = null
                            }
                            isScreenOn = false
                        }
                    }

                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        val pkg = event.packageName
                        if (pkg != null && pkg != launcherPackage && pkg !in excludedPackages) {
                            if (time < earliestAppActivityTime) {
                                earliestAppActivityTime = time
                            }
                            if (isScreenOn && currentApp != null && currentApp != pkg) {
                                closeAppSegment(currentApp!!, time)
                            }
                            if (currentApp != pkg) {
                                appSessionCounts[pkg] = (appSessionCounts[pkg] ?: 0) + 1
                            }
                            currentApp = pkg
                            currentAppStart = time
                        } else if (pkg == launcherPackage || pkg in excludedPackages) {
                            if (isScreenOn && currentApp != null) {
                                closeAppSegment(currentApp!!, time)
                            }
                            currentApp = null
                        }
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val pkg = event.packageName
                        if (pkg != null && pkg == currentApp && isScreenOn) {
                            closeAppSegment(pkg, time)
                            currentApp = null
                        }
                    }
                }
            }

            // Close trailing active session today
            if (isToday && isScreenOn) {
                if (currentApp != null) {
                    closeAppSegment(currentApp!!, endMs)
                }
                if (screenOnTime != null) {
                    totalScreenOnMs += max(0L, endMs - screenOnTime!!)
                }
            }
        } catch (_: Exception) {}

        // Reconcile past days where Android OS purged raw events older than 24h
        if (!isToday && earliestAppActivityTime > startMs + 1800_000L) {
            val prunedEndMs = min(earliestAppActivityTime, endMs)
            Log.i("YurakStats", "Date $date: Reconciling gap from startMs to $prunedEndMs (first app activity was at $earliestAppActivityTime)")
            try {
                val dailyStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startMs - 86400_000L,
                    endMs
                )
                dailyStats?.forEach { st ->
                    val pkg = st.packageName
                    if (pkg != null && pkg != launcherPackage && pkg !in excludedPackages) {
                        val bucketStart = st.firstTimeStamp
                        val bucketEnd = st.lastTimeStamp
                        val overlapStart = max(startMs, bucketStart)
                        val overlapEnd = min(prunedEndMs, bucketEnd)
                        val overlapMs = overlapEnd - overlapStart
                        val bucketDur = bucketEnd - bucketStart

                        if (overlapMs > 0 && bucketDur > 0) {
                            val ratio = (overlapMs.toDouble() / bucketDur).coerceIn(0.0, 1.0)
                            // Use totalTimeVisible if available (API 29+), else totalTimeInForeground
                            val rawTime = if (android.os.Build.VERSION.SDK_INT >= 29 && st.totalTimeVisible > 0L) {
                                st.totalTimeVisible
                            } else {
                                st.totalTimeInForeground
                            }
                            val missingMs = (rawTime * ratio).toLong()
                            if (missingMs > 1000L) {
                                appDurationsMs[pkg] = (appDurationsMs[pkg] ?: 0L) + missingMs
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("YurakStats", "Reconciliation error: ${e.message}")
            }
        }

        Log.i("YurakStats", "Date $date: event-based telegram=${(appDurationsMs["org.telegram.messenger"] ?: 0L) / 1000L}s, total apps=${appDurationsMs.size}")

        val appList = mutableListOf<RealAppUsage>()

        for ((pkgName, durationMs) in appDurationsMs) {
            val totalSeconds = durationMs / 1000L
            if (totalSeconds < 5L) continue

            val (appName, iconBitmap) = resolveAppInfo(pkgName)
            val badge = CategoryClassifier.classify(pkgName, appName)
            val sessions = max(1, appSessionCounts[pkgName] ?: 1)

            appList.add(
                RealAppUsage(
                    packageName = pkgName,
                    appName = appName,
                    totalSeconds = totalSeconds,
                    sessionCount = sessions,
                    iconBitmap = iconBitmap,
                    categoryBadge = badge,
                    isRestricted = badge.label == "Cheklangan",
                    isNearLimit = badge.label == "Cheklovga yaqin",
                    platform = "android",
                )
            )
        }

        appList.sortByDescending { it.totalSeconds }

        // Total screen time in Digital Wellbeing is the sum of active user app usages (excluding lockscreen idle)
        val appsSumSec = appList.sumOf { it.totalSeconds }
        val finalScreenSeconds = min(appsSumSec, 86400L)
        Log.i("YurakStats", "Date $date: finalScreenSeconds=$finalScreenSeconds (${finalScreenSeconds / 3600}h ${(finalScreenSeconds % 3600) / 60}m)")

        return DayUsageSummary(
            date = date,
            dayOfWeekLabel = dayOfWeekLabel,
            fullDayName = fullDayName,
            totalSeconds = finalScreenSeconds,
            isToday = isToday,
            isFuture = false,
            apps = appList,
        )
    }

    /**
     * Retrieves full 7-day week usage stats in Tashkent timezone (UTC+5).
     * Future days have totalSeconds = 0 and isFuture = true.
     */
    fun getWeekStats(anchorDate: LocalDate): WeekUsageSummary {
        val days = TashkentTime.getWeekDays(anchorDate)
        val daySummaries = days.map { getDayStats(it) }

        val startDate = days.first()
        val endDate = days.last()
        val rangeLabel = TashkentTime.formatWeekRange(startDate, endDate)
        val totalWeekSeconds = daySummaries.filter { !it.isFuture }.sumOf { it.totalSeconds }

        return WeekUsageSummary(
            startDate = startDate,
            endDate = endDate,
            rangeLabel = rangeLabel,
            totalSeconds = totalWeekSeconds,
            days = daySummaries,
        )
    }

    private fun resolveAppInfo(pkgName: String): Pair<String, androidx.compose.ui.graphics.ImageBitmap?> {
        if (pkgName == launcherPackage || pkgName == "home") {
            return Pair("Home screen", null)
        }
        return try {
            val appInfo = packageManager.getApplicationInfo(pkgName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString()
            val drawable = packageManager.getApplicationIcon(appInfo)
            val icon = DrawableUtils.toImageBitmap(pkgName, drawable)
            Pair(label, icon)
        } catch (_: Exception) {
            val fallback = pkgName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            Pair(fallback, null)
        }
    }
}
