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

                    // Auto-rearm recurring saved alarms or disable one-shot specific-date alarms
                    val savedAlarms = com.quickalarm.app.util.AppSettings.getSavedAlarms(context)
                    val matchingSaved = savedAlarms.find { it.id == alarmId }
                    if (matchingSaved != null && matchingSaved.isEnabled) {
                        if (matchingSaved.specificDateMillis != null) {
                            // Specific date alarm fired -> disable it
                            com.quickalarm.app.util.AppSettings.updateSavedAlarm(context, matchingSaved.copy(isEnabled = false))
                        } else {
                            // Recurring alarm (Everyday or Day-of-week) -> schedule next occurrence
                            val nextTrigger = matchingSaved.getNextTriggerTimeMillis()
                            val nextAlarmItem = com.quickalarm.app.model.AlarmItem(
                                id = matchingSaved.id,
                                label = matchingSaved.label,
                                durationMinutes = 0,
                                triggerTimeMillis = nextTrigger,
                                createdAtMillis = System.currentTimeMillis()
                            )
                            AlarmScheduler.scheduleAlarm(context, nextAlarmItem)
                        }
                    }
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
