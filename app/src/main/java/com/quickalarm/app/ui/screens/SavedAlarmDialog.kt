package com.quickalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickalarm.app.model.SavedAlarmItem
import com.quickalarm.app.ui.theme.*

@Composable
fun SavedAlarmDialog(
    alarmToEdit: SavedAlarmItem? = null,
    onDismiss: () -> Unit,
    onSave: (SavedAlarmItem) -> Unit
) {
    val colors = AppTheme.colors
    val isEditing = alarmToEdit != null

    val initialHour12 = if (alarmToEdit != null) {
        val h = alarmToEdit.hour
        when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
    } else 7

    val initialIsPm = if (alarmToEdit != null) alarmToEdit.hour >= 12 else false
    val initialMinute = alarmToEdit?.minute ?: 0

    var hour12 by remember { mutableIntStateOf(initialHour12) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var isPm by remember { mutableStateOf(initialIsPm) }

    // Helper for automatic time of day labeling
    fun getTimeOfDayLabel(h24: Int): String {
        return when (h24) {
            in 0..4 -> "Late Night Alarm"
            in 5..7 -> "Early Morning Alarm"
            in 8..11 -> "Morning Alarm"
            in 12..16 -> "Afternoon Alarm"
            in 17..20 -> "Evening Alarm"
            else -> "Night Alarm"
        }
    }

    // Convert 12-hour + AM/PM back to 24-hour
    val hour24 = when {
        isPm && hour12 < 12 -> hour12 + 12
        !isPm && hour12 == 12 -> 0
        else -> hour12
    }

    var userHasCustomizedLabel by remember { mutableStateOf(alarmToEdit != null) }
    var label by remember {
        mutableStateOf(alarmToEdit?.label ?: getTimeOfDayLabel(hour24))
    }

    // Auto-update label when time changes unless user typed a custom label
    LaunchedEffect(hour24) {
        if (!userHasCustomizedLabel) {
            label = getTimeOfDayLabel(hour24)
        }
    }

    val tempAlarm = SavedAlarmItem(
        id = alarmToEdit?.id ?: System.currentTimeMillis(),
        hour = hour24,
        minute = minute,
        label = label,
        isEnabled = true
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 600.dp)
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (colors.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (colors.isDark) AccentEmerald else Color(0xFF065F46),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "Edit Saved Alarm" else "New Saved Alarm",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Display Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                if (colors.isDark) listOf(Color(0xFF064E3B), Color(0xFF0F2942))
                                else listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, AccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getTimeOfDayLabel(hour24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tempAlarm.getFormattedTime(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) AccentEmerald else Color(0xFF047857),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AM / PM Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(colors.cardBackgroundElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (!isPm) AccentEmerald else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { isPm = false }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "AM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isPm) Color.White else colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (isPm) AccentEmerald else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { isPm = true }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "PM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPm) Color.White else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hours Selector (1 - 12) with Zero Overlap Layout
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hour", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(text = "$hour12 ${if (isPm) "PM" else "AM"}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AccentEmerald)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { hour12 = if (hour12 > 1) hour12 - 1 else 12 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-1h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { hour12 = if (hour12 < 12) hour12 + 1 else 1 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+1h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    }
                }

                Slider(
                    value = hour12.toFloat(),
                    onValueChange = { hour12 = it.toInt() },
                    valueRange = 1f..12f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentEmerald,
                        activeTrackColor = AccentEmerald,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Minutes Selector (0 - 59) with Zero Overlap Layout
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Minute", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(text = String.format("%02d mins", minute), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryCyan)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { minute = (minute - 5 + 60) % 60 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-5m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { minute = (minute - 1 + 60) % 60 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-1m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { minute = (minute + 1) % 60 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+1m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { minute = (minute + 5) % 60 },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    }
                }

                Slider(
                    value = minute.toFloat(),
                    onValueChange = { minute = it.toInt() },
                    valueRange = 0f..59f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryCyan,
                        activeTrackColor = SecondaryCyan,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Label Input with dynamic time-of-day suggestion
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        userHasCustomizedLabel = true
                    },
                    label = { Text("Alarm Label") },
                    trailingIcon = {
                        if (userHasCustomizedLabel) {
                            IconButton(onClick = {
                                userHasCustomizedLabel = false
                                label = getTimeOfDayLabel(hour24)
                            }) {
                                Icon(Icons.Default.Sync, contentDescription = "Auto Label", tint = AccentEmerald)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentEmerald,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalLabel = label.ifBlank { getTimeOfDayLabel(hour24) }
                        val saved = SavedAlarmItem(
                            id = alarmToEdit?.id ?: System.currentTimeMillis(),
                            hour = hour24,
                            minute = minute,
                            label = finalLabel,
                            isEnabled = true
                        )
                        onSave(saved)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text(
                        text = if (isEditing) "Update Saved Alarm" else "Save & Enable Alarm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
