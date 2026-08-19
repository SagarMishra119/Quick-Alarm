package com.quickalarm.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.util.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.quickalarm.app.ACTION_TRIGGER_ALARM"
        const val ACTION_DISMISS_ALARM = "com.quickalarm.app.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.quickalarm.app.ACTION_SNOOZE_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Quick Alarm"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                if (alarmId != -1L) {
                    AlarmScheduler.removeAlarm(context, alarmId)
                }

                // Acquire WakeLock
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "QuickAlarm:AlarmWakeLock"
                )
                wakeLock.acquire(10000) // Acquire for 10 seconds

                // Create FullScreen Intent for AlarmActivity
                val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
                    putExtra("ALARM_ID", alarmId)
                    putExtra("ALARM_LABEL", alarmLabel)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(fullScreenIntent)

                // Dismiss PendingIntent
                val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                    this.action = ACTION_DISMISS_ALARM
                    putExtra("NOTIFICATION_ID", alarmId.toInt())
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId.toInt() + 1000,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Snooze PendingIntent (5 minutes)
                val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                    this.action = ACTION_SNOOZE_ALARM
                    putExtra("NOTIFICATION_ID", alarmId.toInt())
                    putExtra("ALARM_LABEL", alarmLabel)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId.toInt() + 2000,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    alarmId.toInt() + 3000,
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                AlarmScheduler.createNotificationChannel(context)

                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val notificationBuilder = NotificationCompat.Builder(context, AlarmScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("⏰ Quick Alarm Firing!")
                    .setContentText(alarmLabel)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setContentIntent(fullScreenPendingIntent)
                    .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent)
                    .addAction(R.drawable.ic_launcher_foreground, "Snooze 5m", snoozePendingIntent)

                val notifId = if (alarmId != -1L) alarmId.toInt() else System.currentTimeMillis().toInt()
                notificationManager.notify(notifId, notificationBuilder.build())
            }

            ACTION_DISMISS_ALARM -> {
                val notifId = intent.getIntExtra("NOTIFICATION_ID", -1)
                if (notifId != -1) {
                    notificationManager.cancel(notifId)
                }
                // Send broadcast to stop ringtone if AlarmActivity is active
                context.sendBroadcast(Intent("com.quickalarm.app.STOP_RINGTONE"))
            }

            ACTION_SNOOZE_ALARM -> {
                val notifId = intent.getIntExtra("NOTIFICATION_ID", -1)
                if (notifId != -1) {
                    notificationManager.cancel(notifId)
                }
                context.sendBroadcast(Intent("com.quickalarm.app.STOP_RINGTONE"))

                // Schedule snooze alarm for 5 minutes
                val snoozeItem = AlarmItem(
                    id = System.currentTimeMillis(),
                    triggerTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000),
                    durationMinutes = 5,
                    label = "Snoozed: $alarmLabel"
                )
                AlarmScheduler.scheduleAlarm(context, snoozeItem)
            }
        }
    }
}
