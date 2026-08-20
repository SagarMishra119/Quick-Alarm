package com.quickalarm.app.model

import android.content.Context
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import org.json.JSONObject

data class SoundItem(
    val id: String,
    val title: String,
    val uriString: String?,
    val isCustom: Boolean = false,
    val soundType: String = TYPE_SYSTEM_ALARM
) {
    fun getUri(context: Context): Uri? {
        if (!uriString.isNullOrBlank()) {
            return try {
                Uri.parse(uriString)
            } catch (e: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
        return when (id) {
            SOUND_ID_NOTIFICATION -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            SOUND_ID_RINGTONE -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("uriString", uriString ?: "")
        json.put("isCustom", isCustom)
        json.put("soundType", soundType)
        return json.toString()
    }

    companion object {
        const val SOUND_ID_DEFAULT = "default_alarm"
        const val SOUND_ID_NOTIFICATION = "default_notification"
        const val SOUND_ID_RINGTONE = "default_ringtone"
        const val SOUND_ID_CUSTOM = "custom_sound"

        const val TYPE_SYSTEM_ALARM = "alarm"
        const val TYPE_SYSTEM_NOTIFICATION = "notification"
        const val TYPE_SYSTEM_RINGTONE = "ringtone"
        const val TYPE_LOCAL_FILE = "custom"

        fun fromJson(jsonStr: String): SoundItem? {
            return try {
                val json = JSONObject(jsonStr)
                SoundItem(
                    id = json.getString("id"),
                    title = json.getString("title"),
                    uriString = if (json.has("uriString") && !json.isNull("uriString")) {
                        json.getString("uriString").takeIf { it.isNotBlank() }
                    } else null,
                    isCustom = json.optBoolean("isCustom", false),
                    soundType = json.optString("soundType", TYPE_SYSTEM_ALARM)
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getDefaultSystemSounds(): List<SoundItem> {
            return listOf(
                SoundItem(SOUND_ID_DEFAULT, "Default Alarm Sound", null, isCustom = false, soundType = TYPE_SYSTEM_ALARM),
                SoundItem(SOUND_ID_NOTIFICATION, "System Notification Sound", null, isCustom = false, soundType = TYPE_SYSTEM_NOTIFICATION),
                SoundItem(SOUND_ID_RINGTONE, "System Ringtone", null, isCustom = false, soundType = TYPE_SYSTEM_RINGTONE)
            )
        }

        /**
         * Scans all installed alarm tones from OEM firmware (Pixel, Samsung, OnePlus, Xiaomi, etc.)
         */
        fun getInstalledDeviceAlarmSounds(context: Context): List<SoundItem> {
            val list = mutableListOf<SoundItem>()
            // Always include standard Default sound first
            list.add(SoundItem(SOUND_ID_DEFAULT, "Default System Alarm", null, isCustom = false, soundType = TYPE_SYSTEM_ALARM))

            try {
                val ringtoneManager = RingtoneManager(context).apply {
                    setType(RingtoneManager.TYPE_ALARM)
                }
                val cursor: Cursor? = ringtoneManager.cursor
                cursor?.use {
                    var index = 0
                    while (it.moveToNext()) {
                        val title = it.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                        val uri = ringtoneManager.getRingtoneUri(it.position)
                        if (uri != null && title != null) {
                            list.add(
                                SoundItem(
                                    id = "sys_alarm_${index++}",
                                    title = title,
                                    uriString = uri.toString(),
                                    isCustom = false,
                                    soundType = TYPE_SYSTEM_ALARM
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback to query notification sounds if alarms list is very short (< 3)
            if (list.size <= 2) {
                try {
                    val notifManager = RingtoneManager(context).apply {
                        setType(RingtoneManager.TYPE_NOTIFICATION)
                    }
                    val cursor = notifManager.cursor
                    cursor?.use {
                        var index = 0
                        while (it.moveToNext() && list.size < 15) {
                            val title = it.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                            val uri = notifManager.getRingtoneUri(it.position)
                            if (uri != null && title != null) {
                                list.add(
                                    SoundItem(
                                        id = "sys_notif_${index++}",
                                        title = title,
                                        uriString = uri.toString(),
                                        isCustom = false,
                                        soundType = TYPE_SYSTEM_NOTIFICATION
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return list.distinctBy { it.title }
        }
    }
}
