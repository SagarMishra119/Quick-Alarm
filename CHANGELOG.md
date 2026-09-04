# Changelog

All notable changes to **Quick Alarm** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.6.0] - 2026-08-26

### Added
- **🔁 Everyday & Custom Day-of-Week Recurring Alarms:** Integrated a full recurrence scheduler in `SavedAlarmDialog.kt` with 7 circular day chips (`[ S | M | T | W | T | F | S ]`) and quick presets (`Weekdays (M-F)`, `Weekends (S-S)`, `Everyday`). Automatically calculates and rearms the next matching occurrence after firing via `AlarmReceiver.kt`.
- **📅 Specific Calendar Date Alarms:** Added option to schedule alarms for exact future calendar dates (e.g. `📅 Oct 14, 2026`) via native DatePicker modal and quick date chips (`Tomorrow`, `This Weekend`). One-shot execution automatically deactivates after the target date.
- **🕒 Optimized Dial Sizing & Balanced Layout:** Proportionately scaled Hour (`64dp`) and Minute (`50dp`) dial radii to fit the recurrence tabs below the precision seconds selector with zero clipping or layout crowding.
- **🏷️ Dashboard Recurrence Badges:** Saved alarm cards on the main dashboard now display dynamic recurrence summaries (e.g. `• Weekdays`, `• Every day`, `• Mon, Wed, Fri`, `• 📅 Oct 14`).
- **🛡️ Backward-Compatible Data Model:** Updated `SavedAlarmItem` JSON serialization to seamlessly support `repeatDays` and `specificDateMillis` without breaking existing saved alarms.

### Changed
- **Version Code Bump:** Incremented `versionCode` to `14` and `versionName` to `3.6.0`.

---

## [3.5.4] - 2026-08-24

### Fixed & Improved
- **🎰 Separated Wheel Header Labels:** Moved `HOURS`, `MINS`, and `SECS` labels outside/above the tumbler box to prevent top fading numbers from colliding with text headers.
- **🎯 Dead-Center Digit & Colon Alignment:** Computed symmetrical item heights and content padding so digits sit dead-center in the frosted magnifier lens with baseline-aligned colons (`:`).
- **📌 OEM Status Bar Icon Guarantee:** Upgraded status channel to `IMPORTANCE_DEFAULT` with `CATEGORY_ALARM` and non-dismissible flags (`FLAG_NO_CLEAR`, `FLAG_ONGOING_EVENT`) to ensure Vivo, Xiaomi, and Samsung status bars reliably display the top app icon.
- **📱 Compact Dialog Spacing:** Optimized internal vertical padding across `PresetEditDialog` and `CustomDurationDialog` for a balanced, comfortable fit.

### Changed
- **Version Code Bump:** Incremented `versionCode` to `13` and `versionName` to `3.5.4`.

---

## [3.5.3] - 2026-08-23

### Fixed & Improved
- **🎰 Infinite Looping Casino Reels:** Re-architected `CasinoTimeTumbler.kt` with a continuous 360° virtual circular loop—scrolling past `59` or `23` wraps around seamlessly to `00` without hitting any end walls.
- **🎯 Pixel-Perfect Alignment & Centralization:** Fixed vertical snapping and geometric alignment across Hours, Minutes, and Seconds columns to ensure values align with the center glowing magnifier lens and divider colons.
- **📌 Persistent Status Bar Alarm Icon:** Restored non-intrusive, silent status indicator channel ensuring the top status bar app icon remains visible when alarms are armed across all Android OEM devices.

### Changed
- **Version Code Bump:** Incremented `versionCode` to `12` and `versionName` to `3.5.3`.

---

## [3.5.2] - 2026-08-23

### Added
- **🎰 Casino Slot Tumbler Duration Wheel:** Built `CasinoTimeTumbler.kt`—a tactile 3-column rolling wheel (`00..23h : 00..59m : 00..59s`) with momentum snapping, glowing center laser lens, and 1-tap quick jump chips for all custom timers and presets.
- **Notification Drawer Zero-Clutter:** Removed notification drawer cards while preserving Android's persistent native status bar alarm icon (next to Wi-Fi/Battery) via system `AlarmClockInfo`.
- **Liquid 12h Clock Dial & Top AM/PM Tab:** Refined `SavedAlarmDialog.kt` with a prominent 12-hour clock dial and liquid AM/PM segmented toggle tab.
- **Precision Seconds Tab Strip:** Seamless 1-tap second selection strip (`00s`, `15s`, `30s`, `45s`) across time-setting interfaces.

### Changed
- **Version Code Bump:** Incremented `versionCode` to `11` and `versionName` to `3.5.2`.

---

## [3.5.1] - 2026-08-23

### Added
- **Asymmetric Dual-Dial Layout:** Sized dominant Hour Dial (large ~`150dp`) and compact Minute Dial (~`120dp`) with genuine liquid glass translucency showing the dynamic app wallpaper beneath.
- **24-Hour Concentric Dials for Presets/Timers:** Enabled custom countdowns and presets up to `23h 59m 59s` via dual-concentric orbits (Outer: `0–11h`, Inner: `12–23h`).
- **Precision Seconds Selector:** Integrated a dedicated seconds selector bar allowing single-second precision setting across all timers and daily alarms.
- **Alarm Audio Loop Fix:** Resolved recent-app recreation bug in `AlarmActivity` to ensure dismissed alarms never restart audio when opening from background.
- **Broadcast Dismissal Sync:** Broadcasts dismissal across activities to close all background ringing instances cleanly.

### Changed
- **Version Code Bump:** Incremented `versionCode` to `10` and `versionName` to `3.5.1`.

---

## [3.5.0] - 2026-08-23

### Added
- **Liquid Dual Clock Dial Time Pickers:** Integrated lightweight, high-performance interactive circular clock dials (`DualClockDialPicker.kt`) for setting hours and minutes with smooth touch-drag rotation physics.
- **Top AM / PM Segmented Tab:** Clean liquid glass toggle tab for daily alarm configuration.
- **Emergency In-App Ringing Banner:** Added a high-priority pulsating emergency dismiss/snooze card on `MainScreen` allowing instant alarm shutoff even if notification permissions are disabled.
- **Auto-Redirect on App Resume:** `MainActivity` immediately redirects to `AlarmActivity` if opened while an alarm is ringing.
- **10-Minute Safety Auto-Silence:** Added automatic timeout in `AlarmSoundService` to silence unattended alarms and prevent battery drain.
- **Status Bar & Notification Shade Indicator:** Added dynamic status bar alarm icon and silent ongoing notification showing the next armed alarm time.

### Changed
- **Target SDK 36 (Android 16):** Upgraded `targetSdk` to API Level 36 for full Google Play forward compliance.
- **Version Code Bump:** Incremented `versionCode` to `9` and `versionName` to `3.5.0`.
- **Zero-Stepper UI Modernization:** Replaced old stepper buttons with precision dual circular dials and dynamic button titles across all time-setting dialogs.

---

## [Beta Testing] - 2026-08-22

### Ongoing Verification & Closed Testing Phase
- **Google Play Closed Testing Deployment:** Successfully rolled out v3.4.1 (Build 8) targeting Android 15 (API Level 35) to closed beta testers.
- **Hardware & Multi-OEM Testing:** Verified foreground alarm service (`AlarmSoundService`) reliability, audio loop playback, and full-screen intent wake-locks across diverse Android devices (Samsung OneUI, Xiaomi HyperOS/MIUI, OnePlus OxygenOS, and Google Pixel).
- **Tester Feedback Analysis:** Monitored background audio persistence, real-time snooze synchronization, and zero-overlap stepper row responsiveness.
- **Battery & Doze Mode Compliance:** Confirmed exact alarm trigger precision under deep Doze mode without battery drain.

---

## [3.4.1] - 2026-08-21

### Changed
- **Target SDK 35 (Android 15):** Upgraded `compileSdk` and `targetSdk` to API Level 35 to fully comply with Google Play's latest security, privacy, and performance standards.
- **Version Code Bump:** Incremented `versionCode` to `8` and `versionName` to `3.4.1` for production release.

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
