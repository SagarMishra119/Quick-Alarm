package com.quickalarm.app.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SavedAlarmItem(
    val id: Long = System.currentTimeMillis(),
    val hour: Int,         // 0 - 23
    val minute: Int,       // 0 - 59
    val label: String = "Alarm",
    val isEnabled: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val repeatDays: Set<Int> = emptySet(),       // Calendar.SUNDAY (1) .. Calendar.SATURDAY (7). Empty = Everyday / Daily
    val specificDateMillis: Long? = null        // If set, fires on this specific calendar date at midnight
) {
    /**
     * Computes the next exact trigger timestamp for this alarm.
     * Supports:
     * 1. Specific Calendar Date (one-shot for a specific date)
     * 2. Day-of-Week Recurring (e.g. Mon, Wed, Fri or Weekdays/Weekends)
     * 3. Everyday / Daily (today if in future, else tomorrow)
     */
    fun getNextTriggerTimeMillis(): Long {
        val now = Calendar.getInstance()

        // Case 1: Specific Date Alarm
        if (specificDateMillis != null) {
            val target = Calendar.getInstance().apply {
                timeInMillis = specificDateMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return target.timeInMillis
        }

        // Case 2: Custom Day-of-Week Recurring Alarm (e.g. Mon, Wed, Fri)
        if (repeatDays.isNotEmpty()) {
            for (dayOffset in 0..7) {
                val candidate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val candidateDayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)

                if (candidateDayOfWeek in repeatDays) {
                    if (candidate.timeInMillis > now.timeInMillis) {
                        return candidate.timeInMillis
                    }
                }
            }
        }

        // Case 3: Standard Everyday / Daily Alarm
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

    /**
     * Returns a concise human-readable recurrence or date badge.
     * Examples: "Every day", "Weekdays", "Weekends", "Mon, Wed, Fri", "📅 Oct 14, 2026"
     */
    fun getScheduleSummary(): String {
        if (specificDateMillis != null) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return "📅 " + sdf.format(Date(specificDateMillis))
        }

        if (repeatDays.isEmpty() || repeatDays.size == 7) {
            return "Every day"
        }

        val weekdays = setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
        )
        if (repeatDays == weekdays) return "Weekdays"

        val weekends = setOf(Calendar.SATURDAY, Calendar.SUNDAY)
        if (repeatDays == weekends) return "Weekends"

        // Day name abbreviations in calendar order (Sunday to Saturday)
        val dayNames = mapOf(
            Calendar.SUNDAY to "Sun",
            Calendar.MONDAY to "Mon",
            Calendar.TUESDAY to "Tue",
            Calendar.WEDNESDAY to "Wed",
            Calendar.THURSDAY to "Thu",
            Calendar.FRIDAY to "Fri",
            Calendar.SATURDAY to "Sat"
        )

        return listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        ).filter { it in repeatDays }
            .joinToString(", ") { dayNames[it] ?: "" }
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("hour", hour)
        json.put("minute", minute)
        json.put("label", label)
        json.put("isEnabled", isEnabled)
        json.put("createdAtMillis", createdAtMillis)

        if (repeatDays.isNotEmpty()) {
            val repeatArray = JSONArray()
            repeatDays.forEach { repeatArray.put(it) }
            json.put("repeatDays", repeatArray)
        }

        if (specificDateMillis != null) {
            json.put("specificDateMillis", specificDateMillis)
        }

        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): SavedAlarmItem? {
            return try {
                val json = JSONObject(jsonStr)
                val repeatDaysSet = mutableSetOf<Int>()
                if (json.has("repeatDays")) {
                    val repeatArray = json.getJSONArray("repeatDays")
                    for (i in 0 until repeatArray.length()) {
                        repeatDaysSet.add(repeatArray.getInt(i))
                    }
                }

                val specificDate = if (json.has("specificDateMillis")) json.getLong("specificDateMillis") else null

                SavedAlarmItem(
                    id = json.getLong("id"),
                    hour = json.getInt("hour"),
                    minute = json.getInt("minute"),
                    label = json.getString("label"),
                    isEnabled = json.getBoolean("isEnabled"),
                    createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis()),
                    repeatDays = repeatDaysSet,
                    specificDateMillis = specificDate
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
