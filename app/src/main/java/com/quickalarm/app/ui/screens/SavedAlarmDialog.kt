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
    val isEditing = alarmToEdit != null

    var initialHour12 = if (alarmToEdit != null) {
        val h = alarmToEdit.hour
        when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
    } else 7

    var initialIsPm = if (alarmToEdit != null) alarmToEdit.hour >= 12 else false
    var initialMinute = alarmToEdit?.minute ?: 0

    var hour12 by remember { mutableIntStateOf(initialHour12) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var isPm by remember { mutableStateOf(initialIsPm) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "Morning Alarm") }

    // Convert 12-hour + AM/PM back to 24-hour
    val hour24 = when {
        isPm && hour12 < 12 -> hour12 + 12
        !isPm && hour12 == 12 -> 0
        else -> hour12
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
                .heightIn(max = 580.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
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
                                .background(Color(0xFF064E3B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "Edit Saved Alarm" else "New Saved Alarm",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Display Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF064E3B), Color(0xFF0F2942))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, AccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Scheduled Alarm Time",
                            fontSize = 11.sp,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tempAlarm.getFormattedTime(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentEmerald,
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
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
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
                                color = if (!isPm) Color.Black else TextSecondary
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
                                color = if (isPm) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hours Selector (1 - 12)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hour: $hour12", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { hour12 = if (hour12 > 1) hour12 - 1 else 12 },
                            modifier = Modifier.size(30.dp).background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1 hr", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { hour12 = if (hour12 < 12) hour12 + 1 else 1 },
                            modifier = Modifier.size(30.dp).background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1 hr", tint = Color.White, modifier = Modifier.size(16.dp))
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
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Minutes Selector (0 - 59)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Minute: ${String.format("%02d", minute)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { minute = (minute - 5 + 60) % 60 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { minute = (minute - 1 + 60) % 60 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { minute = (minute + 1) % 60 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { minute = (minute + 5) % 60 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Label Input
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label (e.g. Wake Up, Gym, Medicine)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentEmerald,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalLabel = label.ifBlank { "Alarm ${tempAlarm.getFormattedTime()}" }
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
                        color = Color.Black
                    )
                }
            }
        }
    }
}
