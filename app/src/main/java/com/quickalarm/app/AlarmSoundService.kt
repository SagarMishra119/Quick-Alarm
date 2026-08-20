package com.quickalarm.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.util.AlarmScheduler
import com.quickalarm.app.util.AppSettings

class AlarmSoundService : Service() {

    companion object {
        const val ACTION_START_ALARM = "com.quickalarm.app.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.quickalarm.app.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.quickalarm.app.ACTION_SNOOZE_ALARM"

        var isRinging = false
            private set
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM
        val alarmId = intent?.getLongExtra("ALARM_ID", -1L) ?: -1L
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Quick Alarm"
        val notifId = if (alarmId != -1L) alarmId.toInt() else 1001

        when (action) {
            ACTION_START_ALARM -> {
                isRinging = true
                acquireWakeLock()
                startRinging(alarmId, alarmLabel, notifId)
            }
            ACTION_STOP_ALARM -> {
                stopAlarmAndSelf()
            }
            ACTION_SNOOZE_ALARM -> {
                val snoozeMinutes = intent?.getIntExtra("SNOOZE_MINUTES", AppSettings.getSnoozeMinutes(this)) ?: 5
                val snoozeItem = AlarmItem(
                    id = System.currentTimeMillis(),
                    triggerTimeMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L),
                    durationMinutes = snoozeMinutes,
                    label = "Snoozed: $alarmLabel"
                )
                AlarmScheduler.scheduleAlarm(this, snoozeItem)
                stopAlarmAndSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "QuickAlarm:AlarmServiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes max
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRinging(alarmId: Long, alarmLabel: String, notifId: Int) {
        // 1. Play Sound in Background Service
        try {
            val selectedSound = AppSettings.getSelectedSound(this)
            val soundUri = selectedSound.getUri(this) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(this@AlarmSoundService, soundUri)
                } catch (e: Exception) {
                    val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    setDataSource(this@AlarmSoundService, fallbackUri)
                }
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Start Repeating Vibration
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 600, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Build & Start Foreground Notification
        val notification = buildForegroundNotification(alarmId, alarmLabel, notifId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notifId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notifId, notification)
        }

        // 4. Also launch full-screen AlarmActivity
        try {
            val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildForegroundNotification(alarmId: Long, alarmLabel: String, notifId: Int): Notification {
        AlarmScheduler.createNotificationChannel(this)

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            notifId + 100,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Action
        val dismissIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra("ALARM_ID", alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            notifId + 200,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeMinutes = AppSettings.getSnoozeMinutes(this)
        val snoozeIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("SNOOZE_MINUTES", snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            notifId + 300,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Quick Alarm Ringing!")
            .setContentText(alarmLabel)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze ${snoozeMinutes}m", snoozePendingIntent)
            .build()
    }

    private fun stopAlarmAndSelf() {
        isRinging = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlarmAndSelf()
        super.onDestroy()
    }
}
