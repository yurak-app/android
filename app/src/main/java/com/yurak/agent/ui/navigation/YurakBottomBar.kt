package com.yurak.agent.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakCrimsonLight
import com.yurak.agent.ui.YurakMuted

@Composable
fun YurakBottomBar(
    currentRoute: YurakRoute,
    onNavigate: (YurakRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier.border(BorderStroke(1.dp, YurakBorder)),
    ) {
        YurakRoute.bottomNavItems.forEach { route ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.titleUz
                    )
                },
                label = {
                    Text(
                        text = route.titleUz,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YurakCrimson,
                    selectedTextColor = YurakCrimson,
                    unselectedIconColor = YurakMuted,
                    unselectedTextColor = YurakMuted,
                    indicatorColor = YurakCrimsonLight,
                ),
            )
        }
    }
}

