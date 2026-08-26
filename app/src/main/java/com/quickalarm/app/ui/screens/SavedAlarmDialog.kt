package com.quickalarm.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickalarm.app.model.SavedAlarmItem
import com.quickalarm.app.ui.components.AmPmSegmentedTab
import com.quickalarm.app.ui.components.LiquidClockDial
import com.quickalarm.app.ui.components.LiquidSecondsSelectorBar
import com.quickalarm.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ScheduleType {
    EVERYDAY,
    CUSTOM_DAYS,
    SPECIFIC_DATE
}

@Composable
fun SavedAlarmDialog(
    alarmToEdit: SavedAlarmItem? = null,
    onDismiss: () -> Unit,
    onSave: (SavedAlarmItem) -> Unit
) {
    val context = LocalContext.current
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

    // Schedule Mode State
    val initialScheduleType = when {
        alarmToEdit?.specificDateMillis != null -> ScheduleType.SPECIFIC_DATE
        alarmToEdit?.repeatDays != null && alarmToEdit.repeatDays.isNotEmpty() && alarmToEdit.repeatDays.size < 7 -> ScheduleType.CUSTOM_DAYS
        else -> ScheduleType.EVERYDAY
    }

    var scheduleType by remember { mutableStateOf(initialScheduleType) }
    var selectedRepeatDays by remember {
        mutableStateOf(alarmToEdit?.repeatDays ?: setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
        ))
    }

    var specificDateMillis by remember {
        mutableStateOf(
            alarmToEdit?.specificDateMillis ?: Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }

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

    val finalRepeatDays = when (scheduleType) {
        ScheduleType.EVERYDAY -> emptySet()
        ScheduleType.CUSTOM_DAYS -> if (selectedRepeatDays.isEmpty()) setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY) else selectedRepeatDays
        ScheduleType.SPECIFIC_DATE -> emptySet()
    }

    val finalSpecificDate = when (scheduleType) {
        ScheduleType.SPECIFIC_DATE -> specificDateMillis
        else -> null
    }

    val tempAlarm = SavedAlarmItem(
        id = alarmToEdit?.id ?: System.currentTimeMillis(),
        hour = hour24,
        minute = minute,
        label = label,
        isEnabled = true,
        repeatDays = finalRepeatDays,
        specificDateMillis = finalSpecificDate
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp)
                .padding(vertical = 4.dp),
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
                                .background(if (colors.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (colors.isDark) AccentEmerald else Color(0xFF065F46),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "Edit Alarm" else "Set Alarm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Top AM / PM Segmented Tab
                AmPmSegmentedTab(
                    isAm = !isPm,
                    onAmSelected = { isPm = false },
                    onPmSelected = { isPm = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic Time-of-Day Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, AccentEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = getTimeOfDayLabel(hour24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Large Digital Time Readout with Seconds
                Text(
                    text = "${String.format("%02d", hour12)} : ${String.format("%02d", minute)} : ${String.format("%02d", seconds)} ${if (isPm) "PM" else "AM"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryCyan,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Dual Circular Dials (Slightly reduced sizes for perfect alignment)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Dial (64dp radius)
                    LiquidClockDial(
                        selectedValue = hour12,
                        range = 1..12,
                        isHourDial = true,
                        title = "Hours (1-12)",
                        dialRadiusDp = 64,
                        accentColor = SecondaryCyan,
                        onValueChange = { hour12 = it }
                    )

                    // Minute Dial (50dp radius)
                    LiquidClockDial(
                        selectedValue = minute,
                        range = 0..59,
                        isHourDial = false,
                        title = "Minutes (0-59)",
                        dialRadiusDp = 50,
                        accentColor = SecondaryCyan,
                        onValueChange = { minute = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Precision Seconds Selector Bar
                LiquidSecondsSelectorBar(
                    seconds = seconds,
                    onSecondsChange = { seconds = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ================= REPEAT & SCHEDULE SECTION =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF1E293B).copy(alpha = 0.55f) else Color(0xFFF1F5F9))
                        .border(1.dp, colors.surfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SCHEDULE TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // 3-Way Mode Segmented Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ScheduleTypeTab(
                            title = "Everyday",
                            icon = Icons.Default.Repeat,
                            isSelected = scheduleType == ScheduleType.EVERYDAY,
                            onClick = { scheduleType = ScheduleType.EVERYDAY },
                            modifier = Modifier.weight(1f)
                        )
                        ScheduleTypeTab(
                            title = "Custom Days",
                            icon = Icons.Default.DateRange,
                            isSelected = scheduleType == ScheduleType.CUSTOM_DAYS,
                            onClick = { scheduleType = ScheduleType.CUSTOM_DAYS },
                            modifier = Modifier.weight(1.1f)
                        )
                        ScheduleTypeTab(
                            title = "Date 📅",
                            icon = Icons.Default.Event,
                            isSelected = scheduleType == ScheduleType.SPECIFIC_DATE,
                            onClick = { scheduleType = ScheduleType.SPECIFIC_DATE },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Content for Custom Days
                    AnimatedVisibility(
                        visible = scheduleType == ScheduleType.CUSTOM_DAYS,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            // 7 Days Circular Chips
                            val daysOfWeek = listOf(
                                Calendar.SUNDAY to "S",
                                Calendar.MONDAY to "M",
                                Calendar.TUESDAY to "T",
                                Calendar.WEDNESDAY to "W",
                                Calendar.THURSDAY to "T",
                                Calendar.FRIDAY to "F",
                                Calendar.SATURDAY to "S"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                daysOfWeek.forEach { (dayCal, letter) ->
                                    val isSelected = dayCal in selectedRepeatDays
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) SecondaryCyan else (if (colors.isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                                            )
                                            .clickable {
                                                selectedRepeatDays = if (isSelected) {
                                                    selectedRepeatDays - dayCal
                                                } else {
                                                    selectedRepeatDays + dayCal
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = letter,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Day Presets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickFilterChip(
                                    label = "Weekdays (M-F)",
                                    isSelected = selectedRepeatDays == setOf(
                                        Calendar.MONDAY,
                                        Calendar.TUESDAY,
                                        Calendar.WEDNESDAY,
                                        Calendar.THURSDAY,
                                        Calendar.FRIDAY
                                    ),
                                    onClick = {
                                        selectedRepeatDays = setOf(
                                            Calendar.MONDAY,
                                            Calendar.TUESDAY,
                                            Calendar.WEDNESDAY,
                                            Calendar.THURSDAY,
                                            Calendar.FRIDAY
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickFilterChip(
                                    label = "Weekends (S-S)",
                                    isSelected = selectedRepeatDays == setOf(Calendar.SATURDAY, Calendar.SUNDAY),
                                    onClick = {
                                        selectedRepeatDays = setOf(Calendar.SATURDAY, Calendar.SUNDAY)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Content for Specific Date
                    AnimatedVisibility(
                        visible = scheduleType == ScheduleType.SPECIFIC_DATE,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            val sdf = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                            val dateStr = sdf.format(Date(specificDateMillis))

                            // Display Selected Date Card with Edit Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF))
                                    .border(1.dp, SecondaryCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance().apply { timeInMillis = specificDateMillis }
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val chosen = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, y)
                                                    set(Calendar.MONTH, m)
                                                    set(Calendar.DAY_OF_MONTH, d)
                                                    set(Calendar.HOUR_OF_DAY, 0)
                                                    set(Calendar.MINUTE, 0)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                specificDateMillis = chosen.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).apply {
                                            datePicker.minDate = System.currentTimeMillis()
                                        }.show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = SecondaryCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Date Selector Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickDateChip(
                                    label = "Tomorrow",
                                    onClick = {
                                        val cal = Calendar.getInstance().apply {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        specificDateMillis = cal.timeInMillis
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickDateChip(
                                    label = "This Weekend",
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        val daysUntilSat = (Calendar.SATURDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
                                        cal.add(Calendar.DAY_OF_YEAR, if (daysUntilSat == 0) 7 else daysUntilSat)
                                        cal.set(Calendar.HOUR_OF_DAY, 0)
                                        cal.set(Calendar.MINUTE, 0)
                                        cal.set(Calendar.SECOND, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        specificDateMillis = cal.timeInMillis
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

                // Save Action Button
                Button(
                    onClick = {
                        val finalLabel = label.ifBlank { getTimeOfDayLabel(hour24) }
                        val saved = SavedAlarmItem(
                            id = alarmToEdit?.id ?: System.currentTimeMillis(),
                            hour = hour24,
                            minute = minute,
                            label = finalLabel,
                            isEnabled = true,
                            repeatDays = finalRepeatDays,
                            specificDateMillis = finalSpecificDate
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
                        text = if (isEditing) "Update Alarm (${tempAlarm.getFormattedTime()} • ${tempAlarm.getScheduleSummary()})"
                        else "Set Alarm (${tempAlarm.getFormattedTime()} • ${tempAlarm.getScheduleSummary()})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

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

@Composable
private fun ScheduleTypeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) SecondaryCyan else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else colors.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else colors.textSecondary
            )
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) SecondaryCyan.copy(alpha = 0.25f) else (if (colors.isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
            )
            .border(
                1.dp,
                if (isSelected) SecondaryCyan else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) SecondaryCyan else colors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickDateChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}
