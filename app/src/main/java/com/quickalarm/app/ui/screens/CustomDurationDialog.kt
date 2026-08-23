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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickalarm.app.ui.components.LiquidClockDial
import com.quickalarm.app.ui.components.LiquidSecondsSelectorBar
import com.quickalarm.app.ui.theme.*
import com.quickalarm.app.util.AlarmScheduler
import java.util.Calendar

@Composable
fun CustomDurationDialog(
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Int, label: String) -> Unit
) {
    val colors = AppTheme.colors

    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(45) }
    var seconds by remember { mutableIntStateOf(0) }

    val totalSeconds = (hours * 3600) + (minutes * 60) + seconds
    val totalMinutes = if (totalSeconds > 0) ((totalSeconds + 59) / 60) else 0
    val targetTimeMillis = System.currentTimeMillis() + (totalSeconds * 1000L)
    val scrollState = rememberScrollState()

    // Automatic time of day calculation based on target alarm trigger time
    fun getTimeOfDayPeriod(triggerMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = triggerMillis }
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..4 -> "Late Night"
            in 5..7 -> "Early Morning"
            in 8..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
    }

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
                                .background(if (colors.isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Custom Countdown Timer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Time-of-Day Category Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (colors.isDark) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, PrimaryIndigo.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (totalSeconds > 0) "${getTimeOfDayPeriod(targetTimeMillis)} Alarm (Rings at ${AlarmScheduler.formatTime(targetTimeMillis)})" else "Select duration (up to 24h)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) SecondaryCyan else PrimaryIndigo
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Large Digital Duration Readout with Seconds
                Text(
                    text = "${String.format("%02d", hours)}h : ${String.format("%02d", minutes)}m : ${String.format("%02d", seconds)}s",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryIndigo,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Asymmetric Dual Dials (Concentric 24h Hour Dial + Compact Minute Dial)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Concentric 24-Hour Dial (Outer: 0-11h, Inner: 12-23h)
                    LiquidClockDial(
                        selectedValue = hours,
                        range = 0..23,
                        isHourDial = true,
                        is24HourPreset = true,
                        title = "Hours (0-23h)",
                        dialRadiusDp = 72,
                        accentColor = PrimaryIndigo,
                        onValueChange = { hours = it }
                    )

                    // Compact Minute Dial
                    LiquidClockDial(
                        selectedValue = minutes,
                        range = 0..59,
                        isHourDial = false,
                        title = "Minutes (0-59m)",
                        dialRadiusDp = 58,
                        accentColor = PrimaryIndigo,
                        onValueChange = { minutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Precision Seconds Selector Bar
                LiquidSecondsSelectorBar(
                    seconds = seconds,
                    onSecondsChange = { seconds = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button with Dynamic Duration Label
                val durationText = when {
                    hours > 0 && minutes > 0 && seconds > 0 -> "+${hours}h ${minutes}m ${seconds}s"
                    hours > 0 && minutes > 0 -> "+${hours}h ${minutes}m"
                    hours > 0 && seconds > 0 -> "+${hours}h ${seconds}s"
                    hours > 0 -> "+${hours}h"
                    minutes > 0 && seconds > 0 -> "+${minutes}m ${seconds}s"
                    minutes > 0 -> "+${minutes}m"
                    seconds > 0 -> "+${seconds}s"
                    else -> "+0m"
                }

                Button(
                    onClick = {
                        if (totalSeconds > 0) {
                            val period = getTimeOfDayPeriod(targetTimeMillis)
                            val label = "$period Alarm ($durationText)"
                            onConfirm(totalMinutes, label)
                        }
                    },
                    enabled = totalSeconds > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo
                    )
                ) {
                    Text(
                        text = if (totalSeconds > 0) "Start Timer ($durationText)" else "Set Duration",
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
