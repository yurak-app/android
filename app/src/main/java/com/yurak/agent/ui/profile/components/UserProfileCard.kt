package com.yurak.agent.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate

@Composable
fun UserProfileCard(
    userEmail: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = YurakCardBg),
        border = BorderStroke(1.dp, YurakBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(YurakCrimson),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = userEmail.take(1).uppercase(),
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = userEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = YurakSlate,
            )

            Text(
                text = userEmail,
                fontSize = 13.sp,
                color = YurakMuted,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFE4E6),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = YurakCrimson,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Yurak Telemetriya Foydalanuvchisi",
                        fontSize = 11.sp,
                        color = Color(0xFF9F1239),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

