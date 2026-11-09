package com.yurak.agent.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.usage.DayUsageSummary
import kotlin.math.max

@Composable
fun WeeklyUsageBarChart(
    days: List<DayUsageSummary>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Dynamic max scale: at least 8 hours (28800s) or higher if a day exceeded 8 hours
    val maxSecondsInWeek = days.filter { !it.isFuture }.maxOfOrNull { it.totalSeconds } ?: 0L
    val maxScaleSeconds = max(28800L, maxSecondsInWeek)
    val maxHours = (maxScaleSeconds / 3600).toInt()

    val yLabels = listOf(
        "${maxHours}s",
        "${(maxHours * 0.75).toInt()}s",
        "${(maxHours * 0.50).toInt()}s",
        "0s"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background horizontal dashed guidelines & labels
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                yLabels.forEach { label ->
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
                                fontSize = 10.sp,
                            ),
                        )
                    }
                }
            }

            // 7 Columns for the week
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = index == selectedIndex
                    val isFuture = day.isFuture
                    val fraction = if (isFuture) 0f else (day.totalSeconds.toFloat() / maxScaleSeconds).coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isFuture) Modifier.clickable { onSelectDay(index) }
                                else Modifier
                            ),
                    ) {
                        // Bar Container
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 22.dp else 16.dp)
                                .height(120.dp),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (!isFuture && day.totalSeconds > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFE2E8F0) else Color(0xFFF1F5F9))
                                        .then(
                                            if (isSelected) Modifier.border(
                                                width = 2.dp,
                                                color = Color(0xFF0284C7),
                                                shape = RoundedCornerShape(8.dp),
                                            ) else Modifier
                                        ),
                                    contentAlignment = Alignment.BottomCenter,
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        // Blue top segment (35%)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.35f)
                                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF93C5FD))
                                        )
                                        // Yellow middle segment (20%)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.20f)
                                                .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFFFCD34D))
                                        )
                                        // Red/Coral bottom segment (45%)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.45f)
                                                .background(if (isSelected) Color(0xFFE11D48) else Color(0xFFFDA4AF))
                                        )
                                    }
                                }
                            } else if (isFuture) {
                                // Future day: muted placeholder dot, NO fake bar!
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFCBD5E1))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day of week label
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.dayOfWeekLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                    ),
                                )
                            }
                        } else {
                            Text(
                                text = day.dayOfWeekLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        day.isToday -> Color(0xFF0284C7)
                                        isFuture -> Color(0xFFCBD5E1)
                                        else -> YurakSlate
                                    },
                                    fontSize = 11.sp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

