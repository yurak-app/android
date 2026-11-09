package com.yurak.agent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yurak.agent.AppContainer
import com.yurak.agent.data.ApiException
import com.yurak.agent.data.StoredCredentials
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(container: AppContainer, deviceName: String, onLoggedIn: (StoredCredentials) -> Unit) {
    var serverUrl by remember { mutableStateOf(container.config.backendUrl) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (loading) return
        if (email.isBlank() || password.isBlank()) {
            error = "Enter your email and password."
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
        Text(
            "Yurak",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = YurakCrimson,
            ),
        )
        Text(
            "Log in to start tracking on this device.",
            style = MaterialTheme.typography.bodyMedium.copy(color = YurakMuted),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YurakCardBg,
                unfocusedContainerColor = YurakCardBg,
                focusedTextColor = YurakSlate,
                unfocusedTextColor = YurakSlate,
                focusedBorderColor = YurakCrimson,
                unfocusedBorderColor = YurakBorder,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YurakCardBg,
                unfocusedContainerColor = YurakCardBg,
                focusedTextColor = YurakSlate,
                unfocusedTextColor = YurakSlate,
                focusedBorderColor = YurakCrimson,
                unfocusedBorderColor = YurakBorder,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YurakCardBg,
                unfocusedContainerColor = YurakCardBg,
                focusedTextColor = YurakSlate,
                unfocusedTextColor = YurakSlate,
                focusedBorderColor = YurakCrimson,
                unfocusedBorderColor = YurakBorder,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
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
                Text("Log in", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Text(
            "No account yet? Create one on the Yurak website — this app is sign-in only.",
            style = MaterialTheme.typography.bodySmall.copy(color = YurakMuted),
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
