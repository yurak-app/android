package com.yurak.agent.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakEmerald
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSky
import com.yurak.agent.ui.YurakSlate

@Composable
fun AgentStatusCard(
    hasUsageAccess: Boolean,
    pendingCount: Int,
    onOpenUsageSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onCheckUpdate: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Tizim va Xavfsizlik",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = YurakSlate,
            )

            // 1. Usage Access row
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
                            text = "Usage Access",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = YurakSlate,
                        )
                        Text(
                            text = if (hasUsageAccess) "Ruxsat faol" else "Ruxsat berilmagan",
                            fontSize = 11.sp,
                            color = YurakMuted,
                        )
                    }
                }

                if (!hasUsageAccess) {
                    TextButton(onClick = onOpenUsageSettings) {
                        Text(
                            text = "Yoqish",
                            color = YurakCrimson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // 2. Pending Queue & Sync row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = YurakEmerald,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sinxronizatsiya",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = YurakSlate,
                        )
                        Text(
                            text = if (pendingCount > 0) "$pendingCount ta hodisa navbatda" else "Barcha hodisalar yuborilgan",
                            fontSize = 11.sp,
                            color = YurakMuted,
                        )
                    }
                }

                TextButton(onClick = onSyncNow) {
                    Text(
                        text = "Sinxronlash",
                        color = YurakCrimson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // 3. App Version & Update Check row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = YurakSky,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ilova versiyasi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = YurakSlate,
                        )
                        Text(
                            text = "v1.0.0 (Rasmiy reliz)",
                            fontSize = 11.sp,
                            color = YurakMuted,
                        )
                    }
                }

                TextButton(onClick = onCheckUpdate) {
                    Text(
                        text = "Tekshirish",
                        color = YurakCrimson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}
