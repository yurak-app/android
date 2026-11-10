package com.yurak.agent.ui.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakMuted

@Composable
fun AuthHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Yurak",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = YurakCrimson,
            ),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Qurilmangizda telemetriyani boshlash uchun tizimga kiring.",
            fontSize = 14.sp,
            color = YurakMuted,
        )
    }
}

