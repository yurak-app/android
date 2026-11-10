package com.yurak.agent

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yurak.agent.tracking.SyncWorker
import com.yurak.agent.ui.MainScreen
import com.yurak.agent.ui.YurakTheme
import com.yurak.agent.ui.auth.LoginScreen

/** The app's one screen host — swaps between LoginScreen and MainScreen with BottomNavigation */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as YurakApplication).container

        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE

        setContent {
            YurakTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var credentials by remember { mutableStateOf(container.credentialStore.load()) }

                    if (credentials == null) {
                        LoginScreen(
                            container = container,
                            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                            onLoggedIn = { result ->
                                container.credentialStore.save(result)
                                SyncWorker.schedule(applicationContext)
                                credentials = result
                            },
                        )
                    } else {
                        MainScreen(
                            container = container,
                            onLogout = { credentials = null },
                        )
                    }
                }
            }
        }
    }
}
