package com.yurak.agent.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yurak.agent.data.DailyAppStatDto
import com.yurak.agent.ui.FormatUtils
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.usage.RealAppUsage
import com.yurak.agent.util.TashkentTime

@Composable
fun TopAppsSnapshotCard(
    localApps: List<RealAppUsage>,
    remoteApps: List<DailyAppStatDto>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bugungi eng faol ilovalar",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = YurakSlate
            )
            TextButton(
                onClick = onViewAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Barchasi →",
                    color = YurakCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (localApps.isNotEmpty()) {
            val maxSeconds = localApps.firstOrNull()?.totalSeconds?.coerceAtLeast(1) ?: 1L
            localApps.take(5).forEach { app ->
                LocalAppItemRow(app = app, maxSeconds = maxSeconds)
            }
        } else if (remoteApps.isNotEmpty()) {
            val maxSeconds = remoteApps.firstOrNull()?.totalSeconds?.coerceAtLeast(1) ?: 1
            remoteApps.take(5).forEach { app ->
                RemoteAppItemRow(app = app, maxSeconds = maxSeconds)
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = YurakCardBg),
                border = BorderStroke(1.dp, YurakBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bugungi faollik ma'lumotlari mavjud emas",
                        fontSize = 13.sp,
                        color = YurakMuted
                    )
                }
            }
        }
    }
}

@Composable
fun LocalAppItemRow(app: RealAppUsage, maxSeconds: Long) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(YurakCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.categoryBadge.label,
                        fontSize = 11.sp,
                        color = YurakMuted
                    )
                }

                Text(
                    text = TashkentTime.formatDuration(app.totalSeconds),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
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
fun RemoteAppItemRow(app: DailyAppStatDto, maxSeconds: Int) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (app.displayName ?: app.identifier).take(1).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName ?: app.identifier.substringAfterLast("."),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (app.totalBytes > 0) FormatUtils.formatBytes(app.totalBytes) else "${app.sessionCount} ta seans",
                        fontSize = 11.sp,
                        color = YurakMuted
                    )
                }

                Text(
                    text = FormatUtils.formatDuration(app.totalSeconds),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalSeconds.toFloat() / maxSeconds.toFloat()).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
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

