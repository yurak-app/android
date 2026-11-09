package com.yurak.agent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.usage.DayTrafficSummary
import com.yurak.agent.usage.DayUsageSummary
import com.yurak.agent.usage.RealAppUsage
import com.yurak.agent.util.TashkentTime

@Composable
fun DashboardScreen(
    stats: DailyStatsResponseDto?,
    localUsage: DayUsageSummary? = null,
    localTraffic: DayTrafficSummary? = null,
    device: DeviceItemDto?,
    userEmail: String,
    hasUsageAccess: Boolean,
    pendingCount: Int,
    syncing: Boolean,
    onOpenUsageSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onViewAppUsage: () -> Unit,
    onViewDataUsage: () -> Unit,
) {
    val totalScreenSeconds: Long = localUsage?.totalSeconds?.takeIf { it > 0 }
        ?: stats?.screenSeconds?.toLong()
        ?: stats?.apps?.sumOf { it.totalSeconds.toLong() }
        ?: 0L
    val totalBytes = localTraffic?.totalBytes?.takeIf { it > 0 } ?: (stats?.totalBytes ?: 0L)
    val hotspotBytes = localTraffic?.hotspotBytes?.takeIf { it > 0 } ?: (stats?.totalHotspotBytes ?: 0L)
    val cellularBytes = localTraffic?.cellularBytes?.takeIf { it > 0 } ?: (stats?.totalCellularBytes ?: 0L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF4338CA),
                            modifier = Modifier.size(26.dp),
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
                            text = device?.name ?: "Xiaomi Qurilma",
                            style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Battery Pill
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
                                modifier = Modifier.size(16.dp),
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

        // 2. Usage Access Alert (if missing)
        if (!hasUsageAccess) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = YurakCrimson)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ilovalarni kuzatish ruxsati kerak",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YurakCrimson
                                ),
                            )
                        }
                        Text(
                            "Yurak qaysi ilovalarda qancha vaqt o'tkazilganini hisoblashi uchun ushbu ruxsatni yoqing.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF881337)),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                        Button(
                            onClick = onOpenUsageSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = YurakCrimson),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Ruxsatni yoqish", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Quick Stats Grid (2x2 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        title = "Ekran vaqti",
                        value = FormatUtils.formatDuration(totalScreenSeconds),
                        subtitle = "Bugungi jami faollik",
                        icon = Icons.Default.Timer,
                        iconBg = Color(0xFFFFE4E6),
                        iconTint = YurakCrimson,
                        modifier = Modifier.weight(1f).clickable { onViewAppUsage() },
                    )
                    MetricCard(
                        title = "Jami Internet",
                        value = FormatUtils.formatBytes(totalBytes),
                        subtitle = "Barcha tarmoq sarfi",
                        icon = Icons.Default.DataUsage,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = YurakSky,
                        modifier = Modifier.weight(1f).clickable { onViewDataUsage() },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        title = "Tarqatilgan Hotspot",
                        value = FormatUtils.formatBytes(hotspotBytes),
                        subtitle = "Ulangan qurilmalarga",
                        icon = Icons.Default.WifiTethering,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706),
                        modifier = Modifier.weight(1f).clickable { onViewDataUsage() },
                    )
                    MetricCard(
                        title = "Mobil Internet",
                        value = FormatUtils.formatBytes(cellularBytes),
                        subtitle = "SIM-karta trafigi",
                        icon = Icons.Default.SignalCellularAlt,
                        iconBg = Color(0xFFF3E8FF),
                        iconTint = YurakPurple,
                        modifier = Modifier.weight(1f).clickable { onViewDataUsage() },
                    )
                }
            }
        }

        // 4. Top Apps Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Bugungi eng faol ilovalar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
                TextButton(onClick = onViewAppUsage) {
                    Text("Barchasi", color = YurakCrimson, fontWeight = FontWeight.Bold)
                }
            }
        }

        val localTopApps = localUsage?.apps?.take(5) ?: emptyList()
        val remoteTopApps = stats?.apps?.take(5) ?: emptyList()

        if (localTopApps.isNotEmpty()) {
            val maxSeconds = localTopApps.firstOrNull()?.totalSeconds?.coerceAtLeast(1) ?: 1L
            items(localTopApps) { app ->
                LocalAppRowItem(app = app, maxSeconds = maxSeconds)
            }
        } else if (remoteTopApps.isNotEmpty()) {
            val maxSeconds = remoteTopApps.firstOrNull()?.totalSeconds?.coerceAtLeast(1) ?: 1
            items(remoteTopApps) { app ->
                AppUsageRowItem(
                    app = app,
                    maxSeconds = maxSeconds,
                )
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
                            "Bugungi hodisalar hali yuborilmadi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    }
                }
            }
        }

        // 5. Sync Now Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                border = BorderStroke(1.dp, YurakBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (pendingCount > 0) "$pendingCount ta ma'lumot navbatda" else "Server bilan to'liq tenglashtirilgan",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = YurakSlate,
                            ),
                        )
                        Text(
                            text = if (pendingCount > 0) "Serverga yuborish uchun bosing" else "Barcha o'zgarishlar saqlangan",
                            style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                        )
                    }

                    Button(
                        onClick = onSyncNow,
                        enabled = !syncing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YurakSlate),
                    ) {
                        if (syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Yuborish", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
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
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                ),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = YurakSlate,
                ),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = YurakMuted),
            )
        }
    }
}

@Composable
fun AppUsageRowItem(app: DailyAppStatDto, maxSeconds: Int) {
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = YurakSlate,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (app.totalBytes > 0) FormatUtils.formatBytes(app.totalBytes) else "${app.sessionCount} ta seans",
                        style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                    )
                }

                Text(
                    text = FormatUtils.formatDuration(app.totalSeconds),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0.05f, 1f)
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
                        .background(YurakCrimson)
                )
            }
        }
    }
}

@Composable
fun LocalAppRowItem(app: RealAppUsage, maxSeconds: Long) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (app.iconBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(YurakCrimson),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            ),
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
                        text = app.categoryBadge.label,
                        style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                    )
                }

                Text(
                    text = TashkentTime.formatDuration(app.totalSeconds),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0.05f, 1f)
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
                        .background(YurakCrimson),
                )
            }
        }
    }
}

fun colorForIdentifier(identifier: String): Color {
    val hash = identifier.hashCode()
    val colors = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFFDC2626), // Red
        Color(0xFF059669), // Green
        Color(0xFFD97706), // Amber
        Color(0xFF7C3AED), // Purple
        Color(0xFF0891B2), // Cyan
        Color(0xFFDB2777), // Pink
    )
    return colors[kotlin.math.abs(hash) % colors.size]
}
