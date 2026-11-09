package com.yurak.agent

import android.app.Application
import com.yurak.agent.tracking.SyncWorker

class YurakApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Re-register the periodic job on every process start (schedule() is a harmless
        // no-op via KEEP if it's already registered) — WorkManager itself survives reboots
        // and process death on its own, but there's no reason not to make sure on launch too.
        if (container.credentialStore.load() != null) {
            SyncWorker.schedule(this)
        }
    }
}
