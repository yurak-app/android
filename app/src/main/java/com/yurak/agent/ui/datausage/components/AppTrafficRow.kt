package com.yurak.agent.ui.datausage.components

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
import com.yurak.agent.ui.YurakEmerald
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSky
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.usage.LocalAppTraffic

@Composable
fun LocalAppTrafficRow(app: LocalAppTraffic, maxBytes: Long) {
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
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = YurakSlate,
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
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Mobil: ${FormatUtils.formatBytes(app.mobileBytes)} · Wi-Fi: ${FormatUtils.formatBytes(app.wifiBytes)}",
                        fontSize = 11.sp,
                        color = YurakMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = FormatUtils.formatBytes(app.totalBytes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF1F5F9)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(YurakSky),
                )
            }
        }
    }
}

@Composable
fun RemoteAppTrafficRow(app: DailyAppStatDto, maxBytes: Long) {
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
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (app.displayName ?: app.identifier).take(1).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
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
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = app.identifier,
                        fontSize = 11.sp,
                        color = YurakMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = FormatUtils.formatBytes(app.totalBytes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (app.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFF1F5F9)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(YurakEmerald),
                )
            }
        }
    }
}
