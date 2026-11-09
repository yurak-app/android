<div align="center">

# ❤️ Yurak Android App & Agent

**Native Kotlin + Jetpack Compose application for tracking application usage, system events, and network data traffic with a modern 4-tab dashboard.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-9.0%2B_(API_28%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![WorkManager](https://img.shields.io/badge/WorkManager-Periodic_Sync-FF8800?logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/workmanager)

[Features](#-features--screens) • [Architecture](#-architecture--design) • [Build & Install](#-build--installation)

</div>

---

## 🌟 Overview

The **Yurak Android App** is both a lightweight, battery-efficient tracking agent and an interactive personal analytics dashboard. It monitors application foreground time, screen locks/unlocks, battery status, and network data usage, while offering a full-featured Compose UI matching the web platform.

---

## 📱 Features & Screens

### 1. 🏠 Dashboard (`Bosh sahifa`)
- Real-time KPI cards: Today's screen time, total data traffic, and connected device pill.
- Live battery level indicator with charging state.
- Top active applications list for the day.
- One-tap **"Sinxronlash" (Sync now)** button with instant background flush.

### 2. 📊 App Usage (`Ilovalar`)
- **Period Selectors**: Switch between `Kun` (Daily), `Hafta` (Weekly), and `Oy` (Monthly) analytics.
- **Interactive 7-Day Stacked Bar Chart**: Highlighting daily screen time across categories with dynamic day selection.
- **Week Date Range Stepper**: Navigate back and forth between weeks.
- **Search & Filter Chips**: Search apps by name and filter by platform (`Barchasi`, `Mobile`, `Desktop`).
- Detailed app breakdown showing total duration and session counts.

### 3. 🌐 Data Traffic (`Trafik`)
- Hero metric card showing total internet consumption.
- Dedicated category breakdown cards:
  - 📶 **Wi-Fi**
  - 📡 **Mobil Internet (Cellular)**
  - 🔄 **Hotspot (Tarqatish)**
  - 💻 **Desktop Ma'lumotlari**
- Per-application internet data usage list.

### 4. 👤 Profile & Devices (`Profil`)
- User avatar, name, and registered email address.
- Linked devices list with active status and last-seen timestamps.
- System permissions monitor (Usage Access, Notifications).
- Secure sign-out with credential clearing.

### 🎨 5. Clean Light & Crimson UI
- High-contrast pure white card background (`#FFFFFF`) with soft borders (`#E2E8F0`).
- Deep Slate typography (`#0F172A`) ensuring readability under all system lighting conditions.
- Signature **Yurak Crimson** (`#E11D48`) and vibrant accents.

---

## 🏗 Architecture & Design

| Component | Technology | Role |
|---|---|---|
| **Language** | Kotlin | Standard Android language |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative, modern UI shell |
| **Activity Tracking** | `UsageStatsManager` | Reads OS-level app switch events without continuous polling |
| **Background Sync** | `WorkManager` (Periodic ~15 min) | Reliable, battery-friendly scheduled sync surviving device reboots |
| **API Client** | OkHttp + Retrofit + kotlinx.serialization | REST API client connecting to Yurak Backend |
| **Credentials** | `EncryptedSharedPreferences` | Hardware-backed secure token storage |
| **Local Settings** | `SharedPreferences` | Stores server URL (defaults to `https://yurak-backend.onrender.com`) |

### 🔋 Battery-Friendly Event Reconstruction
Unlike traditional tracking tools that poll the foreground window every few seconds, Yurak Android relies on Android's native `UsageStatsManager` event log:
- **`MOVE_TO_FOREGROUND`**: Records app switches.
- **`SCREEN_NON_INTERACTIVE`**: Records screen-off as `lock` events.
- **`KEYGUARD_HIDDEN`**: Records screen unlock as `unlock` events.
- **No Foreground Service or Notification Spam**: Runs quietly in the background without draining battery.

---

## 🛠 Build & Installation

### 1. Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 or JDK 21
- Android SDK (API 28+)

### 2. Build Debug APK via Command Line
```bash
# Navigate to the android directory
cd android

# Build the debug APK
./gradlew assembleDebug
```
The compiled APK will be generated at:
```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### 3. Install on Connected Device
With USB Debugging enabled on your Android device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔐 Permissions Required

- **`PACKAGE_USAGE_STATS` (Usage Access)**: Required to read foreground application switch events. The app prompts and deep-links directly to Android Settings on first launch.
- **`INTERNET` & `ACCESS_NETWORK_STATE`**: Required to sync data with the backend API.
- **`READ_NETWORK_USAGE_HISTORY`**: Required to calculate Wi-Fi and mobile data consumption.

---

## 📄 License
MIT © [Yurak Team](https://github.com/yurak-app)
