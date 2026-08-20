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
    val isEditing = presetToEdit != null

    var hours by remember { mutableIntStateOf((presetToEdit?.minutes ?: 45) / 60) }
    var minutes by remember { mutableIntStateOf((presetToEdit?.minutes ?: 45) % 60) }
    
    // Auto-generate title helper
    fun formatTitle(h: Int, m: Int): String {
        return when {
            h > 0 && m > 0 -> "+${h}h ${m}m"
            h > 0 -> "+${h} Hr" + if (h > 1) "s" else ""
            else -> "+${m} Min"
        }
    }

    var title by remember {
        mutableStateOf(presetToEdit?.title ?: formatTitle(hours, minutes))
    }
    var subtitle by remember {
        mutableStateOf(presetToEdit?.subtitle ?: "Quick Alarm")
    }
    var selectedColorKey by remember {
        mutableStateOf(presetToEdit?.colorKey ?: "indigo")
    }
    var isUserCustomTitle by remember {
        mutableStateOf(presetToEdit != null)
    }

    val totalMinutes = (hours * 60) + minutes
    val scrollState = rememberScrollState()

    // When hours/minutes change, if user hasn't explicitly overridden title with a custom name, auto-sync title
    fun updateTime(newHours: Int, newMinutes: Int) {
        hours = newHours.coerceIn(0, 24)
        minutes = newMinutes.coerceIn(0, 59)
        if (!isUserCustomTitle) {
            title = formatTitle(hours, minutes)
        }
    }

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
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF312E81), CircleShape),
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
                            text = if (isEditing) "Edit Preset Alarm" else "New Preset Alarm",
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

                // Duration Selector Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F2942))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Preset Time",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalMinutes > 0) {
                                "${if (hours > 0) "${hours}h " else ""}${minutes}m (${totalMinutes} total mins)"
                            } else "0m (Select a duration)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SecondaryCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- MINUTES SELECTOR (ANY EXACT MINUTE 0 - 59) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minutes: $minutes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Stepper buttons: -5m, -1m, +1m, +5m
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { updateTime(hours, (minutes - 5).coerceAtLeast(0)) },
                            enabled = minutes > 0 || hours > 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { updateTime(hours, if (minutes > 0) minutes - 1 else 59) },
                            enabled = minutes > 0 || hours > 0,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { updateTime(hours, (minutes + 1) % 60) },
                            enabled = minutes < 59 || hours < 24,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { updateTime(hours, (minutes + 5).coerceAtMost(59)) },
                            enabled = minutes < 59,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { updateTime(hours, it.toInt()) },
                    valueRange = 0f..59f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryCyan,
                        activeTrackColor = SecondaryCyan,
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // --- HOURS SELECTOR (0 - 24) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hours: $hours",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { updateTime(hours - 1, minutes) },
                            enabled = hours > 0,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-1h", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { updateTime(hours + 1, minutes) },
                            enabled = hours < 24,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+1h", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Slider(
                    value = hours.toFloat(),
                    onValueChange = { updateTime(it.toInt(), minutes) },
                    valueRange = 0f..24f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryIndigo,
                        activeTrackColor = PrimaryIndigo,
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Button Title Input with Auto-Sync Icon
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        isUserCustomTitle = true
                    },
                    label = { Text("Button Title") },
                    trailingIcon = {
                        IconButton(onClick = {
                            isUserCustomTitle = false
                            title = formatTitle(hours, minutes)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Auto-Sync Title",
                                tint = SecondaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle Input
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Label / Purpose (e.g. Power Nap, Workout)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Color Themes
                Text("THEME COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PresetItem.AVAILABLE_COLORS) { (colorKey, _) ->
                        val dummyPreset = PresetItem(minutes = 1, title = "", subtitle = "", colorKey = colorKey)
                        val color = dummyPreset.getPrimaryColor()
                        val isSelected = selectedColorKey == colorKey

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorKey = colorKey },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { formatTitle(hours, minutes) }
                        val finalSubtitle = subtitle.ifBlank { "Quick Alarm" }
                        val newPreset = PresetItem(
                            id = presetToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                            minutes = totalMinutes.coerceAtLeast(1),
                            title = finalTitle,
                            subtitle = finalSubtitle,
                            colorKey = selectedColorKey
                        )
                        onSave(newPreset)
                    },
                    enabled = totalMinutes > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        text = if (isEditing) "Update Preset" else "Add Preset",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
