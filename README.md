# Yurak Android Agent

A Kotlin/Jetpack Compose app that tracks per-app foreground time (plus screen lock/unlock,
battery, and timezone) and syncs it to the Yurak backend. Same philosophy as the Windows
agent and the browser extension: **sign-in only, no dashboards** — you create your account
on the Yurak website, and every view of the data lives there.

## Why this stack

| Piece | Choice | Why |
|---|---|---|
| Language | Kotlin | Modern Android standard |
| UI | Jetpack Compose | Two screens (login, status) — Compose keeps that small |
| Background work | WorkManager, periodic (~15 min) | The OS-enforced floor for periodic work anyway, matching the Windows agent's and backend's own sync cadence |
| Usage tracking | `UsageStatsManager` | See below |
| Local durable queue | Room (SQLite) | Android-idiomatic version of the Windows agent's `OfflineEventStore` / the extension's storage queue — write immediately, delete only after a confirmed sync |
| API client | Retrofit + OkHttp + kotlinx.serialization | Typed, standard |
| Credential storage | EncryptedSharedPreferences | Android's equivalent of the Windows agent's DPAPI-encrypted `credentials.dat` |
| DI | None (manual wiring, see `AppContainer.kt`) | App's too small to earn Hilt/Dagger — same call made for the Windows agent and the extension |
| `minSdk` | 28 (Android 9, 2018+) | See below |

### No foreground service, no continuous polling

This is the one real design difference from the Windows agent. Windows has to *poll*
(`GetForegroundWindow` every 1.5s) because Windows doesn't keep app-switch history for you.
Android already does: `UsageStatsManager` maintains an event log —
`MOVE_TO_FOREGROUND`/`MOVE_TO_BACKGROUND` (app switches), `SCREEN_INTERACTIVE`/
`SCREEN_NON_INTERACTIVE` (screen on/off), `KEYGUARD_SHOWN`/`KEYGUARD_HIDDEN` (lock/unlock).
`SyncWorker` (`tracking/SyncWorker.kt`) runs every ~15 minutes and reads that log since the
last checkpoint (`tracking/UsageEventReader.kt` reconstructs segments from it) — no
continuously-running service, no persistent notification, nothing polling in real time.
`minSdk 28` is specifically because `SCREEN_NON_INTERACTIVE`/`KEYGUARD_HIDDEN` were added in
API 28; lock/unlock tracking wouldn't be reliably available on anything older.

Talks to the **exact same backend contract** the Windows agent and extension use —
`POST /auth/login` with `device: { platform: "android", name }`, the same `POST /events`
shape (see `data/Dtos.kt`, deliberately mirroring the Windows agent's `Models/Dtos.cs` and
the extension's `types.ts`). No backend changes needed.

## What it tracks, and how

`UsageEventReader` walks the event log and turns it into segments:

- **App switches** (`MOVE_TO_FOREGROUND`) close the current segment and open a new one —
  only when the *package* actually changes, not on every internal activity-to-activity
  navigation within one app (there's no handling of `MOVE_TO_BACKGROUND`/`ACTIVITY_PAUSED`
  at all, deliberately — see the doc comment in the reader).
- **Screen off** (`SCREEN_NON_INTERACTIVE`) closes whatever's open and is recorded as a
  `lock` system event.
- **Keyguard dismissed** (`KEYGUARD_HIDDEN`) is recorded as an `unlock` system event.
  `SCREEN_INTERACTIVE` (the screen waking up, possibly still to a lock screen) and
  `KEYGUARD_SHOWN` are deliberately not separately emitted — "unlock" means genuinely
  usable again, not just the display turning on.
- The launcher/home screen is tracked as its own pseudo-app (identifier `"home"`, matching
  how the Windows agent treats the desktop/taskbar) rather than under its raw package name
  or dropped entirely.
- A segment that's still open at the end of a read (the app currently in the foreground)
  stays open — its true end isn't known yet, so it's persisted (`tracking/TrackingState.kt`)
  and picked up by the *next* run, not guessed at or cut short.
- A session (`sessionStart`, feeding per-app session counts) starts on every genuine app
  switch and every return from a locked screen — never on an internal navigation within the
  same app.

### Idle detection is coarser than Windows' — on purpose, not by oversight

Windows can ask "how long since any keyboard/mouse input, system-wide" (`GetLastInputInfo`)
regardless of which app is focused. Android has no equivalent for third-party apps — there's
no system-wide "last touch" timestamp available without an Accessibility Service (a much
heavier, more sensitive permission than anything else this app asks for, and arguably
overkill for a personal tracker). The practical proxy used here is **screen off** — if the
screen is on and an app is in the foreground, it counts as active, the same way Android's
own Digital Wellbeing effectively works. There's no configurable "idle after N seconds while
the screen stays on" setting, because there's no real signal behind it to configure.

## Permissions — and what each one costs the "login-only" design

- **`PACKAGE_USAGE_STATS`** ("Usage Access") — the load-bearing one. Unlike a normal runtime
  permission, this can't be requested via a dialog; `StatusScreen` deep-links to
  `Settings.ACTION_USAGE_ACCESS_SETTINGS` and you toggle it on by hand, then come back. This
  adds a real third step beyond login (log in → grant Usage Access), but doesn't touch the
  account model at all — still sign-in only, still no register screen anywhere in the app.
- **`POST_NOTIFICATIONS`** (Android 13+) — declared for a future goal-limit notification
  (mirroring the Windows agent's toast) that isn't wired up yet. Normal, low-friction runtime
  prompt when it is.
- **No `FOREGROUND_SERVICE`, no `RECEIVE_BOOT_COMPLETED`** — the event-log approach needs
  neither; WorkManager's own periodic-work scheduling survives reboots on its own.
- **Not a permission, but worth knowing**: aggressive OEM battery optimization (Samsung,
  Xiaomi, etc.) can delay WorkManager's periodic runs further than stock Android would. It
  copes (reschedules rather than silently dying), but sync could lag more on some phones.

## Running it

You'll need **Android Studio** (bundles the SDK, Gradle, everything) — this project has no
Gradle wrapper checked in; Android Studio generates one the first time you open it (or run
`gradle wrapper` yourself if you have a standalone Gradle install).

1. `File → Open` → this `android/` folder.
2. Let Gradle sync (first time only, downloads dependencies).
3. Run on an emulator (Device Manager can create one) or a physical phone with
   Developer Options → USB debugging on, plugged in via USB.
   - **Emulator**: the default backend URL (`http://10.0.2.2:4000`, in Settings) is the
     emulator's own alias for "the host machine's localhost" — works out of the box against
     a backend running on your dev machine.
   - **Physical device**: `10.0.2.2` doesn't mean anything to a real phone. Set the backend
     URL to your dev machine's actual LAN IP (e.g. `http://192.168.1.23:4000`) instead, and
     make sure the phone's on the same network.
4. First launch: login screen (email + password — no register option, per plan.md). After
   logging in, the status screen prompts for Usage Access if it isn't granted yet.

CLI equivalent of step 3 (with a device/emulator already connected):
`./gradlew installDebug` from this directory.

## Known gaps

- **No icon reporting, location, or system-info reporting** — the Windows agent grew these
  organically over time; they were never part of Phase 1's actual scope (idle/screen/lock
  detection, session counts), so this first pass leaves them out rather than importing scope
  creep from a different platform. Same reasoning for goal-limit notifications.
- **Logout doesn't attempt a final sync first** — unlike the Windows agent and the extension,
  which both try to flush whatever's queued before discarding it. Doable (enqueue a sync,
  await its completion via WorkManager's `WorkInfo` flow, then clear), just not wired up in
  this pass; worst case today is losing whatever accumulated since the last successful sync
  (bounded by the ~15-minute interval) when you explicitly log out.
- **Not build-verified here** — this environment has no Android SDK/Gradle. Written and
  reviewed carefully, but a real first build/run needs to happen in Android Studio.
- **Launcher icon** is the same generated placeholder mark used for the Windows agent's
  scaffold assets and the extension's toolbar icon (downscaled from `frontend/public/logo.png`)
  — fine for now, swap `app/src/main/res/mipmap-*/ic_launcher*.png` for real branding later.
#   a n d r o i d  
 