# Changelog

All notable changes to **Quick Alarm** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.4.0] - 2026-08-20

### Added
- **🌙 Full Moon & Starry Night Sky Dark Mode Background:** High-definition moonlit starry night sky wallpaper (`bg_dark_mode.png`) with an ambient protective scrim to preserve 100% legibility of all cards, icons, and text.
- **☀️ Early Morning Dawn & Sunrise Light Mode Aesthetic:** Dynamic golden dawn sun glow radiating across the top-right header, gracefully harmonizing with the crisp sky-blue gradient.
- **🔔 Single-Source Foreground Alarm Sound Service:** Added `AlarmSoundService` (with `mediaPlayback` foreground type) as the unified single sound source for loud continuous audio playback, repeating haptic vibration, and wake-lock, guaranteeing reliable ringing even when the app is completely closed or killed.
- **🔄 Live State & Snooze Synchronization:** Added `ON_RESUME` lifecycle observer and dynamic background polling in `MainScreen` so snoozed alarms and external updates appear instantly on the dashboard without requiring app restart.
- **🕒 Smart Automatic Time-of-Day Labeling:** Dynamically auto-generates context-aware alarm names based on the target trigger hour (e.g. `12 AM - 4:59 AM` ➔ *"Late Night Alarm"*, `5 AM - 7:59 AM` ➔ *"Early Morning Alarm"*, `8 AM - 11:59 AM` ➔ *"Morning Alarm"*, `12 PM - 4:59 PM` ➔ *"Afternoon Alarm"*, `5 PM - 8:59 PM` ➔ *"Evening Alarm"*, `9 PM - 11:59 PM` ➔ *"Night Alarm"*).
- **✨ Frosted Glassmorphism Theme System:** Translucent card surfaces with elevated subtle borders letting the atmospheric background glow through while maintaining high contrast.

### Fixed & Improved
- **Single-Source Audio Architecture:** Delegated all audio playback exclusively to `AlarmSoundService`, eliminating dual-sound overlap when notifications are active.
- **Widget Deprecation:** Removed the home screen AppWidget module for cleaner standalone app operation.
- **Header Simplification:** Removed the `"100% Offline"` pill badge from the header clock section for a cleaner visual layout.
- **Clean Countdown Labeling:** Removed the redundant `"+"` symbol from `"Custom Countdown Timer"`.
- **Zero Stepper Button Overlap:** Completely redesigned stepper rows (`-5m`, `-1m`, `+1m`, `+5m` and `-1h`, `+1h`) across all modal dialogs (`CustomDurationDialog`, `SavedAlarmDialog`, `PresetEditDialog`, `SnoozeDurationDialog`) onto dedicated full-width weighted grid rows, mathematically eliminating all label and symbol collisions across all screen densities.
- **Theme Icon Matching:** Dynamic sun icon in Light Mode and moon night icon in Dark Mode inside the main header clock card.

---

## [3.3.0] - 2026-08-20

### Added
- **Lighter Dark Slate Gradient Background:** Soft modern slate gradient (`#293548` ➔ `#1E293B` ➔ `#141D2B`) to enhance comfort and reduce eye strain.
- **Pure-White High-Contrast Section Headers:** Crisp `#FFFFFF` styling for all major section titles (`ONE-TAP PRESETS`, `SAVED CLOCK ALARMS`, `ACTIVE ALARMS`, `PREFERENCES & SETTINGS`) in Dark Mode for immediate readability.
- **Vibrant Sky-Blue Light Mode Gradient:** Soft sky-blue gradient (`#BAE6FD` ➔ `#E0F2FE` ➔ `#F0F9FF`) paired with deep `#0F172A` headings.

### Fixed & Improved
- **Preset Button Icon Polish:** Replaced duplicate `+` icon badge with a single, clean `Icons.Default.Alarm` icon.
- **Preset Text Overlap Resolution:** Added flex-weighting (`Modifier.weight(1f, fill = false)`) with `maxLines = 1` and `TextOverflow.Ellipsis` to guarantee zero title/icon collision on compact displays.
- **Dialog Action Buttons:** Standardized modal button text to `"Add Preset"` and `"Add Alarm"` (eliminating duplicate `+` prefixes).
- **Alarm Dismissal Lifecycle:** Guaranteed automatic cleanup (`AlarmScheduler.removeAlarm`) on dismiss or snooze in `AlarmActivity`.

---

## [3.2.0] - 2026-08-20

### Added
- **Dynamic System Light & Dark Mode:** Real-time automatic detection and switching based on Android device system appearance with status bar and navigation bar insets management (`isAppearanceLightStatusBars`).
- **Precision Alignment in Custom Countdown Timer:** Left-column flex weighting and centered `"Pick Timer"` badge alignment matching the preset cards grid.

### Changed
- All modal dialogs (`CustomDurationDialog`, `SavedAlarmDialog`, `SoundPickerDialog`, `SnoozeDurationDialog`, `PresetManageDialog`, `PresetEditDialog`) adapted to dynamic theme tokens.

---

## [3.1.0] - 2026-08-20

### Fixed
- **Startup Crash Resolution:** Fixed duplicate key exception (`IllegalArgumentException`) in Jetpack Compose `LazyColumn` by namespacing all item keys (`"active_${id}"`, `"saved_${id}"`) and generating unique IDs for triggered saved alarms.
- **Zero-Lag Clock Recomposition (120 FPS):** Confined live 1-second ticker logic strictly inside `HeaderClockSection` and `ActiveAlarmCard` to prevent the parent `MainScreen` from recomposing every second.
- **Non-Blocking Sound Query:** Offloaded OEM device ringtone discovery in `SoundPickerDialog` to `Dispatchers.IO`.

### Changed
- Clarified UI terminology distinction between `"SAVED CLOCK ALARMS (e.g. 7:00 AM)"` and `"+ Custom Countdown Timer (+45m from now)"`.

---

## [3.0.0] - 2026-08-20

### Added
- **Saved Fixed Daily Clock Alarms:** Storage for up to 10 daily fixed-time alarms (e.g. `07:00 AM`, `11:30 PM`) with instant ON/OFF toggle switches and AM/PM time selector (empty by default).
- **OEM Device Sound Library Explorer:** Support for scanning all pre-installed phone ringtones via `RingtoneManager` plus custom audio file picker (`audio/*` MP3, WAV, FLAC, OGG, AAC) with in-app preview playback.

---

## [2.0.0] - 2026-08-20

### Added
- **Customizable Presets System:** Reorder (move up/down), add, edit, and delete presets (up to 10 total) with 10 custom color themes and dynamic title generation.
- **Persistent Offline Storage:** Settings, custom presets, selected sounds, and snooze durations stored in local `SharedPreferences`.
- **Configurable Snooze:** Snooze interval selector with quick chips (1 to 60 minutes).
- **Single-Source Audio Engine:** Eliminated dual-sound overlap by using silent notification channels and a dedicated looping `MediaPlayer` in `AlarmSoundService`.

---

## [1.0.0] - 2026-08-19

### Initial Release
- Instant one-tap alarms with 6 fixed presets (`+15m`, `+30m`, `+1h`, `+2h`, `+4h`, `+6h`).
- Basic countdown dialog picker.
- Exact alarms via Android `AlarmManager`.
- Lock screen waking alarm activity with full-screen intent.
- 100% offline and standalone operation.
