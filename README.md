# Quick Alarm v3

A clean, modern, lightweight, and 100% offline Android alarm and quick-timer application built with **Kotlin** and **Jetpack Compose**. It allows users to set timers and alarms in a single tap with custom presets, stored fixed clock alarms, home screen widget, customizable alarm audio from device library, and configurable snooze intervals.

---

## 📊 Version Comparison (v1.0 vs v2.0 vs v3.0)

| Feature / Capability | Quick Alarm v1.0 | Quick Alarm v2.0 | Quick Alarm v3.0 (Latest) |
| :--- | :--- | :--- | :--- |
| **Interactive Home Widget** | ❌ None | ❌ None | **✅ 4x2 / 4x1 Home Screen Widget** (1-tap preset alarm scheduling + status badge) |
| **Stored Fixed Alarms** | ❌ None | ❌ None | **✅ Up to 10 Saved Time Alarms** (Empty by default, toggle switches, AM/PM time pickers) |
| **Device Sound Library** | Default sound only | Single custom audio pick | **✅ Full OEM System Alarm Library** (Scans all pre-installed phone ringtones + local files) |
| **One-Tap Presets** | Fixed 6 presets | Customizable presets | **✅ Up to 10 Presets** with ±1m/±5m steppers, 0-59 sliders, dynamic titles & 10 theme colors |
| **Performance / Lag Fix** | Basic | Periodic 1s disk I/O | **✅ 120 FPS Buttery Smooth**: Removed all main-thread disk I/O & IPC from clock ticker loop |
| **Snooze Customization** | Fixed at 5 minutes | Basic modal | **✅ 1m–60m Stepper + Slider** with responsive scrollable layout |
| **Audio Engine** | Dual-sound collision | Single-source audio | **✅ Silent Notification Channel + looper MediaPlayer** in `AlarmActivity` |
| **Local Offline Storage** | In-memory only | Basic preferences | **✅ Robust `SharedPreferences`** with zero external network or Firebase dependencies |

---

## 🌟 What's New in v3.0

- **📱 Interactive Android Home Screen Widget (`QuickAlarmWidget`):**
  - Instant one-tap alarms directly from your home screen without opening the app.
  - Displays live active alarm status badge.
  - Tapping the widget header launches the full app.
- **🕒 Stored Fixed Alarms (Up to 10, Empty by Default):**
  - Save your favorite recurring daily times (e.g. `07:00 AM - Morning Workout`, `11:00 PM - Bedtime`).
  - Toggle switches on each card to enable/disable.
  - Interactive AM/PM time picker modal.
- **🎵 Full Device System Sound Library Explorer:**
  - Automatically queries all pre-installed manufacturer alarm ringtones (Pixel, Samsung, OnePlus, Xiaomi, etc.).
  - Pick and preview any local audio track (MP3, WAV, FLAC, OGG, AAC) from device storage.
- **⚡ Performance & Smoothness Optimization:**
  - Completely eliminated disk I/O and JSON parsing from the 1-second clock loop, ensuring smooth 120 FPS scrolling and zero frame drops.
- **✨ UI Beautification & Ergonomics:**
  - Modern card designs with neon accents, AMOLED deep background (`#0F172A`), and clear section hierarchy.

---

## 📁 Key File Changes & Architecture

```
app/src/main/java/com/quickalarm/app/
├── model/
│   ├── AlarmItem.kt              # Active alarm data model (JSON serializable)
│   ├── PresetItem.kt             # Customizable preset model (10 colors, titles, minutes)
│   ├── SavedAlarmItem.kt         # [NEW v3] Stored fixed clock alarm model with next trigger helper
│   └── SoundItem.kt              # [UPDATED v3] OEM RingtoneManager scanner & audio URI holder
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt         # [UPDATED v3] Zero-lag ticker, Saved Alarms list & Widget sync
│   │   ├── SavedAlarmDialog.kt   # [NEW v3] Modal AM/PM clock time picker for saved alarms
│   │   ├── CustomDurationDialog.kt # Exact 1-minute steppers & scrollable container
│   │   ├── SoundPickerDialog.kt  # [UPDATED v3] Full OEM system sound library & preview player
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
│   └── AppSettings.kt            # [UPDATED v3] Offline manager for presets, sounds & saved alarms
└── widget/
    └── QuickAlarmWidgetProvider.kt # [NEW v3] AppWidgetProvider handling 1-tap home screen triggers
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

The generated APK is output at `QuickAlarmv3.apk` in the root directory and `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📜 Permissions Used

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Triggers alarms at exact target times via `AlarmManager`.
- `POST_NOTIFICATIONS`: Displays heads-up alarm alerts on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: Reschedules active alarms after device reboots via `BootReceiver`.
- `VIBRATE`: Vibration feedback when alarms ring.
- `WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`: Wakes up the screen and displays over the lock screen.
