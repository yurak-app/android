package com.yurak.agent.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yurak.agent.AppContainer
import com.yurak.agent.data.ApiException
import com.yurak.agent.data.StoredCredentials
import com.yurak.agent.ui.YurakBackground
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakMuted
import com.yurak.agent.ui.auth.components.AuthFields
import com.yurak.agent.ui.auth.components.AuthHeader
import kotlinx.coroutines.launch

/**
 * Modernized, modular Login screen for Yurak Mobile Agent.
 */
@Composable
fun LoginScreen(
    container: AppContainer,
    deviceName: String,
    onLoggedIn: (StoredCredentials) -> Unit,
) {
    var serverUrl by remember { mutableStateOf(container.config.backendUrl) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (loading) return
        if (email.isBlank() || password.isBlank()) {
            error = "Email va parolingizni kiriting."
            return
        }
        loading = true
        error = null
        scope.launch {
            try {
                if (serverUrl.isNotBlank() && serverUrl.trim() != container.config.backendUrl) {
                    container.config.backendUrl = serverUrl.trim().removeSuffix("/")
                    container.onBackendUrlChanged()
                }
                val result = container.apiClient.login(email.trim(), password, deviceName)
                onLoggedIn(StoredCredentials(result.user.email, result.apiToken, result.deviceId))
            } catch (e: ApiException) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YurakBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        AuthHeader()

        Spacer(modifier = Modifier.height(24.dp))

        AuthFields(
            serverUrl = serverUrl,
            onServerUrlChange = { serverUrl = it },
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Button(
            onClick = ::submit,
            enabled = !loading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YurakCrimson),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
            } else {
                Text(
                    text = "Kirish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Yangi hisob yaratish uchun rasmiy Yurak veb-saytidan foydalaning.",
            fontSize = 12.sp,
            color = YurakMuted,
        )
    }
}

