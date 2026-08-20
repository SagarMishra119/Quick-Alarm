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
    var colorKey by remember { mutableStateOf(presetToEdit?.colorKey ?: "indigo") }
    var subtitle by remember { mutableStateOf(presetToEdit?.subtitle ?: "Quick Alarm") }

    fun formatTitle(h: Int, m: Int): String {
        return when {
            h > 0 && m > 0 -> "+${h}h ${m}m"
            h > 0 -> "+${h}h"
            else -> "+${m}m"
        }
    }

    var title by remember {
        mutableStateOf(presetToEdit?.title ?: formatTitle(hours, minutes))
    }

    var userCustomizedTitle by remember {
        mutableStateOf(presetToEdit != null && presetToEdit.title != formatTitle(presetToEdit.minutes / 60, presetToEdit.minutes % 60))
    }

    fun onTimeChanged(newHours: Int, newMinutes: Int) {
        hours = newHours
        minutes = newMinutes
        if (!userCustomizedTitle) {
            title = formatTitle(newHours, newMinutes)
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

    val tempPreset = PresetItem(
        id = presetToEdit?.id ?: "preset_${System.currentTimeMillis()}",
        title = title.ifBlank { formatTitle(hours, minutes) },
        subtitle = subtitle,
        minutes = (hours * 60) + minutes,
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
                .heightIn(max = 580.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (colors.isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.AddAlarm,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "Edit Preset" else "Add New Preset",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview Card
                Text(
                    text = "LIVE PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                PresetAlarmButton(
                    preset = tempPreset,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hours Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hours: $hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { if (hours > 0) onTimeChanged(hours - 1, minutes) },
                            enabled = hours > 0,
                            modifier = Modifier.size(30.dp).background(colors.chipBackground, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1 hr", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { if (hours < 24) onTimeChanged(hours + 1, minutes) },
                            enabled = hours < 24,
                            modifier = Modifier.size(30.dp).background(colors.chipBackground, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1 hr", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Slider(
                    value = hours.toFloat(),
                    onValueChange = { onTimeChanged(it.toInt(), minutes) },
                    valueRange = 0f..24f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryIndigo,
                        activeTrackColor = PrimaryIndigo,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Minutes Adjuster (±1m, ±5m buttons and exact slider)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Minutes: $minutes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val nextM = (minutes - 5).coerceAtLeast(0)
                                if (nextM > 0 || hours > 0) onTimeChanged(hours, nextM)
                            },
                            enabled = minutes > 0 || hours > 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = {
                                if (minutes > 0 || hours > 0) onTimeChanged(hours, (minutes - 1).coerceAtLeast(0))
                            },
                            enabled = minutes > 0 || hours > 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = {
                                if (minutes < 59) onTimeChanged(hours, minutes + 1)
                            },
                            enabled = minutes < 59,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = {
                                val nextM = (minutes + 5).coerceAtMost(59)
                                onTimeChanged(hours, nextM)
                            },
                            enabled = minutes < 59,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    }
                }

                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { onTimeChanged(hours, it.toInt()) },
                    valueRange = 0f..59f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryCyan,
                        activeTrackColor = SecondaryCyan,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                                title = formatTitle(hours, minutes)
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

                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

                // Color Theme Picker
                Text(
                    text = "COLOR THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableColors) { (key, _) ->
                        val isSelected = colorKey == key
                        val colorGradient = PresetItem.getGradientForKey(key)

                        Box(
                            modifier = Modifier
                                .size(38.dp)
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
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Preset Button
                Button(
                    onClick = {
                        val totalMins = (hours * 60) + minutes
                        if (totalMins > 0) {
                            val finalTitle = title.ifBlank { formatTitle(hours, minutes) }
                            val finalSub = subtitle.ifBlank { "Quick Alarm" }
                            val result = PresetItem(
                                id = presetToEdit?.id ?: "preset_${System.currentTimeMillis()}",
                                title = finalTitle,
                                subtitle = finalSub,
                                minutes = totalMins,
                                colorKey = colorKey
                            )
                            onSave(result)
                        }
                    },
                    enabled = (hours * 60) + minutes > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        text = if (isEditing) "Save Changes" else "Add Preset",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
