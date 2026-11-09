package com.yurak.agent.tracking

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.util.Log

data class AppTrafficStats(
    val packageName: String,
    val appDisplayName: String?,
    val mobileBytes: Long,
    val wifiBytes: Long,
    val hotspotBytes: Long = 0L,
    val totalBytes: Long = mobileBytes + wifiBytes + hotspotBytes,
)

class NetworkTrafficReader(
    private val context: Context,
    private val packageManager: PackageManager,
) {
    private val TAG = "NetworkTrafficReader"

    private val networkStatsManager: NetworkStatsManager? by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
    }

    private class TempStats(
        var mobileBytes: Long = 0L,
        var wifiBytes: Long = 0L,
        var hotspotBytes: Long = 0L,
    )

    fun readTraffic(startTime: Long, endTime: Long): List<AppTrafficStats> {
        val manager = networkStatsManager ?: return emptyList()
        val safeEndTime = if (endTime <= startTime) System.currentTimeMillis() + 1000L else endTime + 1000L
        val uidMap = mutableMapOf<Int, TempStats>()

        // 1. Cellular
        querySummary(manager, ConnectivityManager.TYPE_MOBILE, null, startTime, safeEndTime) { uid, rx, tx ->
            val stats = uidMap.getOrPut(uid) { TempStats() }
            if (uid == -5 || uid == NetworkStats.Bucket.UID_TETHERING) {
                stats.hotspotBytes += (rx + tx)
            } else {
                stats.mobileBytes += (rx + tx)
            }
        }

        // 2. Wi-Fi
        querySummary(manager, ConnectivityManager.TYPE_WIFI, null, startTime, safeEndTime) { uid, rx, tx ->
            val stats = uidMap.getOrPut(uid) { TempStats() }
            if (uid == -5 || uid == NetworkStats.Bucket.UID_TETHERING) {
                stats.hotspotBytes += (rx + tx)
            } else {
                stats.wifiBytes += (rx + tx)
            }
        }

        val result = mutableListOf<AppTrafficStats>()
        for ((uid, stats) in uidMap) {
            val total = stats.mobileBytes + stats.wifiBytes + stats.hotspotBytes
            if (total <= 0) continue

            val details = resolveUidDetails(uid)
            result.add(
                AppTrafficStats(
                    packageName = details.packageName,
                    appDisplayName = details.appName,
                    mobileBytes = stats.mobileBytes,
                    wifiBytes = stats.wifiBytes,
                    hotspotBytes = stats.hotspotBytes,
                    totalBytes = total,
                )
            )
        }
        return result
    }

    private inline fun querySummary(
        manager: NetworkStatsManager,
        networkType: Int,
        subscriberId: String?,
        startTime: Long,
        endTime: Long,
        crossinline onBucket: (uid: Int, rx: Long, tx: Long) -> Unit,
    ) {
        var stats: NetworkStats? = null
        try {
            stats = manager.querySummary(networkType, subscriberId, startTime, endTime)
            val bucket = NetworkStats.Bucket()
            while (stats?.hasNextBucket() == true) {
                stats.getNextBucket(bucket)
                onBucket(bucket.uid, bucket.rxBytes, bucket.txBytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "querySummary error for type $networkType: ${e.message}")
        } finally {
            try {
                stats?.close()
            } catch (_: Exception) {}
        }
    }

    private data class UidDetails(val packageName: String, val appName: String)

    private fun resolveUidDetails(uid: Int): UidDetails {
        if (uid == -5 || uid == NetworkStats.Bucket.UID_TETHERING) {
            return UidDetails("system:hotspot", "Hotspot")
        }
        if (uid == Process.SYSTEM_UID) {
            return UidDetails("android.uid.system", "Android OS")
        }
        if (uid == 0) {
            return UidDetails("android.uid.root", "System (Root)")
        }
        if (uid == -4 || uid == NetworkStats.Bucket.UID_REMOVED) {
            return UidDetails("android.uid.removed", "Removed apps")
        }

        val packages = try {
            packageManager.getPackagesForUid(uid)
        } catch (_: Exception) {
            null
        }

        if (packages.isNullOrEmpty()) {
            return UidDetails("uid.$uid", "App (UID $uid)")
        }

        val primaryPackage = packages[0]
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(primaryPackage, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(primaryPackage, 0)
            }
            val label = packageManager.getApplicationLabel(appInfo).toString()
            UidDetails(primaryPackage, label)
        } catch (_: Exception) {
            UidDetails(primaryPackage, primaryPackage)
        }
    }
}

