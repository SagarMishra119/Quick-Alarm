package com.quickalarm.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.quickalarm.app.MainActivity
import com.quickalarm.app.R
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.util.AlarmScheduler
import com.quickalarm.app.util.AppSettings

class QuickAlarmWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PRESET_CLICK = "com.quickalarm.app.ACTION_WIDGET_PRESET_CLICK"
        const val EXTRA_MINUTES = "EXTRA_PRESET_MINUTES"
        const val EXTRA_LABEL = "EXTRA_PRESET_LABEL"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickAlarmWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val provider = QuickAlarmWidgetProvider()
                provider.onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val presets = AppSettings.getPresets(context)
        val activeAlarms = AlarmScheduler.getActiveAlarms(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_alarm)

            // Header Click -> Open MainActivity
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                100,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header, openAppPendingIntent)

            // Status Text (Next active alarm)
            if (activeAlarms.isNotEmpty()) {
                val nextAlarm = activeAlarms.first()
                val timeStr = AlarmScheduler.formatTime(nextAlarm.triggerTimeMillis)
                views.setTextViewText(R.id.widget_status_text, "⏰ Rings $timeStr")
                views.setTextColor(R.id.widget_status_text, 0xFF06B6D4.toInt())
            } else {
                views.setTextViewText(R.id.widget_status_text, "No Alarms")
                views.setTextColor(R.id.widget_status_text, 0xFF94A3B8.toInt())
            }

            // Top 4 Presets
            val btnIds = listOf(
                Pair(R.id.widget_btn_1, Pair(R.id.widget_btn_1_title, R.id.widget_btn_1_sub)),
                Pair(R.id.widget_btn_2, Pair(R.id.widget_btn_2_title, R.id.widget_btn_2_sub)),
                Pair(R.id.widget_btn_3, Pair(R.id.widget_btn_3_title, R.id.widget_btn_3_sub)),
                Pair(R.id.widget_btn_4, Pair(R.id.widget_btn_4_title, R.id.widget_btn_4_sub))
            )

            for (i in btnIds.indices) {
                val (containerId, textIds) = btnIds[i]
                val (titleId, subId) = textIds

                if (i < presets.size) {
                    val preset = presets[i]
                    views.setTextViewText(titleId, preset.title)
                    views.setTextViewText(subId, preset.subtitle)

                    val clickIntent = Intent(context, QuickAlarmWidgetProvider::class.java).apply {
                        action = ACTION_PRESET_CLICK
                        putExtra(EXTRA_MINUTES, preset.minutes)
                        putExtra(EXTRA_LABEL, "${preset.title} Quick Alarm")
                    }
                    val clickPendingIntent = PendingIntent.getBroadcast(
                        context,
                        200 + i,
                        clickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(containerId, clickPendingIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_PRESET_CLICK) {
            val minutes = intent.getIntExtra(EXTRA_MINUTES, 15)
            val label = intent.getStringExtra(EXTRA_LABEL) ?: "Quick Alarm"

            val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val alarm = AlarmItem(
                id = System.currentTimeMillis(),
                triggerTimeMillis = triggerTime,
                durationMinutes = minutes,
                label = label
            )

            val success = AlarmScheduler.scheduleAlarm(context, alarm)
            if (success) {
                Toast.makeText(
                    context,
                    "⏰ Alarm set for ${AlarmScheduler.formatTime(triggerTime)}",
                    Toast.LENGTH_LONG
                ).show()
                updateAllWidgets(context)
            } else {
                Toast.makeText(
                    context,
                    "Failed to set alarm. Check exact alarm permissions.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
