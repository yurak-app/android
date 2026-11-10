package com.yurak.agent.ui.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yurak.agent.ui.YurakBorder
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakCrimson
import com.yurak.agent.ui.YurakSlate

@Composable
fun AuthFields(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = YurakCardBg,
        unfocusedContainerColor = YurakCardBg,
        focusedTextColor = YurakSlate,
        unfocusedTextColor = YurakSlate,
        focusedBorderColor = YurakCrimson,
        unfocusedBorderColor = YurakBorder,
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            label = { Text("Server URL") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Parol") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )
    }
}

