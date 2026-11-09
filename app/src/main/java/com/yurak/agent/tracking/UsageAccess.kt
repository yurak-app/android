package com.yurak.agent.tracking

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

/**
 * "Usage Access" is not a normal runtime permission — there's no dialog to show; the only
 * way to grant it is the user finding Settings > Apps > Special app access > Usage access
 * and toggling this app on by hand. [hasAccess] and [openSettings] are the two things the
 * UI needs: check whether that's already happened, and deep-link there if not.
 */
object UsageAccess {
    fun hasAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        // checkOpNoThrow (not the newer unsafeCheckOpNoThrow, API 29+) — available since API
        // 19, comfortably below this app's minSdk 28. Same check, just the older name.
        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
