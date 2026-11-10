package com.yurak.agent.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.data.DeviceItemDto
import com.yurak.agent.ui.YurakBackground
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.profile.components.AgentStatusCard
import com.yurak.agent.ui.profile.components.ConnectedDevicesCard
import com.yurak.agent.ui.profile.components.UserProfileCard

/**
 * Profile and Settings screen for Yurak Mobile Agent.
 * Decomposed into modular components: UserProfileCard, ConnectedDevicesCard, AgentStatusCard.
 */
@Composable
fun ProfileScreen(
    userEmail: String,
    devices: List<DeviceItemDto>,
    hasUsageAccess: Boolean,
    pendingCount: Int,
    onOpenUsageSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onCheckUpdate: () -> Unit,
    onLogout: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. User Profile Header Card
        item {
            UserProfileCard(userEmail = userEmail)
        }

        // 2. Connected Devices List
        item {
            ConnectedDevicesCard(devices = devices)
        }

        // 3. System Permissions, Sync, and App Version Update Check
        item {
            AgentStatusCard(
                hasUsageAccess = hasUsageAccess,
                pendingCount = pendingCount,
                onOpenUsageSettings = onOpenUsageSettings,
                onSyncNow = onSyncNow,
                onCheckUpdate = onCheckUpdate,
            )
        }

        // 4. Logout Action Button
        item {
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YurakCrimson),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hisobdan chiqish (Log out)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
