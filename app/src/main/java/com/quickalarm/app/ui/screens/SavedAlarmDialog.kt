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
import com.quickalarm.app.ui.components.AmPmSegmentedTab
import com.quickalarm.app.ui.components.LiquidClockDial
import com.quickalarm.app.ui.components.LiquidSecondsSelectorBar
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
    val initialMinute = alarmToEdit?.minute ?: 30

    var hour12 by remember { mutableIntStateOf(initialHour12) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var seconds by remember { mutableIntStateOf(0) }
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
                .heightIn(max = 700.dp)
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (colors.isDark) Color(0xFF111827).copy(alpha = 0.88f) else Color(0xFFFFFFFF).copy(alpha = 0.92f)
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
                    .padding(20.dp),
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
                            text = if (isEditing) "Edit Alarm" else "Set Alarm",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Top AM / PM Segmented Tab
                AmPmSegmentedTab(
                    isAm = !isPm,
                    onAmSelected = { isPm = false },
                    onPmSelected = { isPm = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Time-of-Day Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, AccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getTimeOfDayLabel(hour24),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Large Digital Time Readout with Seconds
                Text(
                    text = "${String.format("%02d", hour12)} : ${String.format("%02d", minute)} : ${String.format("%02d", seconds)} ${if (isPm) "PM" else "AM"}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Asymmetric Dual Circular Dials (Dominant Hour Dial + Compact Minute Dial)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dominant Hour Dial
                    LiquidClockDial(
                        selectedValue = hour12,
                        range = 1..12,
                        isHourDial = true,
                        title = "Hours (1-12)",
                        dialRadiusDp = 72,
                        accentColor = SecondaryCyan,
                        onValueChange = { hour12 = it }
                    )

                    // Compact Minute Dial
                    LiquidClockDial(
                        selectedValue = minute,
                        range = 0..59,
                        isHourDial = false,
                        title = "Minutes (0-59)",
                        dialRadiusDp = 58,
                        accentColor = SecondaryCyan,
                        onValueChange = { minute = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Precision Seconds Selector Bar
                LiquidSecondsSelectorBar(
                    seconds = seconds,
                    onSecondsChange = { seconds = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Label Input Field
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
                                Icon(Icons.Default.Sync, contentDescription = "Auto Label", tint = SecondaryCyan)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryCyan,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Action Button
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
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCyan)
                ) {
                    Text(
                        text = if (isEditing) "Update Alarm (${tempAlarm.getFormattedTime()})" else "Set Alarm (${tempAlarm.getFormattedTime()})",
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
