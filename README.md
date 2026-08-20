# Quick Alarm

A clean, modern, lightweight, and 100% offline Android alarm and quick-timer application built with **Kotlin** and **Jetpack Compose**. It allows users to set timers and alarms in a single tap with custom presets, custom durations, customizable alarm audio, and configurable snooze intervals.

---

## 📊 Version Comparison: v1.0 vs v2.0

| Feature / Capability | Quick Alarm v1.0 | Quick Alarm v2.0 (Latest) |
| :--- | :--- | :--- |
| **One-Tap Presets** | Fixed 6 presets (+15m, +30m, +1h, +2h, +4h, +6h) | **Fully customizable (Up to 10 presets)**: Add, edit, delete, reorder, and set custom durations (any exact minute). |
| **Preset Colors & Titles** | Hardcoded titles and styles | **10 theme colors**, dynamic real-time auto-updating titles, and custom labels. |
| **Alarm Sounds** | Default system sound only | **Customizable Audio**: System alarm, notification, ringtone, or custom audio files (MP3, WAV, OGG, FLAC) with live preview. |
| **Snooze Duration** | Fixed at 5 minutes | **Customizable Snooze**: Quick presets (1m, 3m, 5m, 10m, 15m, 20m, 30m) or 1–60 min custom slider/steppers. |
| **Audio Playback Engine** | Dual-sound collision between notifications & `AlarmActivity` | **Silent Notification Channel + Activity Audio**: Eliminates overlapping audio/echoes on lock screen. |
| **UI Dashboard Layout** | Active alarms positioned below presets | **Active Alarms Moved to Top**: Real-time countdowns & cancel controls visible immediately below the header clock. |
| **Preferences & Storage** | In-memory / basic alarm list | **Persistent Local Settings**: Sound, snooze, and presets saved offline via `AppSettings` and `SharedPreferences`. |

---

## 🌟 What's New in v2.0

- **Customizable Alarm Sounds:**
  - Built-in system sounds (System Alarm, System Notification, System Ringtone).
  - Pick custom audio tracks (MP3, WAV, OGG, FLAC) from device storage with live preview before saving.
- **Customizable Snooze Intervals:**
  - Preset snooze durations (1m, 3m, 5m, 10m, 15m, 20m, 30m) or custom intervals (1 - 60 minutes).
- **Preset Management (Up to 10 Quick Presets):**
  - Create, edit, delete, and reorder custom one-tap alarm presets with custom titles, labels, durations, and vibrant color themes.
  - One-tap "Reset to Defaults" option.
- **Optimized UI Layout (Active Alarms Moved Up):**
  - Active alarms and live countdowns are now positioned prominently right above the presets for instant visibility and fast cancellation.
- **Eliminated Dual-Sound Collision:**
  - Resolved the audio collision bug where notifications and `AlarmActivity` played two overlapping sounds when ringing while the phone screen was off.
- **100% Offline & Private:**
  - All preferences and alarms are persisted strictly in local `SharedPreferences`. No account, Firebase, or internet permissions required.

---

## 📁 Key File Changes & Architecture

```
app/src/main/java/com/quickalarm/app/
├── model/
│   ├── AlarmItem.kt              # Active alarm data model (JSON serializable)
│   ├── PresetItem.kt             # [NEW v2] Customizable preset model (colors, titles, minutes)
│   └── SoundItem.kt              # [NEW v2] Sound configuration & custom audio URI holder
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt         # [UPDATED v2] Restructured layout with Active Alarms on top & settings
│   │   ├── CustomDurationDialog.kt # [UPDATED v2] Exact 1-minute steppers & scrollable container
│   │   ├── SoundPickerDialog.kt  # [NEW v2] Audio file picker & preview player
│   │   ├── SnoozeDurationDialog.kt # [NEW v2] Snooze preset chips & 1-60m stepper slider
│   │   ├── PresetManageDialog.kt # [NEW v2] Reorder (up/down), edit, delete, and reset presets
│   │   ├── PresetEditDialog.kt   # [NEW v2] Custom title auto-sync, 1m/5m steppers & color picker
│   │   └── PermissionBanner.kt   # Android 13+ Notification & Exact Alarm permission cards
│   └── theme/
│       ├── Color.kt              # Color palettes & gradients
│       ├── Theme.kt              # Material 3 dark theme setup
│       └── Type.kt               # Typography definitions
└── util/
    ├── AlarmScheduler.kt         # [UPDATED v2] Silent notification channel + Exact AlarmManager logic
    └── AppSettings.kt            # [NEW v2] Offline SharedPreferences manager for sounds, snooze & presets
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

The generated APK is output at `QuickAlarmv2.apk` in the root directory and `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📜 Permissions Used

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Triggers alarms at exact target times via `AlarmManager`.
- `POST_NOTIFICATIONS`: Displays heads-up alarm alerts on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: Reschedules active alarms after device reboots via `BootReceiver`.
- `VIBRATE`: Vibration feedback when alarms ring.
- `WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`: Wakes up the screen and displays over the lock screen.
