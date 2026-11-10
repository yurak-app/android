package com.yurak.agent.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate

@Composable
fun SyncStatusCard(
    pendingCount: Int,
    syncing: Boolean,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (pendingCount > 0) "$pendingCount ta ma'lumot navbatda" else "Server bilan to'liq tenglashtirilgan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = YurakSlate,
                )
                Text(
                    text = if (pendingCount > 0) "Serverga yuborish uchun bosing" else "Barcha o'zgarishlar saqlangan",
                    fontSize = 11.sp,
                    color = YurakMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Button(
                onClick = onSyncNow,
                enabled = !syncing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YurakSlate),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yuborish",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

