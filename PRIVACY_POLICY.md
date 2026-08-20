# Privacy Policy for Quick Alarm

**Last updated:** August 20, 2026

**Quick Alarm** ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains our practices regarding data collection, usage, and disclosure for the **Quick Alarm** mobile application.

---

## 1. 100% Offline Application
Quick Alarm is designed from the ground up as a **100% offline, standalone utility application**. 
* We do **NOT** require an account, email address, or login.
* We do **NOT** collect, transmit, store, or sell any personal information.
* We do **NOT** use any third-party tracking, analytics, or advertising SDKs.
* We do **NOT** connect to external servers or transmit telemetry data over the internet.

---

## 2. Device Permissions Used & Purpose

Quick Alarm requests minimal Android device permissions solely to provide its core alarm and quick-timer functionality:

| Permission | Purpose & Usage |
| :--- | :--- |
| **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`** | Required to wake the device and trigger alarms with exact precision at the user's requested time. |
| **`POST_NOTIFICATIONS`** | Required on Android 13+ to display heads-up notifications when an alarm fires and to provide "Dismiss" and "Snooze" action buttons. |
| **`FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK`** | Used exclusively to play the chosen alarm sound and vibration continuously when an alarm triggers, even if the application is in the background or the screen is locked. |
| **`VIBRATE`** | Provides haptic vibration feedback when an alarm triggers. |
| **`WAKE_LOCK` / `USE_FULL_SCREEN_INTENT`** | Allows the application to wake the screen and display the full-screen alarm ringing interface over the lock screen when an alarm fires. |
| **`RECEIVE_BOOT_COMPLETED`** | Allows the application to automatically restore and reschedule your active and saved alarms after your device reboots. |

---

## 3. Local Data Storage
All user preferences (such as preset durations, custom alarm labels, selected ringtone preferences, and snooze intervals) are stored **locally on your device** using standard Android `SharedPreferences`. This data never leaves your device and is automatically erased if you uninstall the application.

---

## 4. Children's Privacy
Quick Alarm does not collect any personal data from anyone, including children under the age of 13.

---

## 5. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Any updates will be published on this page with a revised "Last updated" date.

---

## 6. Contact Us
If you have any questions or suggestions regarding this Privacy Policy, please contact the developer via GitHub:
**Repository:** [https://github.com/SagarMishra119/Quick-Alarm](https://github.com/SagarMishra119/Quick-Alarm)
