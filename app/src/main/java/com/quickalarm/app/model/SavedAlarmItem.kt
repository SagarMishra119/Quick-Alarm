package com.quickalarm.app.model

import org.json.JSONObject
import java.util.Calendar

data class SavedAlarmItem(
    val id: Long = System.currentTimeMillis(),
    val hour: Int,         // 0 - 23
    val minute: Int,       // 0 - 59
    val label: String = "Alarm",
    val isEnabled: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    /**
     * Computes the next exact trigger timestamp for this alarm time.
     * If the time today has already passed, schedules for tomorrow at the same time.
     */
    fun getNextTriggerTimeMillis(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    fun getFormattedTime(): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("hour", hour)
        json.put("minute", minute)
        json.put("label", label)
        json.put("isEnabled", isEnabled)
        json.put("createdAtMillis", createdAtMillis)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): SavedAlarmItem? {
            return try {
                val json = JSONObject(jsonStr)
                SavedAlarmItem(
                    id = json.getLong("id"),
                    hour = json.getInt("hour"),
                    minute = json.getInt("minute"),
                    label = json.getString("label"),
                    isEnabled = json.getBoolean("isEnabled"),
                    createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
