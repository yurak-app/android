package com.yurak.agent.data

import android.content.Context

/**
 * Small on-disk settings — plain (not encrypted) SharedPreferences, same reasoning as the
 * Windows agent's config.json: nothing here is secret.
 */
class AppConfig(context: Context) {
    private val prefs = context.getSharedPreferences("yurak_config", Context.MODE_PRIVATE)

    var backendUrl: String
        get() {
            val saved = prefs.getString(KEY_BACKEND_URL, null)
            if (saved.isNullOrBlank() || saved.contains("10.14.56.52") || saved.contains("10.0.2.2") || saved.contains("localhost")) {
                return DEFAULT_BACKEND_URL
            }
            return saved
        }
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value).apply()

    private companion object {
        const val KEY_BACKEND_URL = "backend_url"
        const val DEFAULT_BACKEND_URL = "https://yurak-backend.onrender.com"
    }
}
