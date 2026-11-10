package com.yurak.agent.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurak.agent.ui.FormatUtils
import com.yurak.agent.ui.YurakAmber
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakEmerald
import com.yurak.agent.ui.YurakSky
import com.yurak.agent.ui.common.StatCard

@Composable
fun DashboardKpiGrid(
    totalScreenSeconds: Long,
    totalBytes: Long,
    switchCount: Int,
    chargingSeconds: Long,
    modifier: Modifier = Modifier,
    onViewAppUsage: (() -> Unit)? = null,
    onViewDataUsage: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Faol ekran vaqti",
                value = FormatUtils.formatDuration(totalScreenSeconds),
                subtitle = "Bugungi sof faollik",
                icon = Icons.Default.Timer,
                iconTint = YurakCrimson,
                iconBg = Color(0xFFFFE4E6),
                modifier = Modifier.weight(1f),
                onClick = onViewAppUsage,
            )

            StatCard(
                title = "Tarmoq sarfi",
                value = FormatUtils.formatBytes(totalBytes),
                subtitle = "Mobil + Wi-Fi",
                icon = Icons.Default.Language,
                iconTint = YurakSky,
                iconBg = Color(0xFFE0F2FE),
                modifier = Modifier.weight(1f),
                onClick = onViewDataUsage,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Ilova o'tishlari",
                value = "$switchCount ta",
                subtitle = "Oynalar almashinuvi",
                icon = Icons.Default.SwapHoriz,
                iconTint = YurakEmerald,
                iconBg = Color(0xFFDCFCE7),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Zaryad olayotganda",
                value = FormatUtils.formatDuration(chargingSeconds),
                subtitle = "Quvvatda ekran vaqti",
                icon = Icons.Default.Bolt,
                iconTint = YurakAmber,
                iconBg = Color(0xFFFEF3C7),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
