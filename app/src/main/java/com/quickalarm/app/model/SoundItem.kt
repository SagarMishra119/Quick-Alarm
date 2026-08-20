package com.quickalarm.app.model

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import org.json.JSONObject

data class SoundItem(
    val id: String,
    val title: String,
    val uriString: String?,
    val isCustom: Boolean = false
) {
    fun getUri(context: Context): Uri? {
        if (uriString != null) {
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
        return json.toString()
    }

    companion object {
        const val SOUND_ID_DEFAULT = "default_alarm"
        const val SOUND_ID_NOTIFICATION = "default_notification"
        const val SOUND_ID_RINGTONE = "default_ringtone"
        const val SOUND_ID_CUSTOM = "custom_sound"

        fun fromJson(jsonStr: String): SoundItem? {
            return try {
                val json = JSONObject(jsonStr)
                SoundItem(
                    id = json.getString("id"),
                    title = json.getString("title"),
                    uriString = if (json.has("uriString") && !json.isNull("uriString")) {
                        json.getString("uriString").takeIf { it.isNotBlank() }
                    } else null,
                    isCustom = json.optBoolean("isCustom", false)
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getDefaultSystemSounds(): List<SoundItem> {
            return listOf(
                SoundItem(SOUND_ID_DEFAULT, "Default Alarm Sound", null, isCustom = false),
                SoundItem(SOUND_ID_NOTIFICATION, "System Notification Sound", null, isCustom = false),
                SoundItem(SOUND_ID_RINGTONE, "System Ringtone", null, isCustom = false)
            )
        }
    }
}
