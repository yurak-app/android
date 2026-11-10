package com.yurak.agent.ui.datausage.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurak.agent.ui.FormatUtils
import com.yurak.agent.ui.YurakAmber
import com.yurak.agent.ui.YurakPurple
import com.yurak.agent.ui.YurakSky
import com.yurak.agent.ui.common.StatCard

@Composable
fun TrafficSourcesGrid(
    cellularBytes: Long,
    wifiBytes: Long,
    hotspotBytes: Long,
    modifier: Modifier = Modifier,
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
                title = "Mobil Internet",
                value = FormatUtils.formatBytes(cellularBytes),
                subtitle = "SIM-karta (4G/5G)",
                icon = Icons.Default.SignalCellularAlt,
                iconTint = YurakPurple,
                iconBg = Color(0xFFF3E8FF),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Wi-Fi Tarmoq",
                value = FormatUtils.formatBytes(wifiBytes),
                subtitle = "Simsiz internet",
                icon = Icons.Default.Wifi,
                iconTint = YurakSky,
                iconBg = Color(0xFFE0F2FE),
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = "Tarqatilgan Hotspot",
            value = FormatUtils.formatBytes(hotspotBytes),
            subtitle = "Boshqa qurilmalarga ulashilgan trafik",
            icon = Icons.Default.WifiTethering,
            iconTint = YurakAmber,
            iconBg = Color(0xFFFEF3C7),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

