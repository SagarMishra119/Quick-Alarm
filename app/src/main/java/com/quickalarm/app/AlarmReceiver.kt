package com.quickalarm.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
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

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                if (alarmId != -1L) {
                    AlarmScheduler.removeAlarm(context, alarmId)
                }

                // Start Foreground AlarmSoundService so audio rings & vibrates even if app is closed/killed!
                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    this.action = AlarmSoundService.ACTION_START_ALARM
                    putExtra("ALARM_ID", alarmId)
                    putExtra("ALARM_LABEL", alarmLabel)
                }
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        context.startService(serviceIntent)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            ACTION_DISMISS_ALARM -> {
                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    this.action = AlarmSoundService.ACTION_STOP_ALARM
                }
                context.startService(serviceIntent)
            }

            ACTION_SNOOZE_ALARM -> {
                val snoozeMinutes = intent.getIntExtra("SNOOZE_MINUTES", 5)
                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    this.action = AlarmSoundService.ACTION_SNOOZE_ALARM
                    putExtra("ALARM_LABEL", alarmLabel)
                    putExtra("SNOOZE_MINUTES", snoozeMinutes)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
