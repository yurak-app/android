package com.yurak.agent.usage

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.yurak.agent.util.DrawableUtils
import com.yurak.agent.util.TashkentTime
import java.time.LocalDate

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

        val aggregatedMap: Map<String, UsageStats> = try {
            usageStatsManager.queryAndAggregateUsageStats(startMs, endMs)
        } catch (e: Exception) {
            emptyMap()
        }

        val appList = mutableListOf<RealAppUsage>()

        for ((pkgName, stats) in aggregatedMap) {
            val totalSeconds = stats.totalTimeInForeground / 1000L
            if (totalSeconds < 5L) continue // ignore negligible noise

            val (appName, iconBitmap) = resolveAppInfo(pkgName)
            val badge = CategoryClassifier.classify(pkgName, appName)

            appList.add(
                RealAppUsage(
                    packageName = pkgName,
                    appName = appName,
                    totalSeconds = totalSeconds,
                    sessionCount = 1,
                    iconBitmap = iconBitmap,
                    categoryBadge = badge,
                    isRestricted = badge.label == "Cheklangan",
                    isNearLimit = badge.label == "Cheklovga yaqin",
                    platform = "android",
                )
            )
        }

        appList.sortByDescending { it.totalSeconds }
        val totalDaySeconds = appList.sumOf { it.totalSeconds }

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
