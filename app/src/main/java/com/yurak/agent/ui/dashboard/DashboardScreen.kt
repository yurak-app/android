package com.yurak.agent.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yurak.agent.data.DailyStatsResponseDto
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.ui.YurakBackground
import com.yurak.agent.ui.common.PermissionBanner
import com.yurak.agent.ui.common.UpdateBanner
import com.yurak.agent.ui.dashboard.components.DashboardHeader
import com.yurak.agent.ui.dashboard.components.DashboardKpiGrid
import com.yurak.agent.ui.dashboard.components.SyncStatusCard
import com.yurak.agent.ui.dashboard.components.TopAppsSnapshotCard
import com.yurak.agent.update.UpdateInfo
import com.yurak.agent.usage.DayTrafficSummary
import com.yurak.agent.usage.DayUsageSummary

@Composable
fun DashboardScreen(
    stats: DailyStatsResponseDto?,
    localUsage: DayUsageSummary? = null,
    localTraffic: DayTrafficSummary? = null,
    device: DeviceItemDto?,
    userEmail: String,
    hasUsageAccess: Boolean,
    pendingCount: Int,
    syncing: Boolean,
    onOpenUsageSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onViewAppUsage: () -> Unit,
    onViewDataUsage: () -> Unit,
    updateInfo: UpdateInfo? = null,
    onDismissUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val totalScreenSeconds: Long = localUsage?.totalSeconds?.takeIf { it > 0 }
        ?: stats?.screenSeconds?.toLong()
        ?: stats?.apps?.sumOf { it.totalSeconds.toLong() }
        ?: 0L
    val totalBytes = localTraffic?.totalBytes?.takeIf { it > 0 } ?: (stats?.totalBytes ?: 0L)
    val switchCount = stats?.switchCount ?: 0
    val chargingSeconds = 0L

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Device Info Header Card
        item {
            DashboardHeader(
                userEmail = userEmail,
                device = device,
                pendingCount = pendingCount,
                syncing = syncing,
                onSyncNow = onSyncNow
            )
        }

        // 2. New Version Update Banner (if available)
        if (updateInfo?.hasUpdate == true) {
            item {
                UpdateBanner(
                    updateInfo = updateInfo,
                    onDismiss = onDismissUpdate
                )
            }
        }

        // 3. Usage Access Alert (if missing)
        if (!hasUsageAccess) {
            item {
                PermissionBanner(
                    onGrantClick = onOpenUsageSettings
                )
            }
        }

        // 4. Primary KPI Cards Grid (Clickable to jump to relevant pages)
        item {
            DashboardKpiGrid(
                totalScreenSeconds = totalScreenSeconds,
                totalBytes = totalBytes,
                switchCount = switchCount,
                chargingSeconds = chargingSeconds,
                onViewAppUsage = onViewAppUsage,
                onViewDataUsage = onViewDataUsage,
            )
        }

        // 5. Top Apps Snapshot of Today
        item {
            TopAppsSnapshotCard(
                localApps = localUsage?.apps ?: emptyList(),
                remoteApps = stats?.apps ?: emptyList(),
                onViewAll = onViewAppUsage
            )
        }

        // 6. Offline Events Queue and Sync Action Card
        item {
            SyncStatusCard(
                pendingCount = pendingCount,
                syncing = syncing,
                onSyncNow = onSyncNow
            )
        }
    }
}
