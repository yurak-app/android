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

    private val launchablePackages: Set<String> by lazy {
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            packageManager.queryIntentActivities(intent, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    /**
     * System packages that run continuously in background or alongside user apps.
     * We exclude these from inflating screen time.
     */
    private val excludedSystemPackages = setOf(
        "com.android.systemui",
        "com.google.android.inputmethod.latin",
        "com.touchtype.swiftkey",
        "com.miui.home",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.google.android.gms",
        "com.google.android.gsf",
        "android",
    )

    /**
     * Retrieves genuine system app usage stats for a specific calendar date in Tashkent timezone (UTC+5).
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
        var screenOnStartTime: Long? = null
        var isScreenOn = true
        var firstScreenEventProcessed = false

        var currentPkg: String? = null
        var lastForegroundPkg: String? = null
        var currentStartMs: Long = startMs

        fun recordSegment(pkg: String, fromMs: Long, toMs: Long) {
            if (pkg in excludedSystemPackages || pkg == launcherPackage) return
            val clampedFrom = max(fromMs, startMs)
            val clampedTo = min(toMs, endMs)
            val durationMs = clampedTo - clampedFrom
            if (durationMs > 1000L) {
                appDurationsMs[pkg] = (appDurationsMs[pkg] ?: 0L) + durationMs
            }
        }

        try {
            val events = usageStatsManager.queryEvents(startMs, endMs)
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val time = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        if (!firstScreenEventProcessed) {
                            firstScreenEventProcessed = true
                        }
                        isScreenOn = true
                        if (screenOnStartTime == null) {
                            screenOnStartTime = time
                        }
                        // Restore last foreground app when screen turns on
                        if (currentPkg == null && lastForegroundPkg != null) {
                            currentPkg = lastForegroundPkg
                            currentStartMs = time
                        }
                    }

                    UsageEvents.Event.KEYGUARD_HIDDEN -> {
                        isScreenOn = true
                        if (screenOnStartTime == null) {
                            screenOnStartTime = time
                        }
                        if (currentPkg == null && lastForegroundPkg != null) {
                            currentPkg = lastForegroundPkg
                            currentStartMs = time
                        }
                    }

                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        if (!firstScreenEventProcessed) {
                            firstScreenEventProcessed = true
                            totalScreenOnMs += (time - startMs).coerceAtLeast(0L)
                        } else if (screenOnStartTime != null) {
                            totalScreenOnMs += (time - screenOnStartTime!!).coerceAtLeast(0L)
                            screenOnStartTime = null
                        }

                        if (currentPkg != null && isScreenOn) {
                            recordSegment(currentPkg!!, currentStartMs, time)
                        }
                        isScreenOn = false
                        lastForegroundPkg = currentPkg ?: lastForegroundPkg
                        currentPkg = null
                    }

                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        val pkg = event.packageName
                        if (pkg != null) {
                            val prevPkg = currentPkg
                            if (prevPkg != null && isScreenOn && prevPkg != pkg) {
                                recordSegment(prevPkg, currentStartMs, time)
                            }
                            if (currentPkg != pkg) {
                                appSessionCounts[pkg] = (appSessionCounts[pkg] ?: 0) + 1
                            }
                            currentPkg = pkg
                            lastForegroundPkg = pkg
                            currentStartMs = time
                        }
                    }

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val pkg = event.packageName
                        val active = currentPkg
                        if (active != null && active == pkg && isScreenOn) {
                            recordSegment(active, currentStartMs, time)
                            currentStartMs = time
                        }
                    }
                }
            }

            if (isToday && isScreenOn && currentPkg != null) {
                recordSegment(currentPkg!!, currentStartMs, endMs)
            }
            if (isToday && isScreenOn && screenOnStartTime != null) {
                totalScreenOnMs += (endMs - screenOnStartTime!!).coerceAtLeast(0L)
            }
        } catch (_: Exception) {}

        // Supplementary: merge with queryUsageStats for any user-facing apps
        try {
            val statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMs, endMs)
            for (stat in statsList) {
                val pkg = stat.packageName ?: continue
                if (pkg in excludedSystemPackages || pkg == launcherPackage) continue

                val isLaunchable = pkg in launchablePackages || packageManager.getLaunchIntentForPackage(pkg) != null
                if (!isLaunchable && !appDurationsMs.containsKey(pkg)) continue

                val statSeconds = stat.totalTimeInForeground / 1000L
                if (statSeconds >= 10L && stat.lastTimeUsed >= startMs) {
                    val currentSec = (appDurationsMs[pkg] ?: 0L) / 1000L
                    if (statSeconds > currentSec) {
                        appDurationsMs[pkg] = statSeconds * 1000L
                    }
                }
            }
        } catch (_: Exception) {}

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

        val totalScreenOnSec = totalScreenOnMs / 1000L
        val appsSumSec = appList.sumOf { it.totalSeconds }
        val finalScreenSeconds = when {
            totalScreenOnSec > 60L -> max(totalScreenOnSec, appList.firstOrNull()?.totalSeconds ?: 0L)
            appsSumSec > 0L -> min(appsSumSec, 86400L)
            else -> 0L
        }.coerceIn(0L, 86400L)

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
