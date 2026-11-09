package com.yurak.agent

import android.content.Context
import com.yurak.agent.data.ApiClient
import com.yurak.agent.data.AppConfig
import com.yurak.agent.data.CredentialStore
import com.yurak.agent.db.AppDatabase
import com.yurak.agent.tracking.TrackingState

/**
 * Everything the app needs, built once and handed around — no DI framework (Hilt/Dagger):
 * this app is small enough (a login screen, a status screen, one background worker) that
 * manual wiring is simpler than the ceremony a framework would add. Same call made for the
 * Windows agent (plain classes wired in MainWindow) and the browser extension (a handful of
 * modules importing each other directly).
 */
class AppContainer(context: Context) {
    val config = AppConfig(context)
    val credentialStore = CredentialStore(context)
    val trackingState = TrackingState(context)
    val database: AppDatabase = AppDatabase.get(context)
    val apiClient = ApiClient(config.backendUrl)

    /** Call after Settings changes the backend URL — repoints future requests without
     * recreating the container. */
    fun onBackendUrlChanged() {
        apiClient.setBaseUrl(config.backendUrl)
    }
}
