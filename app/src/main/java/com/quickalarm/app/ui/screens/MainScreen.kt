package com.quickalarm.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.model.PresetItem
import com.quickalarm.app.model.SoundItem
import com.quickalarm.app.ui.theme.*
import com.quickalarm.app.util.AlarmScheduler
import com.quickalarm.app.util.AppSettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Live clock state
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Active alarms state
    var activeAlarms by remember { mutableStateOf(AlarmScheduler.getActiveAlarms(context)) }

    // Presets state
    var presets by remember { mutableStateOf(AppSettings.getPresets(context)) }

    // Sound and Snooze preferences
    var selectedSound by remember { mutableStateOf(AppSettings.getSelectedSound(context)) }
    var snoozeMinutes by remember { mutableIntStateOf(AppSettings.getSnoozeMinutes(context)) }

    // Confirmation banner state
    var lastScheduledAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    // Dialog visibility states
    var showCustomDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showManagePresetsDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<PresetItem?>(null) }
    var showEditPresetDialog by remember { mutableStateOf(false) }

    // Permission states
    var hasExactAlarmPermission by remember { mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // Permission Launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    // Live ticker loop
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            activeAlarms = AlarmScheduler.getActiveAlarms(context)
            hasExactAlarmPermission = AlarmScheduler.canScheduleExactAlarms(context)
            delay(1000)
        }
    }

    // Helper to schedule alarm
    fun setAlarm(minutes: Int, label: String) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val alarm = AlarmItem(
            id = System.currentTimeMillis(),
            triggerTimeMillis = triggerTime,
            durationMinutes = minutes,
            label = label
        )

        val success = AlarmScheduler.scheduleAlarm(context, alarm)
        if (success) {
            activeAlarms = AlarmScheduler.getActiveAlarms(context)
            lastScheduledAlarm = alarm
            showSuccessBanner = true
            Toast.makeText(
                context,
                "⏰ Alarm set for ${AlarmScheduler.formatTime(triggerTime)}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "Failed to set alarm. Check exact alarm permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Helper for quick 5-second test alarm
    fun setTestAlarm() {
        val triggerTime = System.currentTimeMillis() + 5000L // 5 seconds
        val alarm = AlarmItem(
            id = System.currentTimeMillis(),
            triggerTimeMillis = triggerTime,
            durationMinutes = 0,
            label = "⚡ Quick Test Alarm (5s)"
        )
        AlarmScheduler.scheduleAlarm(context, alarm)
        activeAlarms = AlarmScheduler.getActiveAlarms(context)
        lastScheduledAlarm = alarm
        showSuccessBanner = true
        Toast.makeText(context, "⚡ Test alarm scheduled for 5 seconds from now!", Toast.LENGTH_LONG).show()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Scaffold(
            containerColor = DarkBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                // 1. Top App Header & Live Clock with v2.0 badge
                item {
                    HeaderClockSection(currentTimeMillis = currentTimeMillis)
                }

                // 2. Permission Banners (if required)
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        NotificationPermissionBanner(
                            onRequestClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }
                }

                if (!hasExactAlarmPermission) {
                    item {
                        ExactAlarmPermissionBanner(
                            onGrantClick = {
                                AlarmScheduler.openExactAlarmSettings(context)
                            }
                        )
                    }
                }

                // 3. Success Scheduled Confirmation Banner
                item {
                    AnimatedVisibility(
                        visible = showSuccessBanner && lastScheduledAlarm != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        lastScheduledAlarm?.let { alarm ->
                            ScheduledConfirmationCard(
                                alarm = alarm,
                                onDismiss = { showSuccessBanner = false }
                            )
                        }
                    }
                }

                // 4. ACTIVE ALARMS SECTION (MOVED UPWARDS)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ACTIVE ALARMS (${activeAlarms.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.5.sp
                            )
                        }

                        if (activeAlarms.isNotEmpty()) {
                            Text(
                                text = "Tap trash to cancel",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (activeAlarms.isEmpty()) {
                    item {
                        EmptyAlarmsState()
                    }
                } else {
                    items(activeAlarms, key = { it.id }) { alarm ->
                        ActiveAlarmCard(
                            alarm = alarm,
                            onCancel = {
                                AlarmScheduler.cancelAlarm(context, alarm)
                                activeAlarms = AlarmScheduler.getActiveAlarms(context)
                                Toast.makeText(context, "Alarm cancelled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // 5. ONE-TAP ALARMS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ONE-TAP PRESETS (${presets.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Manage Presets Button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { showManagePresetsDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Manage",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manage",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Test +5s button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF312E81))
                                    .clickable { setTestAlarm() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+5s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Grid of One-Tap Preset Buttons (Responsive 2 per row)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (row in presets.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (preset in row) {
                                    PresetAlarmButton(
                                        preset = preset,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            setAlarm(preset.minutes, "${preset.title} Quick Alarm")
                                        }
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 6. Custom Duration Button
                item {
                    CustomAlarmButton(
                        onClick = { showCustomDialog = true }
                    )
                }

                // 7. PREFERENCES SECTION (Sound & Snooze)
                item {
                    Text(
                        text = "PREFERENCES & SETTINGS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sound Card
                        PreferenceCard(
                            icon = Icons.Default.MusicNote,
                            iconColor = SecondaryCyan,
                            title = "Alarm Sound",
                            value = selectedSound.title,
                            modifier = Modifier.weight(1f),
                            onClick = { showSoundDialog = true }
                        )

                        // Snooze Card
                        PreferenceCard(
                            icon = Icons.Default.Snooze,
                            iconColor = AccentAmber,
                            title = "Snooze Time",
                            value = "$snoozeMinutes Minutes",
                            modifier = Modifier.weight(1f),
                            onClick = { showSnoozeDialog = true }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // Custom Duration Modal Dialog
        if (showCustomDialog) {
            CustomDurationDialog(
                onDismiss = { showCustomDialog = false },
                onConfirm = { minutes, label ->
                    showCustomDialog = false
                    setAlarm(minutes, label)
                }
            )
        }

        // Sound Picker Dialog
        if (showSoundDialog) {
            SoundPickerDialog(
                currentSound = selectedSound,
                onDismiss = { showSoundDialog = false },
                onSoundSelected = { newSound ->
                    selectedSound = newSound
                    AppSettings.setSelectedSound(context, newSound)
                    showSoundDialog = false
                    Toast.makeText(context, "Alarm sound updated: ${newSound.title}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Snooze Settings Dialog
        if (showSnoozeDialog) {
            SnoozeDurationDialog(
                currentSnoozeMinutes = snoozeMinutes,
                onDismiss = { showSnoozeDialog = false },
                onSnoozeSelected = { newMinutes ->
                    snoozeMinutes = newMinutes
                    AppSettings.setSnoozeMinutes(context, newMinutes)
                    showSnoozeDialog = false
                    Toast.makeText(context, "Snooze duration set to $newMinutes min", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Manage Presets Dialog
        if (showManagePresetsDialog) {
            PresetManageDialog(
                presets = presets,
                onDismiss = { showManagePresetsDialog = false },
                onPresetsChanged = { updatedList ->
                    presets = updatedList
                    AppSettings.savePresets(context, updatedList)
                },
                onAddNewPreset = {
                    presetToEdit = null
                    showEditPresetDialog = true
                },
                onEditPreset = { preset ->
                    presetToEdit = preset
                    showEditPresetDialog = true
                }
            )
        }

        // Add / Edit Preset Dialog
        if (showEditPresetDialog) {
            PresetEditDialog(
                presetToEdit = presetToEdit,
                onDismiss = {
                    showEditPresetDialog = false
                    presetToEdit = null
                },
                onSave = { savedPreset ->
                    if (presetToEdit != null) {
                        AppSettings.updatePreset(context, savedPreset)
                    } else {
                        AppSettings.addPreset(context, savedPreset)
                    }
                    presets = AppSettings.getPresets(context)
                    showEditPresetDialog = false
                    presetToEdit = null
                    Toast.makeText(context, "Preset saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun HeaderClockSection(currentTimeMillis: Long) {
    val timeFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(PrimaryIndigo, SecondaryCyan)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quick Alarm",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF4338CA), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "v2.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Instant Offline Alarms",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1B4B), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF4338CA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "100% Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Time Ticker Display
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = dateFormat.format(Date(currentTimeMillis)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Text(
                    text = timeFormat.format(Date(currentTimeMillis)),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryCyan,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun PresetAlarmButton(
    preset: PresetItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = preset.getGradient()

    Card(
        modifier = modifier
            .height(96.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = 0.25f),
                            Color(0xFF1E293B)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradient[0].copy(alpha = 0.6f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(gradient[0].copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = gradient[0],
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = preset.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PreferenceCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CustomAlarmButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(GradientCustom),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(GradientCustom),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "+ Custom Duration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Set exact hours & minutes",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Pick Time",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledConfirmationCard(
    alarm: AlarmItem,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))
                    )
                )
                .border(1.dp, AccentEmerald, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alarm Scheduled Successfully!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Set for ${AlarmScheduler.formatTime(alarm.triggerTimeMillis)} (${AlarmScheduler.formatRemainingTime(alarm.triggerTimeMillis)})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA7F3D0)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveAlarmCard(
    alarm: AlarmItem,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF334155), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = alarm.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rings at ${AlarmScheduler.formatTime(alarm.triggerTimeMillis)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryCyan
                    )
                    Text(
                        text = AlarmScheduler.formatRemainingTime(alarm.triggerTimeMillis),
                        fontSize = 11.sp,
                        color = AccentAmber
                    )
                }
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .background(Color(0xFF451A03), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel Alarm",
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyAlarmsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmOff,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "No Active Alarms Scheduled",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "Tap a preset below or set a custom duration",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}
