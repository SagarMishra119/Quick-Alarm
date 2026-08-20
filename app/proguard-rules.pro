# ProGuard / R8 Rules for Quick Alarm

# Preserve data models for JSON serialization (SharedPreferences)
-keepclassmembers class com.quickalarm.app.model.** { *; }
-keep class com.quickalarm.app.model.** { *; }

# Jetpack Compose Rules
-keepattributes *Annotation*
-keepclassmembers class androidx.compose.runtime.RecomposeScopeImpl { *; }

# Service & Receiver components
-keep public class com.quickalarm.app.AlarmSoundService extends android.app.Service
-keep public class com.quickalarm.app.AlarmReceiver extends android.content.BroadcastReceiver
-keep public class com.quickalarm.app.BootReceiver extends android.content.BroadcastReceiver
