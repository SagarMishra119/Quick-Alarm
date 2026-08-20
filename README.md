# Quick Alarm v3.2

A clean, modern, lightweight, and 100% offline Android alarm and quick-timer application built with **Kotlin** and **Jetpack Compose**. It allows users to set timers and alarms in a single tap with custom presets, stored fixed clock alarms, home screen widget, customizable alarm audio from device library, dynamic Light & Dark mode adaptation, and configurable snooze intervals.

---

## 📊 Version Comparison (v1.0 vs v2.0 vs v3.0 vs v3.2)

| Feature / Capability | Quick Alarm v1.0 | Quick Alarm v2.0 | Quick Alarm v3.0 | Quick Alarm v3.2 (Latest) |
| :--- | :--- | :--- | :--- | :--- |
| **System Light & Dark Mode** | Dark only | Dark only | Dark only | **✅ Dynamic System Adaptive**: Auto-switches between Soft Slate Dark & Clean Modern Light |
| **Interactive Home Widget** | ❌ None | ❌ None | ✅ Added | **✅ 4x2 / 4x1 Home Screen Widget** (1-tap preset alarm scheduling + live status badge) |
| **Saved Clock Alarms** | ❌ None | ❌ None | ✅ Added | **✅ Up to 10 Saved Daily Alarms** (Empty by default, toggle switches, AM/PM time pickers) |
| **Custom Countdown Timer** | Basic picker | Basic picker | Basic button | **✅ Precision Aligned Button & Dialog** (e.g. +45m from now with exact ±1m steppers & sliders) |
| **Device Sound Library** | Default sound only | Single custom pick | Synchronous query | **✅ Full OEM System Alarm Library** (Scans all phone ringtones asynchronously on background thread) |
| **One-Tap Presets** | Fixed 6 presets | Customizable presets | Up to 10 presets | **✅ Up to 10 Presets** with ±1m/±5m steppers, 0-59 sliders, dynamic titles & 10 theme colors |
| **Performance & Smoothness** | Basic | Periodic 1s disk I/O | Fixed ticker | **✅ 120 FPS Zero-Lag**: Isolated clock recomposition, zero main-thread I/O |
| **List Key Safety** | Basic | Risk of conflict | Basic | **✅ Bulletproof Namespaced Keys**: Completely eliminates list key collisions and startup crashes |
| **Audio Engine** | Dual-sound collision | Single-source audio | Single-source audio | **✅ Silent Notification Channel + looper MediaPlayer** in `AlarmActivity` |
| **Local Offline Storage** | In-memory only | Basic preferences | SharedPreferences | **✅ Robust `SharedPreferences`** with zero external network or Firebase dependencies |

---

## 🌟 What's New in v3.2

- **🌓 Dynamic System Light & Dark Mode Adaptation:**
  - **Dark Mode:** Refined soft-slate palette (`#111827`), reducing eye strain and improving contrast.
  - **Light Mode:** Modern crisp off-white background (`#F8FAFC`), elevated cards (`#FFFFFF`), and deep slate typography (`#0F172A`).
  - Automatic status bar & navigation bar theme adaptation.
- **📐 Precision Alignment for Custom Countdown Timer:**
  - Standardized horizontal margins and internal text flex weighting so the `"Pick Timer"` badge aligns with the preset cards grid.
- **🛡️ Crash Resilience & Key Isolation:**
  - Namespaced list keys (`"active_${id}"`, `"saved_${id}"`) preventing duplicate key exceptions and startup failures.
- **⚡ 120 FPS Zero-Lag Smoothness:**
  - Isolated clock ticker recomposition inside `HeaderClockSection`.
  - Non-blocking background sound loader on `Dispatchers.IO`.

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
│   │   ├── MainScreen.kt         # [UPDATED v3.2] Adaptive Light/Dark layout, aligned timer button
│   │   ├── SavedAlarmDialog.kt   # [UPDATED v3.2] Modal AM/PM clock time picker for saved alarms
│   │   ├── CustomDurationDialog.kt # [UPDATED v3.2] Aligned Custom Countdown Timer dialog
│   │   ├── SoundPickerDialog.kt  # [UPDATED v3.2] Non-blocking async sound loader & preview player
│   │   ├── SnoozeDurationDialog.kt # [UPDATED v3.2] Snooze preset chips & 1-60m stepper slider
│   │   ├── PresetManageDialog.kt # [UPDATED v3.2] Reorder (up/down), edit, delete, and reset presets
│   │   ├── PresetEditDialog.kt   # [UPDATED v3.2] Dynamic title auto-sync, 1m/5m steppers & color picker
│   │   └── PermissionBanner.kt   # Android 13+ Notification & Exact Alarm permission cards
│   └── theme/
│       ├── Color.kt              # [UPDATED v3.2] Adaptive DarkAppPalette & LightAppPalette
│       ├── Theme.kt              # [UPDATED v3.2] Material 3 dynamic theme provider & status bar insets
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

The generated APK is output at `QuickAlarmv3.2.apk` in the root directory and `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📜 Permissions Used

- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: Triggers alarms at exact target times via `AlarmManager`.
- `POST_NOTIFICATIONS`: Displays heads-up alarm alerts on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: Reschedules active alarms after device reboots via `BootReceiver`.
- `VIBRATE`: Vibration feedback when alarms ring.
- `WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`: Wakes up the screen and displays over the lock screen.
