package com.quickalarm.app.model

import org.json.JSONObject

data class AlarmItem(
    val id: Long,
    val triggerTimeMillis: Long,
    val durationMinutes: Int,
    val label: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("triggerTimeMillis", triggerTimeMillis)
        json.put("durationMinutes", durationMinutes)
        json.put("label", label)
        json.put("createdAtMillis", createdAtMillis)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): AlarmItem? {
            return try {
                val json = JSONObject(jsonStr)
                AlarmItem(
                    id = json.getLong("id"),
                    triggerTimeMillis = json.getLong("triggerTimeMillis"),
                    durationMinutes = json.getInt("durationMinutes"),
                    label = json.getString("label"),
                    createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
