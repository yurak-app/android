plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.yurak.agent"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yurak.agent"
        // 28 (Android 9, 2018+) rather than lower — SCREEN_INTERACTIVE/NON_INTERACTIVE and
        // KEYGUARD_SHOWN/HIDDEN usage-events (what lock/unlock tracking is built on) aren't
        // reliably available before this. See tracking/UsageEventReader.kt.
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Use debug signing by default so the GitHub release APK can be directly
            // downloaded and installed on user devices without requiring a custom keystore.
            // If a production release keystore is configured later, it can override this.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        // Paired with the Kotlin 1.9.24 version pinned in the root build.gradle.kts —
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Periodic sync — see tracking/SyncWorker.kt. 15 minutes is the OS-enforced floor
    // for periodic work regardless of what's requested, matching the Windows agent's
    // and the backend's own cadence assumptions.
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // The local durable event queue — Android-idiomatic equivalent of the Windows
    // agent's OfflineEventStore / the extension's storage.ts queue.
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // EncryptedSharedPreferences for the stored api_token — Android's equivalent of the
    // Windows agent's DPAPI-encrypted credentials.dat. Still alpha upstream (has been for
    // years) but is the standard, Google-maintained way to do this on Android; same
    // "best available option" call as the Windows README makes about its own scaffold assets.
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
