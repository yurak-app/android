package com.yurak.agent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.data.DailyAppStatDto
import com.yurak.agent.data.DailyStatsResponseDto
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.data.RangeStatsResponseDto

enum class UsagePeriod {
    KUN, HAFTA, OY
}

enum class PlatformFilter {
    BARCHASI, MOBILE, DESKTOP
}

@Composable
fun AppUsageScreen(
    dailyStats: DailyStatsResponseDto?,
    rangeStats: RangeStatsResponseDto?,
    device: DeviceItemDto?,
    userEmail: String,
    syncing: Boolean,
    onSyncNow: () -> Unit,
) {
    var period by remember { mutableStateOf(UsagePeriod.HAFTA) }
    var selectedDayIndex by remember { mutableStateOf(4) }
    var platformFilter by remember { mutableStateOf(PlatformFilter.BARCHASI) }
    var searchQuery by remember { mutableStateOf("") }

    val daysOfWeek = listOf("Ya", "Du", "Se", "Cho", "Pay", "Ju", "Sha")
    val fullDayNames = listOf(
        "Yakshanba",
        "Dushanba",
        "Seshanba",
        "Chorshanba",
        "Payshanba",
        "Juma",
        "Shanba"
    )

    val rawApps = when (period) {
        UsagePeriod.KUN -> dailyStats?.apps ?: emptyList()
        else -> rangeStats?.topApps ?: dailyStats?.apps ?: emptyList()
    }

    val filteredApps = remember(rawApps, platformFilter, searchQuery) {
        rawApps.filter { app ->
            val matchesPlatform = when (platformFilter) {
                PlatformFilter.BARCHASI -> true
                PlatformFilter.MOBILE -> (app.platform ?: "android") == "android"
                PlatformFilter.DESKTOP -> (app.platform ?: "") in listOf("windows", "extension", "desktop")
            }
            val matchesQuery = searchQuery.isBlank() ||
                (app.displayName ?: app.identifier).contains(searchQuery, ignoreCase = true)
            matchesPlatform && matchesQuery
        }
    }

    val totalWeekSeconds = remember(rangeStats, dailyStats) {
        val rangeSum = rangeStats?.daily?.sumOf { it.totalSeconds } ?: 0
        if (rangeSum > 0) rangeSum else dailyStats?.apps?.sumOf { it.totalSeconds } ?: 0
    }

    val selectedDaySeconds = remember(dailyStats) {
        dailyStats?.screenSeconds ?: dailyStats?.apps?.sumOf { it.totalSeconds } ?: 18200
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
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                    border = BorderStroke(1.dp, YurakBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = Color(0xFF4338CA),
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userEmail.substringBefore("@"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = YurakSlate,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(YurakEmerald)
                                )
                            }
                            Text(
                                text = "${device?.name ?: "Xiaomi Qurilma"} • ${device?.platform?.uppercase() ?: "ANDROID"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Battery pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = if (device?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                    contentDescription = null,
                                    tint = YurakEmerald,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${device?.batteryLevel ?: 100}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // 2. Period Switcher (Kun | Hafta | Oy)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        UsagePeriod.values().forEach { itemPeriod ->
                            val selected = period == itemPeriod
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) YurakSky else Color.Transparent)
                                    .clickable { period = itemPeriod }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = when (itemPeriod) {
                                        UsagePeriod.KUN -> "Kun"
                                        UsagePeriod.HAFTA -> "Hafta"
                                        UsagePeriod.OY -> "Oy"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else YurakSlate,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // 3. Weekly Bar Chart Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledIconButton(
                                onClick = { if (selectedDayIndex > 0) selectedDayIndex-- },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF0284C7)
                                ),
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Oldingi",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = rangeStats?.let { "${it.start.takeLast(5)} - ${it.end.takeLast(5)}" } ?: "Joriy hafta",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = YurakSlate,
                                    ),
                                )
                                Text(
                                    text = "Tanlangan: ${fullDayNames[selectedDayIndex]}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = YurakSky),
                                )
                            }

                            FilledIconButton(
                                onClick = { if (selectedDayIndex < 6) selectedDayIndex++ },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Keyingi",
                                    tint = YurakSlate,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

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
                                    tint = YurakSky,
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
                                text = FormatUtils.formatDuration(selectedDaySeconds),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YurakSky,
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stacked Weekly Bar Chart
                        WeeklyStackedBarChart(
                            daysOfWeek = daysOfWeek,
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
                                "Haftalik jami",
                                style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                            )
                            Text(
                                FormatUtils.formatDuration(totalWeekSeconds),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YurakSlate,
                                ),
                            )
                        }
                    }
                }
            }

            // 4. Selected Day Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = YurakSky,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tanlangan kun: ${fullDayNames[selectedDayIndex]}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = YurakSlate,
                                ),
                            )
                            Text(
                                text = "${filteredApps.size} ta ilova faol bo'lgan",
                                style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFBAE6FD),
                        ) {
                            Text(
                                text = FormatUtils.formatDuration(selectedDaySeconds),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // 5. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Ilova nomini qidirish...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = YurakMuted)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YurakSky,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = YurakCardBg,
                        unfocusedContainerColor = YurakCardBg,
                        focusedTextColor = YurakSlate,
                        unfocusedTextColor = YurakSlate,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // 6. Platform Filter Chips (Barchasi, Mobile, Desktop)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlatformFilter.values().forEach { filter ->
                        val selected = platformFilter == filter
                        val count = when (filter) {
                            PlatformFilter.BARCHASI -> rawApps.size
                            PlatformFilter.MOBILE -> rawApps.count { (it.platform ?: "android") == "android" }
                            PlatformFilter.DESKTOP -> rawApps.count { (it.platform ?: "") in listOf("windows", "extension") }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) Color(0xFFCCFBF1) else YurakCardBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) Color(0xFF2DD4BF) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.clickable { platformFilter = filter },
                        ) {
                            Text(
                                text = when (filter) {
                                    PlatformFilter.BARCHASI -> "Barchasi ($count)"
                                    PlatformFilter.MOBILE -> "Mobile ($count)"
                                    PlatformFilter.DESKTOP -> "Desktop ($count)"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color(0xFF0F766E) else YurakSlate,
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }

            // 7. App List Items
            if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Mos keluvchi ilovalar topilmadi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    }
                }
            } else {
                items(filteredApps) { app ->
                    AppDetailRow(app = app)
                }
            }
        }

        // Floating Sticky Sync Button
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
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = YurakSky)
                    }
                }
            }
        }
    }
}

@Composable
fun AppDetailRow(app: DailyAppStatDto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorForIdentifier(app.identifier)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (app.displayName ?: app.identifier).take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.displayName ?: app.identifier.substringAfterLast("."),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = YurakSlate,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatUtils.formatDuration(app.totalSeconds),
                        style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val (badgeText, badgeBg, badgeTextColor) = getBadgeForApp(app)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg,
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTextColor
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

fun getBadgeForApp(app: DailyAppStatDto): Triple<String, Color, Color> {
    val id = app.identifier.lowercase()
    return when {
        id.contains("zoom") || id.contains("meet") -> Triple("Ta'lim", Color(0xFFEDE9FE), Color(0xFF6D28D9))
        id.contains("telegram") || id.contains("whatsapp") -> Triple("Aloqa", Color(0xFFE0E7FF), Color(0xFF4338CA))
        id.contains("instagram") || id.contains("tiktok") -> Triple("Ijtimoiy", Color(0xFFFFE4E6), Color(0xFFBE123C))
        id.contains("youtube") -> Triple("Media", Color(0xFFFEE2E2), Color(0xFFB91C1C))
        id.contains("duolingo") -> Triple("O'rganish", Color(0xFFDCFCE7), Color(0xFF15803D))
        id.contains("chrome") || id.contains("browser") -> Triple("Brauzer", Color(0xFFE0F2FE), Color(0xFF0369A1))
        else -> Triple("Ilova", Color(0xFFF1F5F9), Color(0xFF475569))
    }
}

@Composable
fun WeeklyStackedBarChart(
    daysOfWeek: List<String>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
) {
    val heights = listOf(0.45f, 0.70f, 0.60f, 0.65f, 0.85f, 0.55f, 0.50f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("8s", "6s", "4s", "0s").forEach { label ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFFF1F5F9))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                daysOfWeek.forEachIndexed { index, _ ->
                    val isSelected = index == selectedIndex
                    val h = heights[index]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectDay(index) },
                    ) {
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 22.dp else 16.dp)
                                .height((120 * h).dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.Transparent else Color(0xFFF1F5F9))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) YurakSky else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.35f)
                                        .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF93C5FD))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.20f)
                                        .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFFFCD34D))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.45f)
                                        .background(if (isSelected) Color(0xFFE11D48) else Color(0xFFFCA5A5))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectDay(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = YurakSky,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    ),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = YurakMuted,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}
