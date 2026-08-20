package com.quickalarm.app.util

import android.content.Context
import android.content.SharedPreferences
import com.quickalarm.app.model.PresetItem
import com.quickalarm.app.model.SavedAlarmItem
import com.quickalarm.app.model.SoundItem
import org.json.JSONArray

object AppSettings {
    private const val PREFS_SETTINGS = "quick_alarm_settings_v3"
    private const val KEY_PRESETS = "custom_presets"
    private const val KEY_SAVED_ALARMS = "saved_fixed_alarms"
    private const val KEY_SELECTED_SOUND = "selected_sound"
    private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
    
    const val MAX_PRESETS = 10
    const val MAX_SAVED_ALARMS = 10

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
    }

    // ================= PRESETS MANAGEMENT =================

    fun getPresets(context: Context): List<PresetItem> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_PRESETS, null)
        if (jsonStr.isNullOrBlank()) {
            val defaults = PresetItem.getDefaultPresets()
            savePresets(context, defaults)
            return defaults
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<PresetItem>()
            for (i in 0 until jsonArray.length()) {
                val item = PresetItem.fromJson(jsonArray.getString(i))
                if (item != null) list.add(item)
            }
            if (list.isEmpty()) {
                val defaults = PresetItem.getDefaultPresets()
                savePresets(context, defaults)
                defaults
            } else {
                list
            }
        } catch (e: Exception) {
            PresetItem.getDefaultPresets()
        }
    }

    fun savePresets(context: Context, presets: List<PresetItem>) {
        val limited = presets.take(MAX_PRESETS)
        val jsonArray = JSONArray()
        limited.forEach { jsonArray.put(it.toJson()) }
        getPrefs(context).edit().putString(KEY_PRESETS, jsonArray.toString()).apply()
    }

    fun addPreset(context: Context, preset: PresetItem): Boolean {
        val current = getPresets(context).toMutableList()
        if (current.size >= MAX_PRESETS) return false
        current.add(preset)
        savePresets(context, current)
        return true
    }

    fun updatePreset(context: Context, updated: PresetItem) {
        val current = getPresets(context).map {
            if (it.id == updated.id) updated else it
        }
        savePresets(context, current)
    }

    fun deletePreset(context: Context, presetId: String) {
        val current = getPresets(context).filterNot { it.id == presetId }
        savePresets(context, current)
    }

    fun resetPresetsToDefault(context: Context): List<PresetItem> {
        val defaults = PresetItem.getDefaultPresets()
        savePresets(context, defaults)
        return defaults
    }

    // ================= SAVED FIXED ALARMS (UP TO 10) =================

    fun getSavedAlarms(context: Context): List<SavedAlarmItem> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_SAVED_ALARMS, null)
        if (jsonStr.isNullOrBlank()) {
            return emptyList() // Empty by default
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<SavedAlarmItem>()
            for (i in 0 until jsonArray.length()) {
                val item = SavedAlarmItem.fromJson(jsonArray.getString(i))
                if (item != null) list.add(item)
            }
            list.sortedWith(compareBy({ it.hour }, { it.minute }))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSavedAlarms(context: Context, alarms: List<SavedAlarmItem>) {
        val limited = alarms.take(MAX_SAVED_ALARMS)
        val jsonArray = JSONArray()
        limited.forEach { jsonArray.put(it.toJson()) }
        getPrefs(context).edit().putString(KEY_SAVED_ALARMS, jsonArray.toString()).apply()
    }

    fun addSavedAlarm(context: Context, alarm: SavedAlarmItem): Boolean {
        val current = getSavedAlarms(context).toMutableList()
        if (current.size >= MAX_SAVED_ALARMS) return false
        current.add(alarm)
        saveSavedAlarms(context, current)
        return true
    }

    fun updateSavedAlarm(context: Context, updated: SavedAlarmItem) {
        val current = getSavedAlarms(context).map {
            if (it.id == updated.id) updated else it
        }
        saveSavedAlarms(context, current)
    }

    fun deleteSavedAlarm(context: Context, alarmId: Long) {
        val current = getSavedAlarms(context).filterNot { it.id == alarmId }
        saveSavedAlarms(context, current)
    }

    // ================= SOUND SETTINGS =================

    fun getSelectedSound(context: Context): SoundItem {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_SELECTED_SOUND, null)
        if (!jsonStr.isNullOrBlank()) {
            val sound = SoundItem.fromJson(jsonStr)
            if (sound != null) return sound
        }
        return SoundItem(SoundItem.SOUND_ID_DEFAULT, "Default Alarm Sound", null, isCustom = false)
    }

    fun setSelectedSound(context: Context, sound: SoundItem) {
        getPrefs(context).edit().putString(KEY_SELECTED_SOUND, sound.toJson()).apply()
    }

    // ================= SNOOZE SETTINGS =================

    val SNOOZE_PRESETS = listOf(1, 3, 5, 10, 15, 20, 30)

    fun getSnoozeMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_SNOOZE_MINUTES, 5)
    }

    fun setSnoozeMinutes(context: Context, minutes: Int) {
        val sanitized = minutes.coerceIn(1, 60)
        getPrefs(context).edit().putInt(KEY_SNOOZE_MINUTES, sanitized).apply()
    }
}
