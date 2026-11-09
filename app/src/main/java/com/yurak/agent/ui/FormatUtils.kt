package com.yurak.agent.ui

import java.util.Locale

object FormatUtils {
    fun formatDuration(totalSeconds: Int): String {
        if (totalSeconds <= 0) return "0 soniya"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}s ${minutes}d"
            hours > 0 -> "${hours}s"
            minutes > 0 -> "${minutes} daqiqa"
            else -> "${seconds} soniya"
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        val tb = gb * 1024.0

        val d = bytes.toDouble()
        return when {
            d >= tb -> String.format(Locale.US, "%.1f TB", d / tb)
            d >= gb -> String.format(Locale.US, "%.2f GB", d / gb)
            d >= mb -> String.format(Locale.US, "%.1f MB", d / mb)
            d >= kb -> String.format(Locale.US, "%.0f KB", d / kb)
            else -> "$bytes B"
        }
    }
}

