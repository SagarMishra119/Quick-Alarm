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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.quickalarm.app.R
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.model.PresetItem
import com.quickalarm.app.model.SavedAlarmItem
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
    val colors = AppTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current

    // In-memory data states
    var activeAlarms by remember { mutableStateOf(AlarmScheduler.getActiveAlarms(context)) }
    var presets by remember { mutableStateOf(AppSettings.getPresets(context)) }
    var savedAlarms by remember { mutableStateOf(AppSettings.getSavedAlarms(context)) }
    var selectedSound by remember { mutableStateOf(AppSettings.getSelectedSound(context)) }
    var snoozeMinutes by remember { mutableIntStateOf(AppSettings.getSnoozeMinutes(context)) }

    // Instant lifecycle refresh: reload alarms whenever MainActivity resumes (e.g. after snooze/dismiss)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                activeAlarms = AlarmScheduler.getActiveAlarms(context)
                savedAlarms = AppSettings.getSavedAlarms(context)
                presets = AppSettings.getPresets(context)
                selectedSound = AppSettings.getSelectedSound(context)
                snoozeMinutes = AppSettings.getSnoozeMinutes(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Confirmation banner state
    var lastScheduledAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    // Dialog states
    var showCustomDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showManagePresetsDialog by remember { mutableStateOf(false) }
    var presetToEdit by remember { mutableStateOf<PresetItem?>(null) }
    var showEditPresetDialog by remember { mutableStateOf(false) }
    var savedAlarmToEdit by remember { mutableStateOf<SavedAlarmItem?>(null) }
    var showSavedAlarmDialog by remember { mutableStateOf(false) }

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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    // Background sync: automatically updates active alarms list if modified outside MainScreen (e.g. Snooze from notification)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            val freshAlarms = AlarmScheduler.getActiveAlarms(context)
            if (freshAlarms != activeAlarms) {
                activeAlarms = freshAlarms
            }
            val freshSaved = AppSettings.getSavedAlarms(context)
            if (freshSaved != savedAlarms) {
                savedAlarms = freshSaved
            }
        }
    }

    // Helper to schedule an alarm item
    fun scheduleNewAlarm(alarm: AlarmItem) {
        val success = AlarmScheduler.scheduleAlarm(context, alarm)
        if (success) {
            activeAlarms = AlarmScheduler.getActiveAlarms(context)
            lastScheduledAlarm = alarm
            showSuccessBanner = true
            Toast.makeText(
                context,
                "⏰ Alarm set for ${AlarmScheduler.formatTime(alarm.triggerTimeMillis)}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            hasExactAlarmPermission = AlarmScheduler.canScheduleExactAlarms(context)
            Toast.makeText(
                context,
                "Failed to set alarm. Please check permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setQuickPresetAlarm(minutes: Int, label: String) {
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val alarm = AlarmItem(
            id = System.currentTimeMillis(),
            triggerTimeMillis = triggerTime,
            durationMinutes = minutes,
            label = label
        )
        scheduleNewAlarm(alarm)
    }

    fun setTestAlarm() {
        val triggerTime = System.currentTimeMillis() + 5000L
        val alarm = AlarmItem(
            id = System.currentTimeMillis(),
            triggerTimeMillis = triggerTime,
            durationMinutes = 0,
            label = "⚡ Quick Test Alarm (5s)"
        )
        scheduleNewAlarm(alarm)
    }

    // Toggle Saved Alarm ON/OFF (Generates unique active alarm ID to avoid collision)
    fun toggleSavedAlarm(saved: SavedAlarmItem, enable: Boolean) {
        val updated = saved.copy(isEnabled = enable)
        AppSettings.updateSavedAlarm(context, updated)
        savedAlarms = AppSettings.getSavedAlarms(context)

        if (enable) {
            val triggerTime = updated.getNextTriggerTimeMillis()
            val alarmItem = AlarmItem(
                id = System.currentTimeMillis(),
                triggerTimeMillis = triggerTime,
                durationMinutes = ((triggerTime - System.currentTimeMillis()) / 60000L).toInt().coerceAtLeast(1),
                label = updated.label
            )
            scheduleNewAlarm(alarmItem)
        } else {
            val existing = activeAlarms.find { it.label == saved.label }
            if (existing != null) {
                AlarmScheduler.cancelAlarm(context, existing)
                activeAlarms = AlarmScheduler.getActiveAlarms(context)
            }
            Toast.makeText(context, "${updated.getFormattedTime()} alarm disabled", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. AESTHETIC THEMED BACKGROUND ---
        if (colors.isDark) {
            // Dark Mode: Full Moon, Starry Night Sky Wallpaper + Dark Scrim for 100% UI Clarity
            Image(
                painter = painterResource(id = R.drawable.bg_dark_mode),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B14).copy(alpha = 0.45f))
            )
        } else {
            // Light Mode: Vibrant Sky-Blue Gradient + Soft Morning Sun Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors.backgroundGradient))
            )

            // Early Morning Sunrise Sun Orb Glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(240.dp)
                    .offset(x = 60.dp, y = (-40).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDE047).copy(alpha = 0.50f),
                                Color(0xFFFBBF24).copy(alpha = 0.25f),
                                Color(0xFFBAE6FD).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // --- 2. FOREGROUND APP CONTENT ---
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                // Top Header & Live Clock with v3.4 Badge (Clean, No "100% Offline" clutter)
                item {
                    HeaderClockSection()
                }

                // Permissions Banners (if needed)
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
                                hasExactAlarmPermission = AlarmScheduler.canScheduleExactAlarms(context)
                            }
                        )
                    }
                }

                // Success Scheduled Banner
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

                // ACTIVE ALARMS SECTION
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
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.sectionHeaderColor,
                                letterSpacing = 1.5.sp
                            )
                        }

                        if (activeAlarms.isNotEmpty()) {
                            Text(
                                text = "Tap trash to cancel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                if (activeAlarms.isEmpty()) {
                    item {
                        EmptyAlarmsState()
                    }
                } else {
                    items(activeAlarms, key = { "active_${it.id}" }) { alarm ->
                        ActiveAlarmCard(
                            alarm = alarm,
                            onCancel = {
                                AlarmScheduler.cancelAlarm(context, alarm)
                                activeAlarms = AlarmScheduler.getActiveAlarms(context)
                                val savedMatch = savedAlarms.find { it.label == alarm.label }
                                if (savedMatch != null && savedMatch.isEnabled) {
                                    val updated = savedMatch.copy(isEnabled = false)
                                    AppSettings.updateSavedAlarm(context, updated)
                                    savedAlarms = AppSettings.getSavedAlarms(context)
                                }
                                Toast.makeText(context, "Alarm cancelled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // SAVED CLOCK ALARMS SECTION (Fixed daily times, e.g. 7:00 AM)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SAVED CLOCK ALARMS (${savedAlarms.size}/${AppSettings.MAX_SAVED_ALARMS})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.sectionHeaderColor,
                                letterSpacing = 1.5.sp
                            )
                        }

                        if (savedAlarms.size < AppSettings.MAX_SAVED_ALARMS) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (colors.isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5))
                                    .clickable {
                                        savedAlarmToEdit = null
                                        showSavedAlarmDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (colors.isDark) AccentEmerald else Color(0xFF065F46),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Add Alarm",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (colors.isDark) Color.White else Color(0xFF065F46)
                                )
                            }
                        }
                    }
                }

                if (savedAlarms.isEmpty()) {
                    item {
                        EmptySavedAlarmsState(
                            onAddClick = {
                                savedAlarmToEdit = null
                                showSavedAlarmDialog = true
                            }
                        )
                    }
                } else {
                    items(savedAlarms, key = { "saved_${it.id}" }) { saved ->
                        SavedAlarmRowCard(
                            saved = saved,
                            onToggle = { enable -> toggleSavedAlarm(saved, enable) },
                            onEdit = {
                                savedAlarmToEdit = saved
                                showSavedAlarmDialog = true
                            },
                            onDelete = {
                                AppSettings.deleteSavedAlarm(context, saved.id)
                                savedAlarms = AppSettings.getSavedAlarms(context)
                                val running = activeAlarms.find { it.label == saved.label }
                                if (running != null) {
                                    AlarmScheduler.cancelAlarm(context, running)
                                    activeAlarms = AlarmScheduler.getActiveAlarms(context)
                                }
                                Toast.makeText(context, "Saved alarm deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // ONE-TAP PRESETS SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ONE-TAP PRESETS (${presets.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.sectionHeaderColor,
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
                                    .background(colors.cardBackgroundElevated)
                                    .border(1.dp, colors.surfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { showManagePresetsDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Manage", tint = PrimaryIndigo, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            }

                            // Test +5s Button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (colors.isDark) Color(0xFF312E81) else Color(0xFFE0E7FF))
                                    .clickable { setTestAlarm() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("+5s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (colors.isDark) Color.White else PrimaryIndigo)
                            }
                        }
                    }
                }

                // Grid of One-Tap Presets (Clean single alarm icon & no overlap)
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
                                            setQuickPresetAlarm(preset.minutes, "${preset.title} Quick Alarm")
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

                // Custom Countdown Timer Button (Clean: removed "+" symbol)
                item {
                    CustomCountdownButton(onClick = { showCustomDialog = true })
                }

                // PREFERENCES SECTION (High-visibility header)
                item {
                    Text(
                        text = "PREFERENCES & SETTINGS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.sectionHeaderColor,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PreferenceCard(
                            icon = Icons.Default.MusicNote,
                            iconColor = SecondaryCyan,
                            title = "Alarm Sound",
                            value = selectedSound.title,
                            modifier = Modifier.weight(1f),
                            onClick = { showSoundDialog = true }
                        )

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

        // --- ALL DIALOGS ---

        // Custom Countdown Timer Dialog
        if (showCustomDialog) {
            CustomDurationDialog(
                onDismiss = { showCustomDialog = false },
                onConfirm = { minutes, label ->
                    showCustomDialog = false
                    setQuickPresetAlarm(minutes, label)
                }
            )
        }

        // Sound Picker Dialog (Full System Library + Local Audio)
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

        // Saved Clock Alarm Add/Edit Dialog
        if (showSavedAlarmDialog) {
            SavedAlarmDialog(
                alarmToEdit = savedAlarmToEdit,
                onDismiss = {
                    showSavedAlarmDialog = false
                    savedAlarmToEdit = null
                },
                onSave = { savedAlarm ->
                    if (savedAlarmToEdit != null) {
                        AppSettings.updateSavedAlarm(context, savedAlarm)
                    } else {
                        AppSettings.addSavedAlarm(context, savedAlarm)
                    }
                    savedAlarms = AppSettings.getSavedAlarms(context)
                    showSavedAlarmDialog = false
                    savedAlarmToEdit = null

                    // If enabled, schedule the alarm with a unique ID
                    if (savedAlarm.isEnabled) {
                        val triggerTime = savedAlarm.getNextTriggerTimeMillis()
                        val alarmItem = AlarmItem(
                            id = System.currentTimeMillis(),
                            triggerTimeMillis = triggerTime,
                            durationMinutes = ((triggerTime - System.currentTimeMillis()) / 60000L).toInt().coerceAtLeast(1),
                            label = savedAlarm.label
                        )
                        scheduleNewAlarm(alarmItem)
                    }
                    Toast.makeText(context, "Saved alarm ${savedAlarm.getFormattedTime()} updated", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

/**
 * Isolated Clock Section:
 * Live ticker runs strictly inside this Composable.
 */
@Composable
fun HeaderClockSection() {
    val colors = AppTheme.colors
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val timeFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(colors.headerGradient),
                shape = RoundedCornerShape(24.dp)
            )
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(
                                if (colors.isDark) listOf(PrimaryIndigo, SecondaryCyan)
                                else listOf(AccentAmber, Color(0xFFF97316))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (colors.isDark) Icons.Default.NightsStay else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Quick Alarm",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryIndigo, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v3.4",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = if (colors.isDark) "Night Sky & Offline Alarms" else "Morning Sunrise & Offline Alarms",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = dateFormat.format(Date(currentTimeMillis)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted
                )
                Text(
                    text = timeFormat.format(Date(currentTimeMillis)),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) SecondaryCyan else Color(0xFF0284C7),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SavedAlarmRowCard(
    saved: SavedAlarmItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (saved.isEnabled) {
                if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.55f) else Color(0xFFD1FAE5)
            } else colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (saved.isEnabled) AccentEmerald.copy(alpha = 0.6f) else colors.surfaceBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onEdit() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = saved.getFormattedTime(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (saved.isEnabled) colors.textPrimary else colors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = saved.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (saved.isEnabled) (if (colors.isDark) AccentEmerald else Color(0xFF047857)) else colors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(
                    checked = saved.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentEmerald,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.chipBackground
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF87171).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySavedAlarmsState(
    onAddClick: () -> Unit
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackgroundElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onAddClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.chipBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No Saved Clock Alarms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Tap to save daily alarm times (e.g. 7:00 AM)",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
            Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = AccentEmerald, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun PresetAlarmButton(
    preset: PresetItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val gradient = preset.getGradient()

    Card(
        modifier = modifier
            .height(96.dp)
            .shadow(if (colors.isDark) 6.dp else 2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = if (colors.isDark) 0.30f else 0.12f),
                            colors.surface
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradient[0].copy(alpha = 0.6f), colors.surfaceBorder.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(gradient[0].copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = gradient[0],
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = preset.subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .height(84.dp)
            .shadow(if (colors.isDark) 4.dp else 2.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(18.dp))
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
                            color = colors.textMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CustomCountdownButton(
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(if (colors.isDark) 6.dp else 2.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(colors.customButtonBg)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(GradientCustom),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
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
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(GradientCustom),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Custom Countdown Timer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Set exact duration from now (e.g. 45m)",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (colors.isDark) Color(0xFF334155).copy(alpha = 0.9f) else Color(0xFFE0E7FF),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pick Timer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) SecondaryCyan else PrimaryIndigo
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
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.85f) else Color(0xFFD1FAE5)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        if (colors.isDark) listOf(Color(0xFF064E3B).copy(alpha = 0.9f), Color(0xFF022C22).copy(alpha = 0.9f))
                        else listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))
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
                        color = if (colors.isDark) Color.White else Color(0xFF065F46)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Set for ${AlarmScheduler.formatTime(alarm.triggerTimeMillis)} (${AlarmScheduler.formatRemainingTime(alarm.triggerTimeMillis)})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
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
    val colors = AppTheme.colors

    var remainingText by remember(alarm.triggerTimeMillis) {
        mutableStateOf(AlarmScheduler.formatRemainingTime(alarm.triggerTimeMillis))
    }

    LaunchedEffect(alarm.triggerTimeMillis) {
        while (true) {
            remainingText = AlarmScheduler.formatRemainingTime(alarm.triggerTimeMillis)
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
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
                        .background(colors.chipBackground, CircleShape),
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
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Rings at ${AlarmScheduler.formatTime(alarm.triggerTimeMillis)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (colors.isDark) SecondaryCyan else Color(0xFF0284C7)
                    )
                    Text(
                        text = remainingText,
                        fontSize = 11.sp,
                        color = AccentAmber
                    )
                }
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .background(if (colors.isDark) Color(0xFF451A03) else Color(0xFFFEE2E2), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel Alarm",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyAlarmsState() {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackgroundElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.surfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(colors.chipBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmOff,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "No Active Alarms Scheduled",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Text(
                    text = "Toggle a saved alarm or tap a quick preset below",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}
