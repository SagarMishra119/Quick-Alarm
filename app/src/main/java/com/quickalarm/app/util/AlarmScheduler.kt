package com.quickalarm.app.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.quickalarm.app.AlarmReceiver
import com.quickalarm.app.MainActivity
import com.quickalarm.app.R
import com.quickalarm.app.model.AlarmItem
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmScheduler {

    const val CHANNEL_ID = "quick_alarm_channel_v2"
    private const val PREFS_NAME = "quick_alarm_prefs"
    private const val KEY_ALARMS = "saved_alarms"

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    fun scheduleAlarm(context: Context, alarmItem: AlarmItem): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra("ALARM_ID", alarmItem.id)
            putExtra("ALARM_LABEL", alarmItem.label)
            putExtra("ALARM_TRIGGER_TIME", alarmItem.triggerTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Fallback to setAndAllowWhileIdle if exact permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmItem.triggerTimeMillis,
                    pendingIntent
                )
            } else {
                val showIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    alarmItem.id.toInt(),
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmItem.triggerTimeMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }

            saveAlarm(context, alarmItem)
            return true
        } catch (e: SecurityException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun cancelAlarm(context: Context, alarmItem: AlarmItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        removeAlarm(context, alarmItem.id)
    }

    fun getActiveAlarms(context: Context): List<AlarmItem> {
        val prefs = getPrefs(context)
        val jsonArrayStr = prefs.getString(KEY_ALARMS, "[]") ?: "[]"
        val list = mutableListOf<AlarmItem>()
        val now = System.currentTimeMillis()
        try {
            val jsonArray = JSONArray(jsonArrayStr)
            for (i in 0 until jsonArray.length()) {
                val itemStr = jsonArray.getString(i)
                val item = AlarmItem.fromJson(itemStr)
                if (item != null && item.triggerTimeMillis > now - 60_000) { // Keep if within last minute or future
                    list.add(item)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.triggerTimeMillis }
    }

    private fun saveAlarm(context: Context, alarmItem: AlarmItem) {
        val currentAlarms = getActiveAlarms(context).toMutableList()
        currentAlarms.removeAll { it.id == alarmItem.id }
        currentAlarms.add(alarmItem)
        saveAlarmList(context, currentAlarms)
    }

    fun removeAlarm(context: Context, alarmId: Long) {
        val currentAlarms = getActiveAlarms(context).filterNot { it.id == alarmId }
        saveAlarmList(context, currentAlarms)
    }

    private fun saveAlarmList(context: Context, alarms: List<AlarmItem>) {
        val jsonArray = JSONArray()
        alarms.forEach { jsonArray.put(it.toJson()) }
        getPrefs(context).edit().putString(KEY_ALARMS, jsonArray.toString()).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Creates the notification channel for high-priority full-screen alarms.
     * Sound on the notification channel is explicitly set to null to avoid dual-sound collision
     * with AlarmActivity's looping MediaPlayer.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_description)
                // Disable channel sound so AlarmActivity MediaPlayer is the single sound source
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatFullDateTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatRemainingTime(triggerTimeMillis: Long): String {
        val diffMs = triggerTimeMillis - System.currentTimeMillis()
        if (diffMs <= 0) return "Due now"
        val totalSecs = diffMs / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        return when {
            hours > 0 -> "${hours}h ${mins}m remaining"
            mins > 0 -> "${mins}m ${secs}s remaining"
            else -> "${secs}s remaining"
        }
    }
}
