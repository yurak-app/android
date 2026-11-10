package com.yurak.agent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * URL-path style navigation destinations for Yurak Android.
 * Matches web app routing conventions for consistency and modularity.
 */
sealed class YurakRoute(
    val path: String,
    val titleUz: String,
    val icon: ImageVector,
) {
    data object Dashboard : YurakRoute(
        path = "app/dashboard",
        titleUz = "Dashboard",
        icon = Icons.Default.Home
    )

    data object AppUsage : YurakRoute(
        path = "app/app-usage",
        titleUz = "Ekran vaqti",
        icon = Icons.Default.BarChart
    )

    data object DataUsage : YurakRoute(
        path = "app/data-usage",
        titleUz = "Tarmoq",
        icon = Icons.Default.Language
    )

    data object Profile : YurakRoute(
        path = "app/profile",
        titleUz = "Profil",
        icon = Icons.Default.Person
    )

    companion object {
        val bottomNavItems: List<YurakRoute>
            get() = listOf(Dashboard, AppUsage, DataUsage, Profile)
    }
}
