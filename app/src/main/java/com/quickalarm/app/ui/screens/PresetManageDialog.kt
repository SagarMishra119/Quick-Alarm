package com.quickalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.quickalarm.app.model.PresetItem
import com.quickalarm.app.ui.theme.*
import com.quickalarm.app.util.AppSettings

@Composable
fun PresetManageDialog(
    presets: List<PresetItem>,
    onDismiss: () -> Unit,
    onPresetsChanged: (List<PresetItem>) -> Unit,
    onAddNewPreset: () -> Unit,
    onEditPreset: (PresetItem) -> Unit
) {
    val colors = AppTheme.colors
    var currentPresets by remember(presets) { mutableStateOf(presets) }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        if (toIndex in currentPresets.indices) {
            val list = currentPresets.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            currentPresets = list
            onPresetsChanged(list)
        }
    }

    fun deleteItem(index: Int) {
        if (currentPresets.size > 1) { // Keep at least 1 preset
            val list = currentPresets.toMutableList()
            list.removeAt(index)
            currentPresets = list
            onPresetsChanged(list)
        }
    }

    fun resetToDefaults() {
        val defaults = PresetItem.getDefaultPresets()
        currentPresets = defaults
        onPresetsChanged(defaults)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(min = 420.dp, max = 580.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Fixed Header Row
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Manage Quick Presets",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${currentPresets.size}/${AppSettings.MAX_PRESETS} Presets",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Presets List (Bounded with weight(1f))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(currentPresets, key = { _, item -> item.id }) { index, preset ->
                        val gradient = preset.getGradient()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackgroundElevated)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(gradient[0], CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = preset.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "${preset.minutes} mins • ${preset.subtitle}",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Move Up
                                    IconButton(
                                        onClick = { moveItem(index, index - 1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropUp,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) colors.textPrimary else colors.textMuted
                                        )
                                    }

                                    // Move Down
                                    IconButton(
                                        onClick = { moveItem(index, index + 1) },
                                        enabled = index < currentPresets.size - 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Move Down",
                                            tint = if (index < currentPresets.size - 1) colors.textPrimary else colors.textMuted
                                        )
                                    }

                                    // Edit Button
                                    IconButton(
                                        onClick = { onEditPreset(preset) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = SecondaryCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = { deleteItem(index) },
                                        enabled = currentPresets.size > 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = if (currentPresets.size > 1) Color(0xFFEF4444) else colors.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pinned Bottom Actions Row (Always Visible!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset Defaults Button
                    OutlinedButton(
                        onClick = { resetToDefaults() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Text("Reset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Add Preset Button
                    Button(
                        onClick = onAddNewPreset,
                        enabled = currentPresets.size < AppSettings.MAX_PRESETS,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Preset", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
