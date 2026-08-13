# Institute Calling — Android (login + caller flow)

The first vertical slice of the calling CRM: role selection, the City → Branch →
Caller login hierarchy, PIN login, the caller's dial → call → disposition flow,
and a placeholder owner home. Built with the agreed architecture so the backend
and remaining screens drop in later.

## What's implemented

- **Landing** — Owner login / Employee login.
- **Employee login hierarchy** — City → Branch → your name → PIN.
- **Owner login** — pick owner (Neha / Deepa) → PIN.
- **PIN login** — 4-digit keypad, error state.
- **Caller flow** — number entry + dialpad, one Call button, in-call timer,
  disposition screen (six outcomes), optional notes, Save, "calls today" counter,
  log out.
- **Owner home** — greeting + today's totals + branch list (a stub; the date
  review, analytics, and full management screens are intentionally not built yet).

## Architecture

Clean-ish layering in a single module, ready to split later:

```
domain/      models + repository interface (pure Kotlin, no Android)
data/        InMemoryCallingRepository (seeded stand-in for the backend)
di/          Hilt module binding the interface to the implementation
ui/          Compose screens + ViewModels (MVVM, StateFlow, immutable UI state)
```

- Kotlin, Jetpack Compose, Material 3
- MVVM, Repository pattern, Hilt DI, Navigation Compose
- Coroutines + Flow, `StateFlow` UI state, no business logic in composables
- `SessionViewModel` (activity-scoped) holds the hierarchy selections + auth;
  `CallerViewModel` owns the call lifecycle.

## How to build

1. Open the `InstituteCalling` folder in **Android Studio** (latest stable).
2. Let it sync. If it flags a version, accept its suggested update — the versions
   in `gradle/libs.versions.toml` are a recent-but-may-need-nudging starting point,
   and **the `ksp` version must match the `kotlin` version**.
3. Run on an emulator or device (minSdk 24).

There is no Gradle wrapper committed; Android Studio adds it on first open, or run
`gradle wrapper` if you have Gradle installed.

## Backend connection (online-only)

This app now talks to the NestJS backend over HTTP — login, the city/branch/caller
list, and saving calls all hit the API. **The backend must be running** (see the
`backend` project). Configure the address in `di/NetworkModule.kt`:

- Android **emulator** → `http://10.0.2.2:3000/` (already set; this is how the
  emulator reaches your computer's localhost).
- Real **phone** on the same Wi-Fi → change it to your computer's LAN IP, e.g.
  `http://192.168.1.50:3000/`.

Seed logins (from the backend): owners Neha/Deepa, the Rambaug callers, etc., all
with PIN **1234**. Login now actually verifies the PIN against the server.

This is **online-only**: with no connection, login and saving show an error and a
retry. Offline queue + background sync is the next step.

## Honest caveats (read these)

- **Not compiled here** — expect a small fix or two on first sync in Android Studio.
  The `ksp` version must match the `kotlin` version.
- **The JWT is held in memory** — the token is lost when the app is killed, so the
  user logs in again next launch. Move to encrypted storage when hardening.
- **Real dialing is implemented.** Tapping Call requests the phone permissions
  (CALL_PHONE, READ_PHONE_STATE) the first time, places a real call, and captures
  the real start/end/duration from the device call state. After the call ends the
  app shows the disposition screen. Return-to-app behaviour after a call varies by
  OEM (Samsung/Xiaomi/etc.) and may need on-device tuning; if a phone doesn't
  auto-return, reopening the app from recents still shows the disposition.
- **Owner home shows 0 for today's counts** — the structure endpoint doesn't carry
  counts; wiring the branch-summary endpoint into the owner screen is a small
  follow-up.
- **Recording is not here** — by design (v1 is metadata-only). `CallRecord` carries
  a nullable `recordingUrl` for later.

## What's next

- Real backend (auth + persistence) behind `CallingRepository`.
- Real dialing + call-state capture.
- Offline queue + background sync (WorkManager).
- Owner side: date-by-date call review and full cities/branches/staff management.
