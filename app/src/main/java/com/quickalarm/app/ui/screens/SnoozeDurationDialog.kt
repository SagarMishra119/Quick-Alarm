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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
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
import com.quickalarm.app.ui.theme.*
import com.quickalarm.app.util.AppSettings

@Composable
fun SnoozeDurationDialog(
    currentSnoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnoozeSelected: (Int) -> Unit
) {
    val colors = AppTheme.colors
    var selectedMinutes by remember { mutableIntStateOf(currentSnoozeMinutes) }
    val presets = AppSettings.SNOOZE_PRESETS
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 560.dp)
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
                                .background(if (colors.isDark) Color(0xFF78350F) else Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                tint = if (colors.isDark) AccentAmber else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Snooze Duration",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Selected Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (colors.isDark) Color(0xFF451A03) else Color(0xFFFFFBEB), RoundedCornerShape(16.dp))
                        .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Active Snooze Interval",
                            fontSize = 11.sp,
                            color = if (colors.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$selectedMinutes Minutes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (colors.isDark) AccentAmber else Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Preset Chips
                Text(
                    text = "QUICK PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { minutes ->
                        val isSelected = selectedMinutes == minutes
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) AccentAmber else colors.chipBackground,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AccentAmber else colors.surfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMinutes = minutes }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${minutes}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Interval Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CUSTOM INTERVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { selectedMinutes = (selectedMinutes - 5).coerceAtLeast(1) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("-5m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { selectedMinutes = (selectedMinutes - 1).coerceAtLeast(1) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("-1m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { selectedMinutes = (selectedMinutes + 1).coerceAtMost(60) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("+1m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        Button(
                            onClick = { selectedMinutes = (selectedMinutes + 5).coerceAtMost(60) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.chipBackground),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("+5m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = selectedMinutes.toFloat(),
                    onValueChange = { selectedMinutes = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentAmber,
                        activeTrackColor = AccentAmber,
                        inactiveTrackColor = colors.chipBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSnoozeSelected(selectedMinutes) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                ) {
                    Text(
                        text = "Save Snooze Interval",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
