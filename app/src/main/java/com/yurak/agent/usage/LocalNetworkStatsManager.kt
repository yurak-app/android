package com.yurak.agent.usage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Process
import com.yurak.agent.util.DrawableUtils
import com.yurak.agent.util.TashkentTime
import java.time.LocalDate
import kotlin.math.max

class LocalNetworkStatsManager(private val context: Context) {
    private val networkStatsManager: NetworkStatsManager? by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
    }
    private val packageManager: PackageManager = context.packageManager

    private class UidAccumulator(
        var mobile: Long = 0L,
        var wifi: Long = 0L,
        var hotspot: Long = 0L,
    )

    fun getDayTraffic(date: LocalDate): DayTrafficSummary {
        val nsm = networkStatsManager ?: return DayTrafficSummary(date, 0L, 0L, 0L, 0L, emptyList())

        val isToday = date.isEqual(TashkentTime.today())
        val startMs = TashkentTime.startOfDayMs(date)
        val endMs = if (isToday) System.currentTimeMillis() else TashkentTime.endOfDayMs(date)

        // 1. Device-level totals directly from Android OS kernel metrics
        var deviceMobileBytes = 0L
        try {
            val bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startMs, endMs)
            deviceMobileBytes = max(0L, bucket.rxBytes + bucket.txBytes)
        } catch (_: Exception) {}

        var deviceWifiBytes = 0L
        try {
            val bucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startMs, endMs)
            deviceWifiBytes = max(0L, bucket.rxBytes + bucket.txBytes)
        } catch (_: Exception) {}

        // 2. Per-UID breakdown
        val uidMap = mutableMapOf<Int, UidAccumulator>()

        // Mobile per-app
        try {
            val stats = nsm.querySummary(ConnectivityManager.TYPE_MOBILE, null, startMs, endMs)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val bytes = max(0L, bucket.rxBytes + bucket.txBytes)
                if (bytes > 0L) {
                    val acc = uidMap.getOrPut(bucket.uid) { UidAccumulator() }
                    if (bucket.uid == -5 || bucket.uid == NetworkStats.Bucket.UID_TETHERING) {
                        acc.hotspot += bytes
                    } else {
                        acc.mobile += bytes
                    }
                }
            }
            stats.close()
        } catch (_: Exception) {}

        // Wi-Fi per-app (subscriberId MUST be null)
        try {
            val stats = nsm.querySummary(ConnectivityManager.TYPE_WIFI, null, startMs, endMs)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val bytes = max(0L, bucket.rxBytes + bucket.txBytes)
                if (bytes > 0L) {
                    val acc = uidMap.getOrPut(bucket.uid) { UidAccumulator() }
                    if (bucket.uid == -5 || bucket.uid == NetworkStats.Bucket.UID_TETHERING) {
                        acc.hotspot += bytes
                    } else {
                        acc.wifi += bytes
                    }
                }
            }
            stats.close()
        } catch (_: Exception) {}

        var totalHotspot = 0L
        val appList = mutableListOf<LocalAppTraffic>()

        for ((uid, acc) in uidMap) {
            val appTotal = acc.mobile + acc.wifi + acc.hotspot
            if (appTotal <= 0L) continue

            totalHotspot += acc.hotspot

            val (pkgName, appName, iconBitmap) = resolveUid(uid)
            appList.add(
                LocalAppTraffic(
                    packageName = pkgName,
                    appName = appName,
                    totalBytes = appTotal,
                    mobileBytes = acc.mobile,
                    wifiBytes = acc.wifi,
                    hotspotBytes = acc.hotspot,
                    iconBitmap = iconBitmap,
                )
            )
        }

        appList.sortByDescending { it.totalBytes }

        // Use maximum of device summary and sum of UIDs
        val finalMobile = max(deviceMobileBytes, appList.sumOf { it.mobileBytes })
        val finalWifi = max(deviceWifiBytes, appList.sumOf { it.wifiBytes })
        val finalTotal = finalMobile + finalWifi

        return DayTrafficSummary(
            date = date,
            totalBytes = finalTotal,
            cellularBytes = finalMobile,
            wifiBytes = finalWifi,
            hotspotBytes = totalHotspot,
            apps = appList,
        )
    }

    private data class UidInfo(
        val pkgName: String,
        val appName: String,
        val icon: androidx.compose.ui.graphics.ImageBitmap?,
    )

    private fun resolveUid(uid: Int): UidInfo {
        if (uid == -5 || uid == NetworkStats.Bucket.UID_TETHERING) {
            return UidInfo("system:hotspot", "Hotspot / Tether", null)
        }
        if (uid == Process.SYSTEM_UID) {
            return UidInfo("android.uid.system", "Android Tizimi", null)
        }
        if (uid == 0) {
            return UidInfo("android.uid.root", "Root xizmatlari", null)
        }
        if (uid == -4 || uid == NetworkStats.Bucket.UID_REMOVED) {
            return UidInfo("android.uid.removed", "O'chirilgan ilovalar", null)
        }

        val pkgs = try {
            packageManager.getPackagesForUid(uid)
        } catch (_: Exception) {
            null
        }

        val primaryPkg = pkgs?.firstOrNull() ?: "uid:$uid"
        return try {
            val appInfo = packageManager.getApplicationInfo(primaryPkg, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString()
            val drawable = packageManager.getApplicationIcon(appInfo)
            val bitmap = DrawableUtils.toImageBitmap(primaryPkg, drawable)
            UidInfo(primaryPkg, label, bitmap)
        } catch (_: Exception) {
            val fallback = primaryPkg.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            UidInfo(primaryPkg, fallback, null)
        }
    }
}

