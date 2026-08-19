package com.quickalarm.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quickalarm.app.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val alarms = AlarmScheduler.getActiveAlarms(context)
            val now = System.currentTimeMillis()
            for (alarm in alarms) {
                if (alarm.triggerTimeMillis > now) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                } else {
                    AlarmScheduler.removeAlarm(context, alarm.id)
                }
            }
        }
    }
}
