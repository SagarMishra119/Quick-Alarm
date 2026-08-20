# Google Play Console Store Listing & Setup Guide 🚀

This document contains the exact metadata, copy, answers, and steps required when publishing **Quick Alarm** on the **Google Play Console**.

---

## 📌 1. App Store Listing Metadata

### App Title (Max 30 characters)
```
Quick Alarm - Fast Timer
```

### Short Description (Max 80 characters)
```
Instant 1-tap alarm presets, countdowns & saved alarms. 100% offline & ad-free.
```

### Full Description (Max 4000 characters)
```
⏰ Quick Alarm — The Fastest, 100% Offline Alarm & Quick-Timer for Android.

Quick Alarm is built for people who want instant action without navigating through complicated menus. Set quick timers in a single tap, manage daily clock alarms, and wake up reliably with a modern, distraction-free design.

🌟 KEY FEATURES:

⚡ Instant One-Tap Presets:
• Schedule alarms in a fraction of a second (+15m, +30m, +1h, +2h, +4h, +6h).
• Reorder, customize, and color-theme up to 10 personalized presets for naps, cooking, workouts, and focus sessions.

⏰ Saved Daily Clock Alarms:
• Save up to 10 fixed time-of-day daily alarms (e.g., 07:00 AM, 11:30 PM).
• Instant 1-tap ON/OFF toggle switches and quick AM/PM time selector.

⏳ Custom Countdown Timer:
• Set exact countdown durations from right now (e.g., +45m) with precision sliders and ±1m / ±5m step adjusters.
• Smart auto-labeling automatically names alarms based on time of day (Early Morning, Morning, Afternoon, Evening, Late Night).

🌙 Dual Aesthetic Themes:
• Dark Mode: Full moon and starry night sky with frosted glassmorphism surfaces.
• Light Mode: Early morning golden dawn sunrise with high-contrast daylight visibility.
• Automatically follows your device's system theme.

🎵 Full Sound & Ringtone Library:
• Scan and select from your device's pre-installed ringtones and alarm sounds.
• Select custom audio files (MP3, WAV, FLAC, OGG, AAC) with in-app audio preview.
• Single-source audio engine ensures loud, reliable ringing without double-sound collisions.

💤 Configurable Snooze:
• Customizable snooze duration (from 5 to 30 minutes) right from the ringing screen or notification.

🔒 100% Offline & Private:
• No accounts, logins, or tracking.
• Zero internet connectivity required. Your data stays entirely on your phone.

Download Quick Alarm today and experience the fastest, cleanest alarm app for Android!
```

---

## 📋 2. Google Play Console Form Answers

### App Category & Contact Details
* **App category:** `Applications` ➔ `Tools` or `Productivity`
* **Tags:** `Alarm`, `Clock`, `Timer`, `Productivity`, `Utilities`
* **Contact email:** Your developer email address

### Content Rating (Questionnaire)
* **Violence, Sex, Profanity, Drugs:** Select **No** for all questions.
* **Resulting Rating:** **PEGI 3 / Everyone (All ages)**.

### Target Audience & Content
* **Target age group:** Select **13 and older** (or 18+).
* **Contains Ads:** Select **No**.

### Data Safety Form (Mandatory)
* **"Does your app collect or share any of the required user data types?"** ➔ Select **No**.
* **"Is all user data collected by your app encrypted in transit?"** ➔ Select **Not applicable (No data collected)**.
* **"Can users request data deletion?"** ➔ Select **Yes (User can uninstall app to erase all local data)**.

### Exact Alarm Permission Declaration (`USE_EXACT_ALARM`)
* **Primary Feature:** Select **Clock / Alarm / Timer Application**.
* **Justification Description:**
```
Quick Alarm is a dedicated alarm clock and timer utility. The USE_EXACT_ALARM permission is essential for its core functionality to wake the device and sound alarms at the user's exact requested time, including countdown timers and scheduled daily alarms.
```

### Foreground Service Declaration (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`)
* **Service Type:** `Media Playback`
* **Purpose:**
```
Used exclusively by AlarmSoundService to play the user's selected alarm audio and vibration continuously when an alarm triggers, ensuring the alarm sounds reliably even if the app is in the background or the screen is locked.
```

---

## 🖼️ 3. Graphic Assets Checklist

| Asset | Dimensions | Format | Notes |
| :--- | :--- | :--- | :--- |
| **App Icon** | 512 x 512 px | PNG (Max 1 MB) | High-res launcher icon |
| **Feature Graphic** | 1024 x 500 px | PNG / JPEG (Max 15 MB) | Banner displayed at top of Play Store listing |
| **Phone Screenshots** | Min 1080 x 1920 px | PNG / JPEG | Minimum 2 screenshots showing MainScreen (Dark & Light modes) and Alarm Ringing screen |

---

## 🚀 4. Step-by-Step Publishing Steps (When Ready)

1. **Purchase Developer Account:**
   * Go to [https://play.google.com/console](https://play.google.com/console) and complete registration ($25 one-time fee).
2. **Create New App:**
   * Click **"Create App"**, enter Name: `Quick Alarm`, Default Language: `English`, Free app.
3. **Complete Store Listing:**
   * Copy and paste metadata from Section 1 above.
   * Upload Icon, Feature Graphic, and Screenshots.
4. **Complete Policy & Declarations:**
   * Privacy Policy URL: Link your hosted `PRIVACY_POLICY.html` on GitHub.
   * Fill out Data Safety & Exact Alarm declarations using Section 2 above.
5. **Upload Production Release:**
   * Navigate to **Production** ➔ **Create new release**.
   * Upload [**`QuickAlarmv3.4-release.apk`**](file:///D:/Mini%20Projects/QuickAlarm/QuickAlarmv3.4-release.apk) *(2.49 MB)*.
   * Enter Release Notes: `v3.4.0 - Initial Production Release`.
6. **Submit for Review:**
   * Review summary and click **"Send for review"**. Review usually takes 2–4 business days.
