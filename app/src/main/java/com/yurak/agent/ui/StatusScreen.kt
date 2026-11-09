package com.yurak.agent.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yurak.agent.AppContainer
import com.yurak.agent.tracking.SyncWorker
import com.yurak.agent.tracking.UsageAccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The only screen shown once logged in — status + the same handful of settings the
 * Windows agent's StatusPage/SettingsPage show, nothing visual about the tracked data
 * itself (that's the website's job). Polls the local queue count and Usage Access status
 * every couple seconds rather than wiring up WorkManager's LiveData/Flow observers or a
 * lifecycle-resume callback — simpler, and "up to 2s stale" is not something a human
 * looking at this screen will ever notice.
 */
@Composable
fun StatusScreen(container: AppContainer, onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingCount by remember { mutableStateOf(0) }
    var hasUsageAccess by remember { mutableStateOf(UsageAccess.hasAccess(context)) }
    var syncing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            pendingCount = container.database.pendingEvents().count()
            hasUsageAccess = UsageAccess.hasAccess(context)
            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Yurak", style = MaterialTheme.typography.headlineMedium)
        Text(
            container.credentialStore.load()?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        if (!hasUsageAccess) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Usage Access needed", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Yurak needs Usage Access to see which app is in the foreground. " +
                            "This can't be granted from here directly — tap below, find Yurak in the list, and turn it on.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                    )
                    Button(onClick = { UsageAccess.openSettings(context) }) {
                        Text("Open Usage Access settings")
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tracking", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (pendingCount > 0) "$pendingCount event${if (pendingCount == 1) "" else "s"} queued to sync"
                        else "Up to date",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Button(
            onClick = {
                syncing = true
                SyncWorker.runOnce(context)
                scope.launch {
                    delay(1500) // WorkManager doesn't hand back "done" synchronously — see doc comment
                    syncing = false
                }
            },
            enabled = !syncing,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(if (syncing) "Syncing…" else "Sync now")
        }

        OutlinedButton(
            onClick = {
                SyncWorker.cancel(context)
                container.credentialStore.clear()
                container.trackingState.clear()
                scope.launch { container.database.pendingEvents().deleteAll() } // discard anything still unsynced — it belongs to the account being left
                onLogout()
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Log out")
        }
    }
}
