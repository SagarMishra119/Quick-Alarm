package com.quickalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quickalarm.app.ui.theme.GradientCustom
import com.quickalarm.app.ui.theme.PrimaryIndigo
import com.quickalarm.app.ui.theme.SecondaryCyan
import com.quickalarm.app.ui.theme.SurfaceCard
import com.quickalarm.app.ui.theme.TextMuted
import com.quickalarm.app.ui.theme.TextPrimary
import com.quickalarm.app.ui.theme.TextSecondary
import com.quickalarm.app.util.AlarmScheduler

@Composable
fun CustomDurationDialog(
    onDismiss: () -> Unit,
    onConfirm: (durationMinutes: Int, label: String) -> Unit
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(45) }

    val totalMinutes = (hours * 60) + minutes
    val targetTimeMillis = System.currentTimeMillis() + (totalMinutes * 60 * 1000L)

    val quickPresets = listOf(
        Pair(5, "+5m"),
        Pair(10, "+10m"),
        Pair(45, "+45m"),
        Pair(90, "+1.5h"),
        Pair(180, "+3h"),
        Pair(480, "+8h")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = SecondaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom Duration",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Time Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F2942))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalMinutes > 0) "Alarm will trigger at" else "Select duration",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalMinutes > 0) AlarmScheduler.formatTime(targetTimeMillis) else "--:--",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SecondaryCyan
                        )
                        if (totalMinutes > 0) {
                            Text(
                                text = "in $hours hrs $minutes mins",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hours Picker Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hours: $hours", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (hours > 0) hours-- },
                            enabled = hours > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { if (hours < 24) hours++ },
                            enabled = hours < 24,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White)
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
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Minutes Picker Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Minutes: $minutes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (minutes > 0) minutes-- },
                            enabled = minutes > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { if (minutes < 59) minutes++ },
                            enabled = minutes < 59,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White)
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
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPresets) { preset ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF334155), RoundedCornerShape(12.dp))
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
                                color = SecondaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (totalMinutes > 0) {
                            val label = when {
                                hours > 0 && minutes > 0 -> "+${hours}h ${minutes}m Custom Alarm"
                                hours > 0 -> "+${hours}h Custom Alarm"
                                else -> "+${minutes}m Custom Alarm"
                            }
                            onConfirm(totalMinutes, label)
                        }
                    },
                    enabled = totalMinutes > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryIndigo
                    )
                ) {
                    Text(
                        text = "Schedule Custom Alarm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
