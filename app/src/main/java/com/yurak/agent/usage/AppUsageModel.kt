package com.yurak.agent.usage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import java.time.LocalDate

enum class UsagePeriod {
    KUN, HAFTA, OY
}

enum class AppFilter {
    BARCHASI, CHEKLANGAN, CHEKLANMAGAN, MOBILE, DESKTOP
}

data class AppCategoryBadge(
    val label: String,
    val containerColor: Color,
    val textColor: Color,
)

data class RealAppUsage(
    val packageName: String,
    val appName: String,
    val totalSeconds: Long,
    val sessionCount: Int = 1,
    val iconBitmap: ImageBitmap? = null,
    val categoryBadge: AppCategoryBadge,
    val isRestricted: Boolean = false,
    val isNearLimit: Boolean = false,
    val platform: String = "android",
)

data class DayUsageSummary(
    val date: LocalDate,
    val dayOfWeekLabel: String,
    val fullDayName: String,
    val totalSeconds: Long,
    val isToday: Boolean,
    val isFuture: Boolean,
    val apps: List<RealAppUsage>,
)

data class WeekUsageSummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val rangeLabel: String,
    val totalSeconds: Long,
    val days: List<DayUsageSummary>,
)
