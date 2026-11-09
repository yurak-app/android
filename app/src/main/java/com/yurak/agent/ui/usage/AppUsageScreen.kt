package com.yurak.agent.ui.usage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.ui.YurakBackground
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.usage.*
import com.yurak.agent.util.TashkentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun AppUsageScreen(
    userEmail: String,
    device: DeviceItemDto?,
    syncing: Boolean,
    onSyncNow: () -> Unit,
) {
    val context = LocalContext.current
    val usageManager = remember { LocalUsageStatsManager(context) }

    var period by remember { mutableStateOf(UsagePeriod.HAFTA) }
    var anchorDate by remember { mutableStateOf(TashkentTime.today()) }

    var weekStats by remember { mutableStateOf<WeekUsageSummary?>(null) }
    var selectedDayIndex by remember { mutableStateOf(0) }

    var filter by remember { mutableStateOf(AppFilter.BARCHASI) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    // Load real local stats on background dispatcher
    LaunchedEffect(anchorDate) {
        loading = true
        withContext(Dispatchers.IO) {
            val stats = usageManager.getWeekStats(anchorDate)
            withContext(Dispatchers.Main) {
                weekStats = stats
                val today = TashkentTime.today()
                val todayIdx = stats.days.indexOfFirst { it.date == today }
                selectedDayIndex = if (todayIdx >= 0) todayIdx else stats.days.indexOfLast { !it.isFuture }.coerceAtLeast(0)
                loading = false
            }
        }
    }

    val currentWeek = weekStats
    val selectedDay = currentWeek?.days?.getOrNull(selectedDayIndex) ?: currentWeek?.days?.firstOrNull()

    val rawApps = selectedDay?.apps ?: emptyList()

    val filteredApps = remember(rawApps, filter, searchQuery) {
        rawApps.filter { app ->
            val matchesFilter = when (filter) {
                AppFilter.BARCHASI, AppFilter.MOBILE -> true
                AppFilter.CHEKLANGAN -> app.isRestricted || app.isNearLimit
                AppFilter.CHEKLANMAGAN -> !app.isRestricted && !app.isNearLimit
                AppFilter.DESKTOP -> false
            }
            val matchesQuery = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Device Info Header Card
            item {
                DeviceHeaderCard(userEmail = userEmail, device = device)
            }

            // 2. Period Switcher (Kun | Hafta | Oy)
            item {
                PeriodSelector(
                    selectedPeriod = period,
                    onPeriodSelected = { period = it },
                )
            }

            // 3. Weekly Bar Chart Card
            if (currentWeek != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                        border = BorderStroke(1.dp, YurakBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            // Date Range Stepper Header
                            WeekRangeStepper(
                                startDate = currentWeek.startDate,
                                endDate = currentWeek.endDate,
                                selectedDate = selectedDay?.date ?: TashkentTime.today(),
                                onPreviousWeek = { anchorDate = anchorDate.minusWeeks(1) },
                                onNextWeek = { anchorDate = anchorDate.plusWeeks(1) },
                                canGoNext = !currentWeek.days.any { it.isToday },
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Metric: Kunlik foydalanish
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kunlik foydalanish",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = YurakSlate,
                                        ),
                                    )
                                }

                                Text(
                                    text = TashkentTime.formatDuration(selectedDay?.totalSeconds ?: 0L),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                    ),
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Interactive 7-Day Stacked Bar Chart
                            WeeklyUsageBarChart(
                                days = currentWeek.days,
                                selectedIndex = selectedDayIndex,
                                onSelectDay = { selectedDayIndex = it },
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Chart Footer: Haftalik jami
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Haftalik jami",
                                    style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                                )
                                Text(
                                    text = TashkentTime.formatDuration(currentWeek.totalSeconds),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = YurakSlate,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // 4. Selected Day Summary Card
            if (selectedDay != null) {
                item {
                    SelectedDayCard(
                        date = selectedDay.date,
                        activeAppCount = rawApps.size,
                        totalSeconds = selectedDay.totalSeconds,
                    )
                }
            }

            // 5. Search Bar
            item {
                AppUsageSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
            }

            // 6. Filter Chips
            item {
                AppUsageFilterChips(
                    selectedFilter = filter,
                    onFilterSelected = { filter = it },
                    allApps = rawApps,
                )
            }

            // 7. Real App List Items
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF0284C7),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (selectedDay?.isFuture == true) "Kelgusi kunlar uchun hali ma'lumot yo'q"
                            else "Ushbu kunda foydalanilgan ilovalar mavjud emas",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppUsageListItem(app = app)
                }
            }
        }

        // Floating Sticky Sync Button at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onSyncNow,
                    enabled = !syncing,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YurakSlate),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    if (syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yuborilmoqda…", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("O'zgarishlarni yuborish", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE0F2FE),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { onSyncNow() },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0284C7))
                    }
                }
            }
        }
    }
}

