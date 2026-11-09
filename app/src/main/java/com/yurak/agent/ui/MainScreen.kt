package com.yurak.agent.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yurak.agent.AppContainer
import com.yurak.agent.data.*
import com.yurak.agent.ui.usage.AppUsageScreen
import com.yurak.agent.usage.DayTrafficSummary
import com.yurak.agent.usage.DayUsageSummary
import com.yurak.agent.usage.LocalNetworkStatsManager
import com.yurak.agent.usage.LocalUsageStatsManager
import com.yurak.agent.util.TashkentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.yurak.agent.tracking.SyncWorker
import com.yurak.agent.tracking.UsageAccess
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MainTab {
    DASHBOARD, APP_USAGE, DATA_USAGE, PROFILE
}

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
    val backendUrl = remember { container.config.backendUrl }

    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    var dailyStats by remember { mutableStateOf<DailyStatsResponseDto?>(null) }
    var rangeStats by remember { mutableStateOf<RangeStatsResponseDto?>(null) }
    var devices by remember { mutableStateOf<List<DeviceItemDto>>(emptyList()) }

    var pendingCount by remember { mutableStateOf(0) }
    var hasUsageAccess by remember { mutableStateOf(UsageAccess.hasAccess(context)) }
    var syncing by remember { mutableStateOf(false) }

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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(BorderStroke(1.dp, Color(0xFFE2E8F0))),
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.DASHBOARD,
                    onClick = { selectedTab = MainTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = YurakCrimson,
                        selectedTextColor = YurakCrimson,
                        unselectedIconColor = YurakMuted,
                        unselectedTextColor = YurakMuted,
                        indicatorColor = Color(0xFFFFE4E6),
                    ),
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.APP_USAGE,
                    onClick = { selectedTab = MainTab.APP_USAGE },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "App Usage") },
                    label = { Text("App Usage") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = YurakSky,
                        selectedTextColor = YurakSky,
                        unselectedIconColor = YurakMuted,
                        unselectedTextColor = YurakMuted,
                        indicatorColor = Color(0xFFE0F2FE),
                    ),
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.DATA_USAGE,
                    onClick = { selectedTab = MainTab.DATA_USAGE },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Data Usage") },
                    label = { Text("Data Usage") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD97706),
                        selectedTextColor = Color(0xFFD97706),
                        unselectedIconColor = YurakMuted,
                        unselectedTextColor = YurakMuted,
                        indicatorColor = Color(0xFFFEF3C7),
                    ),
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.PROFILE,
                    onClick = { selectedTab = MainTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = YurakSlate,
                        selectedTextColor = YurakSlate,
                        unselectedIconColor = YurakMuted,
                        unselectedTextColor = YurakMuted,
                        indicatorColor = Color(0xFFE2E8F0),
                    ),
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.DASHBOARD -> DashboardScreen(
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
                    onViewAppUsage = { selectedTab = MainTab.APP_USAGE },
                    onViewDataUsage = { selectedTab = MainTab.DATA_USAGE },
                )

                MainTab.APP_USAGE -> AppUsageScreen(
                    userEmail = userEmail,
                    device = currentDevice,
                    syncing = syncing,
                    onSyncNow = onSyncNow,
                )

                MainTab.DATA_USAGE -> DataUsageScreen(
                    dailyStats = dailyStats,
                    rangeStats = rangeStats,
                    localTraffic = localTodayTraffic,
                    syncing = syncing,
                    onSyncNow = onSyncNow,
                )

                MainTab.PROFILE -> ProfileScreen(
                    userEmail = userEmail,
                    devices = devices,
                    backendUrl = backendUrl,
                    hasUsageAccess = hasUsageAccess,
                    pendingCount = pendingCount,
                    onOpenUsageSettings = { UsageAccess.openSettings(context) },
                    onSyncNow = onSyncNow,
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
}
