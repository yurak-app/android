package com.yurak.agent.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakEmerald
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.update.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo?,
    isChecking: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = YurakCrimson,
                    )
                    Text("Tekshirilmoqda...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = YurakSlate)
                } else if (updateInfo?.hasUpdate == true) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = YurakCrimson, modifier = Modifier.size(24.dp))
                    Text("Yangi versiya mavjud!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = YurakSlate)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = YurakEmerald, modifier = Modifier.size(24.dp))
                    Text("Ilova yangilangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = YurakSlate)
                }
            }
        },
        text = {
            if (isChecking) {
                Text(
                    text = "GitHub Releases orqali eng so'nggi versiya tekshirilmoqda...",
                    fontSize = 13.sp,
                    color = YurakMuted,
                )
            } else if (updateInfo?.hasUpdate == true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Joriy versiya: v${updateInfo.currentVersion} → Yangi: v${updateInfo.latestVersion}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = YurakCrimson,
                    )
                    Text(
                        text = updateInfo.releaseTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = YurakSlate,
                    )
                    Text(
                        text = updateInfo.releaseNotes,
                        fontSize = 12.sp,
                        color = YurakMuted,
                        lineHeight = 16.sp,
                    )
                }
            } else {
                Text(
                    text = "Sizda Yurak ilovasining eng so'nggi versiyasi (v${updateInfo?.currentVersion ?: "1.0.0"}) o'rnatilgan.",
                    fontSize = 13.sp,
                    color = YurakSlate,
                )
            }
        },
        confirmButton = {
            if (updateInfo?.hasUpdate == true && !isChecking) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                        context.startActivity(intent)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YurakCrimson),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Yuklab olish", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK", color = YurakCrimson, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (updateInfo?.hasUpdate == true && !isChecking) {
                TextButton(onClick = onDismiss) {
                    Text("Keyinroq", color = YurakMuted)
                }
            }
        },
    )
}
