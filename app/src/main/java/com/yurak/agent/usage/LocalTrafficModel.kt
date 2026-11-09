package com.yurak.agent.usage

import androidx.compose.ui.graphics.ImageBitmap
import java.time.LocalDate

data class LocalAppTraffic(
    val packageName: String,
    val appName: String,
    val totalBytes: Long,
    val mobileBytes: Long,
    val wifiBytes: Long,
    val hotspotBytes: Long = 0L,
    val iconBitmap: ImageBitmap? = null,
)

data class DayTrafficSummary(
    val date: LocalDate,
    val totalBytes: Long,
    val cellularBytes: Long,
    val wifiBytes: Long,
    val hotspotBytes: Long,
    val apps: List<LocalAppTraffic>,
)

