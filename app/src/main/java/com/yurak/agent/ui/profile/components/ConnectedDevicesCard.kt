package com.yurak.agent.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakEmerald
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate

@Composable
fun ConnectedDevicesCard(
    devices: List<DeviceItemDto>,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ulangan Qurilmalar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                )
                Text(
                    text = "${devices.size} ta qurilma",
                    fontSize = 12.sp,
                    color = YurakMuted,
                )
            }

            if (devices.isEmpty()) {
                Text(
                    text = "Hozircha ulangan qurilmalar mavjud emas.",
                    fontSize = 13.sp,
                    color = YurakMuted,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                devices.forEachIndexed { index, device ->
                    DeviceRowItem(device = device)
                    if (index < devices.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRowItem(device: DeviceItemDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = YurakSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Platforma: ${device.platform.uppercase()}",
                fontSize = 11.sp,
                color = YurakMuted,
            )
        }

        if (device.batteryLevel != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
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
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${device.batteryLevel}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
                    )
                }
            }
        }
    }
}

