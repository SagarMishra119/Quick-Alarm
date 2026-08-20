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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Remove
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

    val totalMinutes = (hours * 60) + minutes
    val targetTimeMillis = System.currentTimeMillis() + (totalMinutes * 60 * 1000L)
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

    val quickPresets = listOf(
        Pair(5, "5m"),
        Pair(10, "10m"),
        Pair(15, "15m"),
        Pair(30, "30m"),
        Pair(45, "45m"),
        Pair(60, "1h"),
        Pair(90, "1.5h"),
        Pair(120, "2h"),
        Pair(180, "3h"),
        Pair(480, "8h")
    )

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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row (Clean: removed "+")
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

                Spacer(modifier = Modifier.height(14.dp))

                // Target Time Preview Box with Dynamic Time of Day Label
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                if (colors.isDark) listOf(Color(0xFF1E1B4B), Color(0xFF0F2942))
                                else listOf(Color(0xFFEEF2FF), Color(0xFFE0F2FE))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalMinutes > 0) "${getTimeOfDayPeriod(targetTimeMillis)} Alarm at" else "Select duration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (colors.isDark) SecondaryCyan else PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalMinutes > 0) AlarmScheduler.formatTime(targetTimeMillis) else "--:--",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        if (totalMinutes > 0) {
                            Text(
                                text = "in ${if (hours > 0) "${hours}h " else ""}${minutes}m",
                                fontSize = 13.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Presets Row
                Text(
                    text = "QUICK JUMP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPresets) { preset ->
                        Box(
                            modifier = Modifier
                                .background(colors.chipBackground, RoundedCornerShape(12.dp))
                                .clickable {
                                    hours = preset.first / 60
                                    minutes = preset.first % 60
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.second,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (colors.isDark) SecondaryCyan else PrimaryIndigo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hours Adjuster (Structured layout to prevent overlap)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(text = "${hours}h", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryIndigo)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (hours > 0) hours-- },
                            enabled = hours > 0,
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-1h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { if (hours < 24) hours++ },
                            enabled = hours < 24,
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
                    value = hours.toFloat(),
                    onValueChange = { hours = it.toInt() },
                    valueRange = 0f..24f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryIndigo,
                        activeTrackColor = PrimaryIndigo,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Minutes Adjuster (Structured row prevents overlap!)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Minutes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(text = "$minutes mins", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryCyan)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { minutes = (minutes - 5).coerceAtLeast(0) },
                            enabled = minutes > 0 || hours > 0,
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-5m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { if (minutes > 0) minutes-- },
                            enabled = minutes > 0 || hours > 0,
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-1m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { if (minutes < 59) minutes++ },
                            enabled = minutes < 59,
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+1m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { minutes = (minutes + 5).coerceAtMost(59) },
                            enabled = minutes < 59,
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
                    value = minutes.toFloat(),
                    onValueChange = { minutes = it.toInt() },
                    valueRange = 0f..59f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryCyan,
                        activeTrackColor = SecondaryCyan,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button with Smart Time-of-Day Label
                Button(
                    onClick = {
                        if (totalMinutes > 0) {
                            val period = getTimeOfDayPeriod(targetTimeMillis)
                            val durationText = when {
                                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                                hours > 0 -> "${hours}h"
                                else -> "${minutes}m"
                            }
                            val label = "$period ($durationText)"
                            onConfirm(totalMinutes, label)
                        }
                    },
                    enabled = totalMinutes > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo
                    )
                ) {
                    Text(
                        text = "Start Countdown Alarm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
