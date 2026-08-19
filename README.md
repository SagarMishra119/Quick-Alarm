# Quick Alarm

A clean, modern, and lightweight Android alarm application built with Jetpack Compose and Kotlin. It allows users to quickly set timers and alarms with preset intervals or custom durations, featuring full-screen alarm alerts and system notification integration.
**APK Output / Download Location:** `app/build/outputs/apk/debug/app-debug.apk`
---

## 📱 Features

- **Quick Presets:** One-tap alarm creation (e.g., 5 min, 10 min, 15 min, 30 min, 1 hour).
- **Custom Duration:** Set flexible alarms for custom hours and minutes.
- **Full-Screen Alarm Alert:** High-visibility wake-up screen (`AlarmActivity`) with sound, vibration, and dismissal options.
- **Reliable Scheduling:** Uses Android's `AlarmManager` with exact alarm capabilities (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`).
- **Boot Persistence:** Automatically restores scheduled alarms after device reboots using `BootReceiver`.
- **Modern UI:** Designed with Jetpack Compose, Material 3, and dynamic color theming.

---

## 🛠️ Prerequisites & Requirements

Before building or running the project, make sure you have:

- **Android Studio:** Hedgehog (2023.1.1) or newer
- **JDK:** Java Development Kit (JDK) 17
- **Android SDK:**
  - **Compile SDK:** 34 (Android 14)
  - **Target SDK:** 34
  - **Min SDK:** 26 (Android 8.0 Oreo)
- **Kotlin:** 1.9+
- **Gradle:** Compatible with Android Gradle Plugin 8.3+

---

## 🚀 Steps to Build & Run the App

### 1. Open the Project
1. Launch **Android Studio**.
2. Select **Open** and select the project root directory.
3. Allow Android Studio to complete Gradle sync and download required dependencies.

### 2. Run on Device / Emulator
1. Connect an Android device with **USB Debugging enabled** or start an Android Virtual Device (AVD / Emulator) running Android 8.0+.
2. Select the `app` run configuration in the top toolbar.
3. Click the **Run ▶** button (or press `Shift + F10`).

### 3. Build APK via Command Line
You can also build the APK using the Gradle wrapper:

- **Windows (PowerShell / CMD):**
  ```bash
  .\gradlew.bat assembleDebug
  ```
- **macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```

---

## 🏗️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Architecture:** MVVM (Model-View-ViewModel) pattern
- **Android Jetpack Components:**
  - `AlarmManager` for precise time triggers
  - `BroadcastReceiver` (`AlarmReceiver`, `BootReceiver`)
  - `Activity` (`MainActivity`, `AlarmActivity`)
  - `ViewModel` & Kotlin Coroutines

---

## 📜 Permissions Used

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: To trigger alarms at exact target times.
- `POST_NOTIFICATIONS`: For displaying alarm notifications on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: To reschedule active alarms after the phone restarts.
- `VIBRATE`: For vibration feedback when the alarm fires.
- `WAKE_LOCK`: To wake the screen when an alarm triggers.
