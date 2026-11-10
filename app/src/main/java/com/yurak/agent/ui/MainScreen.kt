package com.yurak.agent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yurak.agent.AppContainer
import com.yurak.agent.data.*
import com.yurak.agent.tracking.SyncWorker
import com.yurak.agent.tracking.UsageAccess
import com.yurak.agent.ui.common.UpdateDialog
import com.yurak.agent.ui.dashboard.DashboardScreen
import com.yurak.agent.ui.datausage.DataUsageScreen
import com.yurak.agent.ui.navigation.YurakBottomBar
import com.yurak.agent.ui.navigation.YurakRoute
import com.yurak.agent.ui.profile.ProfileScreen
import com.yurak.agent.ui.usage.AppUsageScreen
import com.yurak.agent.update.AppUpdateChecker
import com.yurak.agent.update.UpdateInfo
import com.yurak.agent.usage.DayTrafficSummary
import com.yurak.agent.usage.DayUsageSummary
import com.yurak.agent.usage.LocalNetworkStatsManager
import com.yurak.agent.usage.LocalUsageStatsManager
import com.yurak.agent.util.TashkentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

/**
 * Main application host screen with URL-path styled navigation bar.
 * Routes: app/dashboard, app/app-usage, app/data-usage, app/profile.
 */
@Composable
fun MainScreen(
    container: AppContainer,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creds = remember { container.credentialStore.load() }
    val apiToken = creds?.apiToken ?: ""
    val userEmail = creds?.email ?: ""

    var currentRoute by remember { mutableStateOf<YurakRoute>(YurakRoute.Dashboard) }

    var dailyStats by remember { mutableStateOf<DailyStatsResponseDto?>(null) }
    var rangeStats by remember { mutableStateOf<RangeStatsResponseDto?>(null) }
    var devices by remember { mutableStateOf<List<DeviceItemDto>>(emptyList()) }

    var pendingCount by remember { mutableStateOf(0) }
    var hasUsageAccess by remember { mutableStateOf(UsageAccess.hasAccess(context)) }
    var syncing by remember { mutableStateOf(false) }

    // In-App update check state
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateBannerDismissed by remember { mutableStateOf(false) }

    val localUsageManager = remember { LocalUsageStatsManager(context) }
    var localTodaySummary by remember { mutableStateOf<DayUsageSummary?>(null) }

    val localTrafficManager = remember { LocalNetworkStatsManager(context) }
    var localTodayTraffic by remember { mutableStateOf<DayTrafficSummary?>(null) }

    val todayStr = remember { TashkentTime.today().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    val weekAgoStr = remember { TashkentTime.today().minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE) }

    // Fetch initial data
    val refreshData: () -> Unit = {
        scope.launch {
            withContext(Dispatchers.IO) {
                val summary = localUsageManager.getDayStats(TashkentTime.today())
                val traffic = localTrafficManager.getDayTraffic(TashkentTime.today())
                withContext(Dispatchers.Main) {
                    localTodaySummary = summary
                    localTodayTraffic = traffic
                }
            }
            if (apiToken.isNotEmpty()) {
                val dailyRes = container.apiClient.getDailyStats(apiToken, todayStr)
                if (dailyRes is ApiResult.Ok) {
                    dailyStats = dailyRes.value
                }
                val rangeRes = container.apiClient.getRangeStats(apiToken, weekAgoStr, todayStr)
                if (rangeRes is ApiResult.Ok) {
                    rangeStats = rangeRes.value
                }
                val devicesRes = container.apiClient.getDevices(apiToken)
                if (devicesRes is ApiResult.Ok) {
                    devices = devicesRes.value.devices
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
        // Check for updates in background at boot
        val check = AppUpdateChecker.checkUpdate("1.0.0")
        if (check != null) {
            updateInfo = check
        }
        while (true) {
            pendingCount = container.database.pendingEvents().count()
            hasUsageAccess = UsageAccess.hasAccess(context)
            delay(3000)
        }
    }

    val currentDevice = remember(devices, creds) {
        devices.find { it.id == creds?.deviceId } ?: devices.firstOrNull()
    }

    val onSyncNow: () -> Unit = {
        syncing = true
        SyncWorker.runOnce(context)
        scope.launch {
            for (i in 1..30) {
                delay(1500)
                val count = withContext(Dispatchers.IO) { container.database.pendingEvents().count() }
                pendingCount = count
                if (count == 0) break
            }
            refreshData()
            syncing = false
        }
    }

    val onManualCheckUpdate: () -> Unit = {
        showUpdateDialog = true
        isCheckingUpdate = true
        scope.launch {
            val check = AppUpdateChecker.checkUpdate("1.0.0")
            updateInfo = check
            isCheckingUpdate = false
        }
    }

    Scaffold(
        bottomBar = {
            YurakBottomBar(
                currentRoute = currentRoute,
                onNavigate = { currentRoute = it },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                is YurakRoute.Dashboard -> DashboardScreen(
                    stats = dailyStats,
                    localUsage = localTodaySummary,
                    localTraffic = localTodayTraffic,
                    device = currentDevice,
                    userEmail = userEmail,
                    hasUsageAccess = hasUsageAccess,
                    pendingCount = pendingCount,
                    syncing = syncing,
                    onOpenUsageSettings = { UsageAccess.openSettings(context) },
                    onSyncNow = onSyncNow,
                    onViewAppUsage = { currentRoute = YurakRoute.AppUsage },
                    onViewDataUsage = { currentRoute = YurakRoute.DataUsage },
                    updateInfo = if (updateBannerDismissed) null else updateInfo,
                    onDismissUpdate = { updateBannerDismissed = true },
                )

                is YurakRoute.AppUsage -> AppUsageScreen(
                    userEmail = userEmail,
                    device = currentDevice,
                    syncing = syncing,
                    onSyncNow = onSyncNow,
                )

                is YurakRoute.DataUsage -> DataUsageScreen(
                    dailyStats = dailyStats,
                    localTraffic = localTodayTraffic,
                    syncing = syncing,
                    onSyncNow = onSyncNow,
                )

                is YurakRoute.Profile -> ProfileScreen(
                    userEmail = userEmail,
                    devices = devices,
                    hasUsageAccess = hasUsageAccess,
                    pendingCount = pendingCount,
                    onOpenUsageSettings = { UsageAccess.openSettings(context) },
                    onSyncNow = onSyncNow,
                    onCheckUpdate = onManualCheckUpdate,
                    onLogout = {
                        SyncWorker.cancel(context)
                        container.credentialStore.clear()
                        container.trackingState.clear()
                        scope.launch { container.database.pendingEvents().deleteAll() }
                        onLogout()
                    },
                )
            }
        }
    }

    if (showUpdateDialog) {
        UpdateDialog(
            updateInfo = updateInfo,
            isChecking = isCheckingUpdate,
            onDismiss = { showUpdateDialog = false },
        )
    }
}
