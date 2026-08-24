package com.quickalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickalarm.app.model.PresetItem
import com.quickalarm.app.ui.components.CasinoTimeTumbler
import com.quickalarm.app.ui.theme.*

@Composable
fun PresetEditDialog(
    presetToEdit: PresetItem? = null,
    onDismiss: () -> Unit,
    onSave: (PresetItem) -> Unit
) {
    val colors = AppTheme.colors
    val isEditing = presetToEdit != null

    var hours by remember { mutableIntStateOf(presetToEdit?.let { it.minutes / 60 } ?: 0) }
    var minutes by remember { mutableIntStateOf(presetToEdit?.let { it.minutes % 60 } ?: 15) }
    var seconds by remember { mutableIntStateOf(0) }
    var colorKey by remember { mutableStateOf(presetToEdit?.colorKey ?: "indigo") }
    var subtitle by remember { mutableStateOf(presetToEdit?.subtitle ?: "Quick Alarm") }

    fun formatTitle(h: Int, m: Int, s: Int = 0): String {
        return when {
            h > 0 && m > 0 && s > 0 -> "+${h}h ${m}m ${s}s"
            h > 0 && m > 0 -> "+${h}h ${m}m"
            h > 0 && s > 0 -> "+${h}h ${s}s"
            h > 0 -> "+${h}h"
            m > 0 && s > 0 -> "+${m}m ${s}s"
            m > 0 -> "+${m}m"
            s > 0 -> "+${s}s"
            else -> "+0m"
        }
    }

    var title by remember {
        mutableStateOf(presetToEdit?.title ?: formatTitle(hours, minutes, seconds))
    }

    var userCustomizedTitle by remember {
        mutableStateOf(presetToEdit != null && presetToEdit.title != formatTitle(presetToEdit.minutes / 60, presetToEdit.minutes % 60, 0))
    }

    fun onTimeChanged(newHours: Int, newMinutes: Int, newSeconds: Int) {
        hours = newHours
        minutes = newMinutes
        seconds = newSeconds
        if (!userCustomizedTitle) {
            title = formatTitle(newHours, newMinutes, newSeconds)
        }
    }

    val availableColors = listOf(
        Pair("indigo", "Indigo"),
        Pair("cyan", "Cyan"),
        Pair("emerald", "Emerald"),
        Pair("amber", "Amber"),
        Pair("purple", "Purple"),
        Pair("rose", "Rose"),
        Pair("teal", "Teal"),
        Pair("blue", "Blue"),
        Pair("orange", "Orange"),
        Pair("pink", "Pink")
    )

    val totalSeconds = (hours * 3600) + (minutes * 60) + seconds
    val effectiveMinutes = if (totalSeconds > 0) ((totalSeconds + 59) / 60) else 0

    val tempPreset = PresetItem(
        id = presetToEdit?.id ?: "preset_${System.currentTimeMillis()}",
        title = title.ifBlank { formatTitle(hours, minutes, seconds) },
        subtitle = subtitle,
        minutes = effectiveMinutes,
        colorKey = colorKey
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 700.dp)
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (colors.isDark) Color(0xFF111827).copy(alpha = 0.90f) else Color(0xFFFFFFFF).copy(alpha = 0.94f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (colors.isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFCBD5E1)
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (colors.isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.AddAlarm,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "Edit Preset (up to 24h)" else "Add Preset (up to 24h)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Preview Card
                PresetAlarmButton(
                    preset = tempPreset,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 🎰 Vibrant Casino Time Tumbler Roller
                CasinoTimeTumbler(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                    onTimeChange = { h, m, s -> onTimeChanged(h, m, s) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input with Sync Button
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        userCustomizedTitle = true
                    },
                    label = { Text("Button Title") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                userCustomizedTitle = false
                                title = formatTitle(hours, minutes, seconds)
                            }
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync Title", tint = PrimaryIndigo)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle Input
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Category (e.g. Nap, Focus, Rest)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color Theme Picker
                Text(
                    text = "COLOR THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableColors) { (key, _) ->
                        val isSelected = colorKey == key
                        val colorGradient = PresetItem.getGradientForKey(key)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colorGradient))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Preset Button
                Button(
                    onClick = {
                        if (totalSeconds > 0) {
                            val finalTitle = title.ifBlank { formatTitle(hours, minutes, seconds) }
                            val finalSub = subtitle.ifBlank { "Quick Alarm" }
                            val result = PresetItem(
                                id = presetToEdit?.id ?: "preset_${System.currentTimeMillis()}",
                                title = finalTitle,
                                subtitle = finalSub,
                                minutes = effectiveMinutes,
                                colorKey = colorKey
                            )
                            onSave(result)
                        }
                    },
                    enabled = totalSeconds > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        text = if (isEditing) "Save Changes (${formatTitle(hours, minutes, seconds)})" else "Add Preset (${formatTitle(hours, minutes, seconds)})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel", color = colors.textMuted, fontSize = 14.sp)
                }
            }
        }
    }
}
