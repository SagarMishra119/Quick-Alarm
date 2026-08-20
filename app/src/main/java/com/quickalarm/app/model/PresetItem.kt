package com.quickalarm.app.model

import androidx.compose.ui.graphics.Color
import com.quickalarm.app.ui.theme.*
import org.json.JSONObject
import java.util.UUID

data class PresetItem(
    val id: String = UUID.randomUUID().toString(),
    val minutes: Int,
    val title: String,
    val subtitle: String,
    val colorKey: String = "indigo"
) {
    fun getGradient(): List<Color> {
        return when (colorKey) {
            "indigo" -> GradientPreset15m
            "cyan" -> GradientPreset30m
            "emerald" -> GradientPreset1h
            "amber" -> GradientPreset2h
            "purple" -> GradientPreset4h
            "rose" -> GradientPreset6h
            "pink" -> listOf(Color(0xFFEC4899), Color(0xFFBE185D))
            "teal" -> listOf(Color(0xFF14B8A6), Color(0xFF0F766E))
            "orange" -> listOf(Color(0xFFF97316), Color(0xFFC2410C))
            "blue" -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
            else -> GradientPreset15m
        }
    }

    fun getPrimaryColor(): Color {
        return getGradient().first()
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("minutes", minutes)
        json.put("title", title)
        json.put("subtitle", subtitle)
        json.put("colorKey", colorKey)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): PresetItem? {
            return try {
                val json = JSONObject(jsonStr)
                PresetItem(
                    id = json.optString("id", UUID.randomUUID().toString()),
                    minutes = json.getInt("minutes"),
                    title = json.getString("title"),
                    subtitle = json.getString("subtitle"),
                    colorKey = json.optString("colorKey", "indigo")
                )
            } catch (e: Exception) {
                null
            }
        }

        fun getDefaultPresets(): List<PresetItem> {
            return listOf(
                PresetItem(id = "default_15m", minutes = 15, title = "+15 Min", subtitle = "Short Break", colorKey = "indigo"),
                PresetItem(id = "default_30m", minutes = 30, title = "+30 Min", subtitle = "Power Nap", colorKey = "cyan"),
                PresetItem(id = "default_1h",  minutes = 60, title = "+1 Hr",   subtitle = "Focus Session", colorKey = "emerald"),
                PresetItem(id = "default_2h",  minutes = 120, title = "+2 Hrs", subtitle = "Deep Work", colorKey = "amber"),
                PresetItem(id = "default_4h",  minutes = 240, title = "+4 Hrs", subtitle = "Half Day", colorKey = "purple"),
                PresetItem(id = "default_6h",  minutes = 360, title = "+6 Hrs", subtitle = "Full Sleep", colorKey = "rose")
            )
        }

        val AVAILABLE_COLORS = listOf(
            Pair("indigo", "Indigo"),
            Pair("cyan", "Cyan"),
            Pair("emerald", "Emerald"),
            Pair("amber", "Amber"),
            Pair("purple", "Purple"),
            Pair("rose", "Rose"),
            Pair("pink", "Pink"),
            Pair("teal", "Teal"),
            Pair("orange", "Orange"),
            Pair("blue", "Blue")
        )
    }
}
