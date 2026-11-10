package com.yurak.agent.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate

@Composable
fun DashboardHeader(
    userEmail: String,
    device: DeviceItemDto?,
    pendingCount: Int,
    syncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
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
                        text = userEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDCFCE7))
                            .border(BorderStroke(1.dp, Color(0xFFBBF7D0)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Faol",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                        )
                    }
                }

                Text(
                    text = device?.name ?: "Android Qurilma",
                    fontSize = 12.sp,
                    color = YurakMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            IconButton(
                onClick = onSyncNow,
                enabled = !syncing,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (syncing) Color(0xFFF1F5F9) else Color(0xFFEFF6FF)),
            ) {
                if (syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF2563EB),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sinxronlash",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
