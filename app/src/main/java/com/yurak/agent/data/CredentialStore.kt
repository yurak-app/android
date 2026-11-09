package com.yurak.agent.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.Serializable

@Serializable
data class StoredCredentials(
    val email: String,
    val apiToken: String,
    val deviceId: String,
)

/**
 * Stores the device's api_token via EncryptedSharedPreferences (AES256-GCM, keyed by an
 * Android Keystore master key) — the Android equivalent of the Windows agent's
 * DPAPI-encrypted credentials.dat. Only this app, on this device, can decrypt it.
 */
class CredentialStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "yurak_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun load(): StoredCredentials? {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val deviceId = prefs.getString(KEY_DEVICE_ID, null) ?: return null
        return StoredCredentials(email, token, deviceId)
    }

    fun save(credentials: StoredCredentials) {
        prefs.edit()
            .putString(KEY_EMAIL, credentials.email)
            .putString(KEY_TOKEN, credentials.apiToken)
            .putString(KEY_DEVICE_ID, credentials.deviceId)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_EMAIL = "email"
        const val KEY_TOKEN = "api_token"
        const val KEY_DEVICE_ID = "device_id"
    }
}
