# Quick Alarm v3.1

A clean, modern, lightweight, and 100% offline Android alarm and quick-timer application built with **Kotlin** and **Jetpack Compose**. It allows users to set timers and alarms in a single tap with custom presets, stored fixed clock alarms, home screen widget, customizable alarm audio from device library, and configurable snooze intervals.

---

## 📊 Version Comparison (v1.0 vs v2.0 vs v3.1)

| Feature / Capability | Quick Alarm v1.0 | Quick Alarm v2.0 | Quick Alarm v3.1 (Latest) |
| :--- | :--- | :--- | :--- |
| **Interactive Home Widget** | ❌ None | ❌ None | **✅ 4x2 / 4x1 Home Screen Widget** (1-tap preset alarm scheduling + live status badge) |
| **Saved Clock Alarms** | ❌ None | ❌ None | **✅ Up to 10 Saved Daily Alarms** (Empty by default, toggle switches, AM/PM time pickers) |
| **Custom Countdown Timer** | Basic picker | Basic picker | **✅ Dedicated Relative Timer Button** (e.g. +45m from now with exact ±1m steppers & sliders) |
| **Device Sound Library** | Default sound only | Single custom audio pick | **✅ Full OEM System Alarm Library** (Scans all phone ringtones asynchronously on background thread) |
| **One-Tap Presets** | Fixed 6 presets | Customizable presets | **✅ Up to 10 Presets** with ±1m/±5m steppers, 0-59 sliders, dynamic titles & 10 theme colors |
| **Performance / Lag Fix** | Basic | Periodic 1s disk I/O | **✅ 120 FPS Zero-Lag**: Isolated clock recomposition and non-blocking background sound loading |
| **Stability & Key Safety** | Basic | Risk of key conflict | **✅ Bulletproof Namespaced Keys**: Completely eliminates list key collisions and startup crashes |
| **Snooze Customization** | Fixed at 5 minutes | Basic modal | **✅ 1m–60m Stepper + Slider** with responsive scrollable layout |
| **Audio Engine** | Dual-sound collision | Single-source audio | **✅ Silent Notification Channel + looper MediaPlayer** in `AlarmActivity` |
| **Local Offline Storage** | In-memory only | Basic preferences | **✅ Robust `SharedPreferences`** with zero external network or Firebase dependencies |

---

## 🌟 What's New in v3.1

- **🛡️ Crash Fix & Startup Resilience:**
  - Resolved `LazyColumn` key collision between active alarms and saved fixed alarms.
  - Namespaced all composable list keys (`"active_${id}"`, `"saved_${id}"`) to prevent any duplicate key exceptions.
- **⚡ 120 FPS Smoothness & Zero-Lag:**
  - Recomposition isolation: the live 1-second clock ticker now executes strictly inside `HeaderClockSection` without causing the whole `MainScreen` to recompose.
  - Background audio loading: OEM system alarm tones are scanned asynchronously on `Dispatchers.IO`.
- **🏷️ Clear Visual Distinction:**
  - **Saved Clock Alarms:** Set a fixed daily clock time (e.g. `7:00 AM`, `11:00 PM`).
  - **Custom Countdown Timer:** Set a relative duration starting from right now (e.g. `+45m`, `+1h 30m`).
- **📱 Interactive Android Home Screen Widget (`QuickAlarmWidget`):**
  - Instant one-tap alarms directly from your home screen.
  - Tapping the widget header launches the full app.

---

## 📁 Key File Changes & Architecture

```
app/src/main/java/com/quickalarm/app/
├── model/
│   ├── AlarmItem.kt              # Active alarm data model (JSON serializable)
│   ├── PresetItem.kt             # Customizable preset model (10 colors, titles, minutes)
│   ├── SavedAlarmItem.kt         # Stored fixed clock alarm model with next trigger helper
│   └── SoundItem.kt              # OEM RingtoneManager scanner & audio URI holder
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt         # Zero-lag ticker, Saved Alarms list & Widget sync
│   │   ├── SavedAlarmDialog.kt   # Modal AM/PM clock time picker for saved alarms
│   │   ├── CustomDurationDialog.kt # Custom Countdown Timer dialog with 1m steppers & slider
│   │   ├── SoundPickerDialog.kt  # Non-blocking async sound loader & preview player
│   │   ├── SnoozeDurationDialog.kt # Snooze preset chips & 1-60m stepper slider
│   │   ├── PresetManageDialog.kt # Reorder (up/down), edit, delete, and reset presets
│   │   ├── PresetEditDialog.kt   # Dynamic title auto-sync, 1m/5m steppers & color picker
│   │   └── PermissionBanner.kt   # Android 13+ Notification & Exact Alarm permission cards
│   └── theme/
│       ├── Color.kt              # Color palettes & gradients
│       ├── Theme.kt              # Material 3 dark theme setup
│       └── Type.kt               # Typography definitions
├── util/
│   ├── AlarmScheduler.kt         # Silent notification channel + Exact AlarmManager logic
│   └── AppSettings.kt            # Offline manager for presets, sounds & saved alarms
└── widget/
    └── QuickAlarmWidgetProvider.kt # AppWidgetProvider handling 1-tap home screen triggers
```

---

## 🛠️ Prerequisites & Requirements

- **Android Studio:** Hedgehog (2023.1.1) or newer
- **JDK:** Java Development Kit (JDK) 17
- **Android SDK:**
  - **Compile SDK:** 34 (Android 14)
  - **Target SDK:** 34
  - **Min SDK:** 26 (Android 8.0 Oreo)
- **Kotlin:** 1.9+
- **Gradle:** 8.7+

---

## 🚀 Steps to Build & Run

### 1. Build via Gradle Command Line
- **Windows (PowerShell):**
  ```bash
  .\gradlew.bat assembleDebug
  ```
- **macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```

The generated APK is output at `QuickAlarmv3.1.apk` in the root directory and `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📜 Permissions Used

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Triggers alarms at exact target times via `AlarmManager`.
- `POST_NOTIFICATIONS`: Displays heads-up alarm alerts on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: Reschedules active alarms after device reboots via `BootReceiver`.
- `VIBRATE`: Vibration feedback when alarms ring.
- `WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`: Wakes up the screen and displays over the lock screen.
