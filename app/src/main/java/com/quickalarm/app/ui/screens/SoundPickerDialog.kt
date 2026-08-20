package com.quickalarm.app.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickalarm.app.model.SoundItem
import com.quickalarm.app.ui.theme.*

@Composable
fun SoundPickerDialog(
    currentSound: SoundItem,
    onDismiss: () -> Unit,
    onSoundSelected: (SoundItem) -> Unit
) {
    val context = LocalContext.current
    var selectedSound by remember { mutableStateOf(currentSound) }
    var playingSoundId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Query all device system alarm tones asynchronously on first compose
    val installedSounds = remember { SoundItem.getInstalledDeviceAlarmSounds(context) }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            playingSoundId = null
        }
    }

    fun playAudio(sound: SoundItem) {
        stopAudio()
        try {
            val uri = sound.getUri(context) ?: return
            val player = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setOnCompletionListener {
                    playingSoundId = null
                    mediaPlayer = null
                }
                prepare()
                start()
            }
            mediaPlayer = player
            playingSoundId = sound.id
        } catch (e: Exception) {
            e.printStackTrace()
            playingSoundId = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopAudio()
        }
    }

    // Custom Audio Picker Launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Persistence flag not supported for all content providers
                }

                // Resolve file name
                var displayName = "Custom Audio Track"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }

                val customSound = SoundItem(
                    id = "custom_${System.currentTimeMillis()}",
                    title = displayName,
                    uriString = uri.toString(),
                    isCustom = true,
                    soundType = SoundItem.TYPE_LOCAL_FILE
                )
                selectedSound = customSound
                playAudio(customSound)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            stopAudio()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(min = 400.dp, max = 580.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
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
                                .background(Color(0xFF0E7490), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Alarm Sound Library",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${installedSounds.size} Device Sounds Available",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            stopAudio()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Sounds List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pick Custom Audio File Button
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF4338CA), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFF312E81), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "+ Pick Audio File from Storage",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "MP3, WAV, FLAC, OGG, AAC",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // User's custom picked sound if selected
                    if (selectedSound.isCustom) {
                        item {
                            val isPlaying = playingSoundId == selectedSound.id
                            SoundOptionRow(
                                title = selectedSound.title,
                                subtitle = "Custom Storage Audio",
                                isSelected = true,
                                isPlaying = isPlaying,
                                onSelect = { playAudio(selectedSound) },
                                onTogglePlay = {
                                    if (isPlaying) stopAudio() else playAudio(selectedSound)
                                }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "SYSTEM & OEM ALARM TONES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    // All system and device alarm sounds
                    items(installedSounds, key = { it.id + it.title }) { sound ->
                        val isSelected = selectedSound.id == sound.id || (!selectedSound.isCustom && selectedSound.title == sound.title)
                        val isPlaying = playingSoundId == sound.id

                        SoundOptionRow(
                            title = sound.title,
                            subtitle = if (sound.id == SoundItem.SOUND_ID_DEFAULT) "System Default Alarm" else "Device Ringtone",
                            isSelected = isSelected,
                            isPlaying = isPlaying,
                            onSelect = {
                                selectedSound = sound
                                playAudio(sound)
                            },
                            onTogglePlay = {
                                if (isPlaying) stopAudio() else playAudio(sound)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pinned Confirm Button (Never cut off!)
                Button(
                    onClick = {
                        stopAudio()
                        onSoundSelected(selectedSound)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        text = "Set as Alarm Sound",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SoundOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) PrimaryIndigo else SurfaceCardBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = PrimaryIndigo,
                        unselectedColor = TextMuted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isPlaying) Color(0xFF064E3B) else Color(0xFF334155),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop Preview" else "Play Preview",
                    tint = if (isPlaying) AccentEmerald else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
