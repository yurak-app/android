package com.yurak.agent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurak.agent.data.DeviceItemDto

@Composable
fun ProfileScreen(
    userEmail: String,
    devices: List<DeviceItemDto>,
    backendUrl: String,
    hasUsageAccess: Boolean,
    pendingCount: Int,
    onOpenUsageSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. User Profile Header Card
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(YurakCrimson),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = userEmail.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userEmail.substringBefore("@"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = YurakSlate,
                        ),
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFE4E6),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = YurakCrimson,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yurak Pro Foydalanuvchi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF9F1239),
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                        }
                    }
                }
            }
        }

        // 2. Connected Devices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ulangan Qurilmalar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    ),
                )
                Text(
                    text = "${devices.size} ta qurilma",
                    style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                )
            }
        }

        if (devices.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                    border = BorderStroke(1.dp, YurakBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Qurilmalar ro'yxati yuklanmoqda...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
                        )
                    }
                }
            }
        } else {
            items(devices) { dev ->
                DeviceCardItem(device = dev)
            }
        }

        // 3. System & Permissions Status
        item {
            Text(
                text = "Tizim va Xavfsizlik",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                border = BorderStroke(1.dp, YurakBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUsageAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasUsageAccess) YurakEmerald else YurakCrimson,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Usage Access",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = YurakSlate,
                                    ),
                                )
                                Text(
                                    if (hasUsageAccess) "Ruxsat faol" else "Ruxsat berilmagan",
                                    style = MaterialTheme.typography.labelSmall.copy(color = YurakMuted),
                                )
                            }
                        }

                        if (!hasUsageAccess) {
                            TextButton(onClick = onOpenUsageSettings) {
                                Text("Yoqish", color = YurakCrimson, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = YurakSky, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Server URL",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = YurakSlate,
                                    ),
                                )
                                Text(backendUrl, style = MaterialTheme.typography.labelSmall.copy(color = YurakMuted))
                            }
                        }
                    }
                }
            }
        }

        // 4. Log out action
        item {
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YurakCrimson),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hisobdan chiqish (Log out)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeviceCardItem(device: DeviceItemDto) {
    Card(
        shape = RoundedCornerShape(14.dp),
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
            val (icon, iconBg, tint) = when (device.platform.lowercase()) {
                "android" -> Triple(Icons.Default.Smartphone, Color(0xFFE0E7FF), Color(0xFF4338CA))
                "windows", "desktop" -> Triple(Icons.Default.Computer, Color(0xFFDCFCE7), Color(0xFF15803D))
                "extension" -> Triple(Icons.Default.Extension, Color(0xFFFEF3C7), Color(0xFFD97706))
                else -> Triple(Icons.Default.Devices, Color(0xFFF1F5F9), YurakSlate)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = YurakSlate,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Platforma: ${device.platform.uppercase()}",
                    style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
                )
            }

            if (device.batteryLevel != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            imageVector = if (device.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = null,
                            tint = YurakEmerald,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${device.batteryLevel}%",
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
}
