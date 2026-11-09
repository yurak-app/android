package com.yurak.agent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.data.DailyAppStatDto
import com.yurak.agent.data.DailyStatsResponseDto
import com.yurak.agent.data.RangeStatsResponseDto
import com.yurak.agent.usage.DayTrafficSummary
import com.yurak.agent.usage.LocalAppTraffic

@Composable
fun DataUsageScreen(
    dailyStats: DailyStatsResponseDto?,
    rangeStats: RangeStatsResponseDto?,
    localTraffic: DayTrafficSummary? = null,
    syncing: Boolean,
    onSyncNow: () -> Unit,
) {
    val totalBytes = localTraffic?.totalBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalBytes
        ?: 0L
    val cellularBytes = localTraffic?.cellularBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalCellularBytes
        ?: 0L
    val wifiBytes = localTraffic?.wifiBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalWifiBytes
        ?: 0L
    val hotspotBytes = localTraffic?.hotspotBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalHotspotBytes
        ?: 0L

    val allApps = dailyStats?.apps ?: emptyList()
    val desktopBytes = allApps
        .filter { (it.platform ?: "") == "windows" }
        .sumOf { it.totalBytes }
    val extensionBytes = allApps
        .filter { (it.platform ?: "") == "extension" }
        .sumOf { it.totalBytes }

    val localApps = localTraffic?.apps ?: emptyList()
    val appsWithData = remember(allApps) {
        allApps
            .filter { it.totalBytes > 0 }
            .sortedByDescending { it.totalBytes }
    }

    val maxAppBytes = if (localApps.isNotEmpty()) {
        localApps.firstOrNull()?.totalBytes?.coerceAtLeast(1L) ?: 1L
    } else {
        appsWithData.firstOrNull()?.totalBytes?.coerceAtLeast(1L) ?: 1L
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Total Internet Usage Hero Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = YurakSlate),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "JAMI INTERNET TRAFIGI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                        ) {
                            Text(
                                text = "Bugun",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = FormatUtils.formatBytes(totalBytes),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 34.sp,
                        ),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (totalBytes > 0) {
                        val mobileRatio = (cellularBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        val wifiRatio = (wifiBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        val hotspotRatio = (hotspotBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF334155))
                        ) {
                            if (mobileRatio > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .weight(mobileRatio)
                                        .fillMaxHeight()
                                        .background(YurakPurple)
                                )
                            }
                            if (wifiRatio > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .weight(wifiRatio)
                                        .fillMaxHeight()
                                        .background(YurakSky)
                                )
                            }
                            if (hotspotRatio > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .weight(hotspotRatio)
                                        .fillMaxHeight()
                                        .background(Color(0xFFF59E0B))
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Dedicated Traffic Cards Grid
        item {
            Text(
                text = "Tarmoq turlari bo'yicha",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TrafficCard(
                        title = "Mobil Internet",
                        subtitle = "SIM-karta (Cellular)",
                        bytes = cellularBytes,
                        icon = Icons.Default.SignalCellularAlt,
                        badgeColor = Color(0xFFF3E8FF),
                        tintColor = YurakPurple,
                        modifier = Modifier.weight(1f),
                    )

                    TrafficCard(
                        title = "Wi-Fi Tarmoq",
                        subtitle = "Simsiz tarmoq",
                        bytes = wifiBytes,
                        icon = Icons.Default.Wifi,
                        badgeColor = Color(0xFFE0F2FE),
                        tintColor = YurakSky,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TrafficCard(
                        title = "Hotspot / Tether",
                        subtitle = "Boshqalarga ulashilgan",
                        bytes = hotspotBytes,
                        icon = Icons.Default.WifiTethering,
                        badgeColor = Color(0xFFFEF3C7),
                        tintColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                    )

                    TrafficCard(
                        title = "Desktop & Brauzer",
                        subtitle = "Kompyuter va Extension",
                        bytes = desktopBytes + extensionBytes,
                        icon = Icons.Default.Computer,
                        badgeColor = Color(0xFFDCFCE7),
                        tintColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 3. App Data Usage Breakdown List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ilovalar bo'yicha internet sarfi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
                val totalAppCount = if (localApps.isNotEmpty()) localApps.size else appsWithData.size
                Text(
                    text = "$totalAppCount ta ilova",
                    style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                )
            }
        }

        if (localApps.isNotEmpty()) {
            items(localApps) { app ->
                LocalDateDataUsageRow(app = app, maxBytes = maxAppBytes)
            }
        } else if (appsWithData.isNotEmpty()) {
            items(appsWithData) { app ->
                AppDataUsageRow(app = app, maxBytes = maxAppBytes)
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                    border = BorderStroke(1.dp, YurakBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Hozircha ilovalar trafigi qayd etilmadi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocalDateDataUsageRow(app: LocalAppTraffic, maxBytes: Long) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (app.iconBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(YurakSky),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YurakSlate,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = YurakMuted,
                            fontSize = 11.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = FormatUtils.formatBytes(app.totalBytes),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.05f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF1F5F9)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF0284C7)),
                )
            }
        }
    }
}

@Composable
fun TrafficCard(
    title: String,
    subtitle: String,
    bytes: Long,
    icon: ImageVector,
    badgeColor: Color,
    tintColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = FormatUtils.formatBytes(bytes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                ),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = YurakSlate,
                ),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = YurakMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun AppDataUsageRow(app: DailyAppStatDto, maxBytes: Long) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorForIdentifier(app.identifier)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (app.displayName ?: app.identifier).take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName ?: app.identifier.substringAfterLast("."),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = YurakSlate,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = when {
                            app.hotspotBytes > 0 -> "Hotspot: ${FormatUtils.formatBytes(app.hotspotBytes)}"
                            app.cellularBytes > 0 -> "Mobil: ${FormatUtils.formatBytes(app.cellularBytes)}"
                            app.wifiBytes > 0 -> "Wi-Fi: ${FormatUtils.formatBytes(app.wifiBytes)}"
                            else -> "Umumiy trafik"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                    )
                }

                Text(
                    text = FormatUtils.formatBytes(app.totalBytes),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.05f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (app.hotspotBytes > 0) Color(0xFFF59E0B) else YurakSky)
                )
            }
        }
    }
}
