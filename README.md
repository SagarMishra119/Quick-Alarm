# Quick Alarm App

## 📱 APK Installation & Download

The ready-to-install debug APK for testing on an Android device is located locally at:
```
app/build/outputs/apk/debug/app-debug.apk
```
* **Full Local Path:** `D:\Alaram\app\build\outputs\apk\debug\app-debug.apk`
* **Direct Install via ADB:**
  ```bash
  adb install app/build/outputs/apk/debug/app-debug.apk
  ```

---

## 🚀 How to Upload / Push This Project to GitHub

Follow these steps to upload this project to GitHub without affecting any project source files:

### 1. Create a New Repository on GitHub
1. Go to [GitHub - New Repository](https://github.com/new).
2. Enter a repository name (e.g. `quick-alarm-android`).
3. Set the repository to **Public** or **Private**.
4. **Do NOT check** "Add a README file", ".gitignore", or "Choose a license" (we already have local files).
5. Click **Create repository** and copy the repository URL (e.g., `https://github.com/<your-username>/quick-alarm-android.git`).

---

### 2. Initialize Git & Commit Local Files
Open PowerShell or your terminal in the project root directory (`D:\Alaram`) and run:

```bash
# Initialize git repository
git init

# Stage all project files (ignoring build artifacts automatically via .gitignore)
git add .

# Create initial commit
git commit -m "Initial commit - Quick Alarm App"
```

---

### 3. Link Remote Repository & Push
Run the following commands, replacing `<YOUR_GITHUB_REPO_URL>` with your repository URL:

```bash
# Rename default branch to main
git branch -M main

# Add GitHub remote origin
git remote add origin <YOUR_GITHUB_REPO_URL>

# Push code to GitHub
git push -u origin main
```

---

## 🛠️ Tech Stack & Requirements
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Target SDK:** Android 34 (Android 14)
- **Min SDK:** Android 26 (Android 8.0 Oreo)
- **Build System:** Gradle (Kotlin DSL)

---

## 🔨 Building the Project Locally
To generate a fresh debug APK manually:
```bash
./gradlew assembleDebug
```
The output APK will be generated under `app/build/outputs/apk/debug/app-debug.apk`.
