package com.yurak.agent.ui.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.util.TashkentTime
import java.time.LocalDate

@Composable
fun WeekRangeStepper(
    startDate: LocalDate,
    endDate: LocalDate,
    selectedDate: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    canGoNext: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledIconButton(
            onClick = onPreviousWeek,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
            ),
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Oldingi hafta",
                modifier = Modifier.size(18.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = TashkentTime.formatWeekRange(startDate, endDate),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = YurakSlate,
                ),
            )
            Text(
                text = "Tanlangan: ${TashkentTime.formatSelectedDay(selectedDate)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0284C7),
                ),
            )
        }

        FilledIconButton(
            onClick = onNextWeek,
            enabled = canGoNext,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (canGoNext) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                contentColor = if (canGoNext) Color.White else Color(0xFF94A3B8),
            ),
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Keyingi hafta",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

