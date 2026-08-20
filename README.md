# Quick Alarm v3.3

A clean, modern, lightweight, and 100% offline Android alarm and quick-timer application built with **Kotlin** and **Jetpack Compose**. It allows users to set timers and alarms in a single tap with custom presets, stored fixed clock alarms, home screen widget, customizable alarm audio from device library, dynamic Light & Dark mode adaptation, and configurable snooze intervals.

---

## 📊 Version Comparison (v1.0 vs v2.0 vs v3.0 vs v3.3)

| Feature / Capability | Quick Alarm v1.0 | Quick Alarm v2.0 | Quick Alarm v3.0 | Quick Alarm v3.3 (Latest) |
| :--- | :--- | :--- | :--- | :--- |
| **System Light & Dark Mode** | Dark only | Dark only | Dark only | **✅ Dynamic System Adaptive**: Lighter Dark Slate Gradient + Vibrant Sky-Blue Light Gradient |
| **Section Headings Visibility** | Basic | Basic | Muted grey | **✅ High-Contrast**: Crisp `#FFFFFF` headers in Dark Mode, Deep `#0F172A` headers in Light Mode |
| **Preset Icon & Text Layout** | Basic | Basic | Dual plus signs | **✅ Zero Overlap Single Alarm Icon**: Ellipsis flex-weighting & clean single icon badge |
| **Interactive Home Widget** | ❌ None | ❌ None | ✅ Added | **✅ 4x2 / 4x1 Home Screen Widget** (1-tap preset alarm scheduling + live status badge) |
| **Saved Clock Alarms** | ❌ None | ❌ None | ✅ Added | **✅ Up to 10 Saved Daily Alarms** (Empty by default, toggle switches, AM/PM time pickers) |
| **Custom Countdown Timer** | Basic picker | Basic picker | Basic button | **✅ Precision Aligned Button & Dialog** (e.g. +45m from now with exact ±1m steppers & sliders) |
| **Device Sound Library** | Default sound only | Single custom pick | Synchronous query | **✅ Full OEM System Alarm Library** (Scans all phone ringtones asynchronously on background thread) |
| **One-Tap Presets** | Fixed 6 presets | Customizable presets | Up to 10 presets | **✅ Up to 10 Presets** with ±1m/±5m steppers, 0-59 sliders, dynamic titles & 10 theme colors |
| **Performance & Smoothness** | Basic | Periodic 1s disk I/O | Fixed ticker | **✅ 120 FPS Zero-Lag**: Isolated clock recomposition, zero main-thread I/O |
| **List Key Safety** | Basic | Risk of conflict | Basic | **✅ Bulletproof Namespaced Keys**: Completely eliminates list key collisions and startup crashes |

---

## 🌟 What's New in v3.3

- **🌙 Lighter Dark Slate Background & High-Contrast Pure White Headers:**
  - Noticeably lighter dark slate gradient (`#293548` ➔ `#1E293B` ➔ `#141D2B`).
  - Section headings in Dark Mode are crisp pure white (`#FFFFFF`) for instant readability against the background.
- **☀️ Vibrant Sky-Blue Light Mode Gradient:**
  - Soft sky-blue gradient (`#BAE6FD` ➔ `#E0F2FE` ➔ `#F0F9FF`) with bold deep-slate `#0F172A` headings.
- **🎯 Clean Preset Cards (Fixed Overlap & Removed Redundant Plus Signs):**
  - Replaced the old dual `+` icon badge with a single, clean `Icons.Default.Alarm` icon.
  - Added flex-weighting (`Modifier.weight(1f, fill = false)`) with `maxLines = 1` and `TextOverflow.Ellipsis` to guarantee zero title/icon overlap across all device screens.
  - Removed duplicate `+` prefixes from modal action buttons (e.g. `"Add Preset"`, `"Add Alarm"`).
- **🧹 Active Alarm Dismissal Cleanup:**
  - Guaranteed automatic lifecycle cleanup in `AlarmActivity` on dismiss and snooze.

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
│   │   ├── MainScreen.kt         # [UPDATED v3.3] Lighter dark slate, pure white headers, clean preset cards
│   │   ├── SavedAlarmDialog.kt   # Modal AM/PM clock time picker for saved alarms
│   │   ├── CustomDurationDialog.kt # Aligned Custom Countdown Timer dialog
│   │   ├── SoundPickerDialog.kt  # Non-blocking async sound loader & preview player
│   │   ├── SnoozeDurationDialog.kt # Snooze preset chips & 1-60m stepper slider
│   │   ├── PresetManageDialog.kt # [UPDATED v3.3] Clean 'Add Preset' action & reorder controls
│   │   ├── PresetEditDialog.kt   # Dynamic title auto-sync, 1m/5m steppers & color picker
│   │   └── PermissionBanner.kt   # Android 13+ Notification & Exact Alarm permission cards
│   └── theme/
│       ├── Color.kt              # [UPDATED v3.3] Lighter dark palette & pure-white dark headers
│       ├── Theme.kt              # Material 3 dynamic theme provider & status bar insets
│       └── Type.kt               # Typography definitions
├── util/
│   ├── AlarmScheduler.kt         # Silent notification channel + Exact AlarmManager logic
│   └── AppSettings.kt            # Offline manager for presets, sounds & saved alarms
└── widget/
    └── QuickAlarmWidgetProvider.kt # AppWidgetProvider handling 1-tap home screen triggers
```

---

## 🚀 Steps to Build & Run

### Build via Gradle Command Line
- **Windows (PowerShell):**
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Unity\Hub\Editor\6000.3.8f1\Editor\Data\PlaybackEngines\AndroidPlayer\OpenJDK"
  .\gradlew.bat assembleDebug
  ```
- **macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```

The generated APK is output at `QuickAlarmv3.3.apk` in the root directory and `app/build/outputs/apk/debug/app-debug.apk`.
