package com.yurak.agent.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
     * Retrieves genuine system app usage stats for a specific calendar date in Tashkent timezone (UTC+5).
     * Uses chronological UsageEvents (matching Digital Wellbeing) to guarantee that:
     * 1. Screen time never exceeds 24 hours.
     * 2. Only active foreground apps are counted while screen was unlocked.
     * 3. Background services and parallel processes do NOT multiply time into impossible numbers (e.g. 61 hours).
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

        try {
            val events = usageStatsManager.queryEvents(startMs, endMs)
            val event = UsageEvents.Event()

            var currentPkg: String? = null
            var currentStartMs: Long = 0L
            var isScreenInteractive = true

            fun recordSegment(pkg: String, fromMs: Long, toMs: Long) {
                val clampedFrom = max(fromMs, startMs)
                val clampedTo = min(toMs, endMs)
                val durationMs = clampedTo - clampedFrom
                // Ignore micro-glitches (< 500ms) or absurdly long unclosed spans (> 3 hours)
                if (durationMs in 500L..(3 * 3600 * 1000L)) {
                    appDurationsMs[pkg] = (appDurationsMs[pkg] ?: 0L) + durationMs
                }
            }

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val time = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        val pkg = event.packageName
                        if (pkg != null) {
                            val prevPkg = currentPkg
                            if (prevPkg != null && isScreenInteractive) {
                                recordSegment(prevPkg, currentStartMs, time)
                            }
                            if (currentPkg != pkg) {
                                appSessionCounts[pkg] = (appSessionCounts[pkg] ?: 0) + 1
                            }
                            currentPkg = pkg
                            currentStartMs = time
                        }
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val pkg = event.packageName
                        val activePkg = currentPkg
                        if (activePkg != null && activePkg == pkg && isScreenInteractive) {
                            recordSegment(activePkg, currentStartMs, time)
                            currentPkg = null
                        }
                    }

                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        val activePkg = currentPkg
                        if (activePkg != null && isScreenInteractive) {
                            recordSegment(activePkg, currentStartMs, time)
                        }
                        isScreenInteractive = false
                        currentPkg = null
                    }

                    UsageEvents.Event.SCREEN_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_HIDDEN -> {
                        isScreenInteractive = true
                        currentStartMs = time
                    }
                }
            }

            // If app is currently open right now today:
            val activePkg = currentPkg
            if (isToday && isScreenInteractive && activePkg != null) {
                recordSegment(activePkg, currentStartMs, endMs)
            }
        } catch (e: Exception) {
            // queryEvents failed or empty
        }

        // Fallback for older days where Android may have truncated raw events (> 7-10 days ago):
        if (appDurationsMs.isEmpty() && !isToday) {
            try {
                val statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
                for (stat in statsList) {
                    val sec = stat.totalTimeInForeground / 1000L
                    if (sec > 5L) {
                        appDurationsMs[stat.packageName] = min(sec * 1000L, 12 * 3600 * 1000L)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        val appList = mutableListOf<RealAppUsage>()

        for ((pkgName, durationMs) in appDurationsMs) {
            val totalSeconds = durationMs / 1000L
            if (totalSeconds < 3L) continue // ignore under 3 seconds

            // Filter out internal system daemons without any UI
            if (pkgName == "android" || pkgName == "com.android.systemui") continue

            val (appName, iconBitmap) = resolveAppInfo(pkgName)
            val badge = CategoryClassifier.classify(pkgName, appName)

            appList.add(
                RealAppUsage(
                    packageName = pkgName,
                    appName = appName,
                    totalSeconds = totalSeconds,
                    sessionCount = appSessionCounts[pkgName] ?: 1,
                    iconBitmap = iconBitmap,
                    categoryBadge = badge,
                    isRestricted = badge.label == "Cheklangan",
                    isNearLimit = badge.label == "Cheklovga yaqin",
                    platform = "android",
                )
            )
        }

        appList.sortByDescending { it.totalSeconds }

        // Total screen time is sum of active apps (capped at 24h max)
        val totalDaySeconds = min(appList.sumOf { it.totalSeconds }, 86400L)

        return DayUsageSummary(
            date = date,
            dayOfWeekLabel = dayOfWeekLabel,
            fullDayName = fullDayName,
            totalSeconds = totalDaySeconds,
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
        } catch (e: Exception) {
            val fallback = pkgName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            Pair(fallback, null)
        }
    }
}
