package com.yurak.agent.ui.datausage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yurak.agent.data.DailyStatsResponseDto
import com.yurak.agent.data.RangeStatsResponseDto
import com.yurak.agent.ui.YurakBackground
import com.yurak.agent.ui.common.SectionHeader
import com.yurak.agent.ui.datausage.components.LocalAppTrafficRow
import com.yurak.agent.ui.datausage.components.RemoteAppTrafficRow
import com.yurak.agent.ui.datausage.components.TotalTrafficHeroCard
import com.yurak.agent.ui.datausage.components.TrafficSourcesGrid
import com.yurak.agent.usage.DayTrafficSummary

@Composable
fun DataUsageScreen(
    dailyStats: DailyStatsResponseDto?,
    localTraffic: DayTrafficSummary? = null,
    syncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalBytes = localTraffic?.totalBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalBytes
        ?: 0L
    val cellularBytes = localTraffic?.cellularBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalCellularBytes
        ?: 0L
    val wifiBytes = localTraffic?.wifiBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalWifiBytes
        ?: 0L
    val hotspotBytes = localTraffic?.hotspotBytes?.takeIf { it > 0L }
        ?: dailyStats?.totalHotspotBytes
        ?: 0L

    val allApps = dailyStats?.apps ?: emptyList()
    val localApps = localTraffic?.apps ?: emptyList()

    val appsWithData = remember(allApps) {
        allApps.filter { it.totalBytes > 0 }.sortedByDescending { it.totalBytes }
    }

    val maxAppBytes = if (localApps.isNotEmpty()) {
        localApps.firstOrNull()?.totalBytes?.coerceAtLeast(1L) ?: 1L
    } else {
        appsWithData.firstOrNull()?.totalBytes?.coerceAtLeast(1L) ?: 1L
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Total Internet Usage Hero Card
        item {
            TotalTrafficHeroCard(
                totalBytes = totalBytes,
                syncing = syncing,
                onSyncNow = onSyncNow
            )
        }

        // 2. Traffic Sources Breakdown (Cellular, Wi-Fi, Hotspot)
        item {
            TrafficSourcesGrid(
                cellularBytes = cellularBytes,
                wifiBytes = wifiBytes,
                hotspotBytes = hotspotBytes
            )
        }

        // 3. Section Title
        item {
            SectionHeader(
                title = "Ilovalar tarmoq sarfi",
                subtitle = "Bugungi internet sarflari ro'yxati"
            )
        }

        // 4. App List
        if (localApps.isNotEmpty()) {
            items(localApps) { app ->
                LocalAppTrafficRow(app = app, maxBytes = maxAppBytes)
            }
        } else {
            items(appsWithData) { app ->
                RemoteAppTrafficRow(app = app, maxBytes = maxAppBytes)
            }
        }
    }
}

