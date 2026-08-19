package com.quickalarm.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.quickalarm.app.model.AlarmItem
import com.quickalarm.app.ui.theme.AccentAmber
import com.quickalarm.app.ui.theme.AccentEmerald
import com.quickalarm.app.ui.theme.DarkBackground
import com.quickalarm.app.ui.theme.GradientCustom
import com.quickalarm.app.ui.theme.GradientPreset15m
import com.quickalarm.app.ui.theme.GradientPreset1h
import com.quickalarm.app.ui.theme.GradientPreset2h
import com.quickalarm.app.ui.theme.GradientPreset30m
import com.quickalarm.app.ui.theme.GradientPreset4h
import com.quickalarm.app.ui.theme.GradientPreset6h
import com.quickalarm.app.ui.theme.PrimaryIndigo
import com.quickalarm.app.ui.theme.SecondaryCyan
import com.quickalarm.app.ui.theme.SurfaceCard
import com.quickalarm.app.ui.theme.SurfaceCardBorder
import com.quickalarm.app.ui.theme.TextMuted
import com.quickalarm.app.ui.theme.TextPrimary
import com.quickalarm.app.ui.theme.TextSecondary
import com.quickalarm.app.util.AlarmScheduler
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickPreset(
    val minutes: Int,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Live clock state
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Active alarms state
    var activeAlarms by remember { mutableStateOf(AlarmScheduler.getActiveAlarms(context)) }

    // Last set alarm banner state
    var lastScheduledAlarm by remember { mutableStateOf<AlarmItem?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    // Custom duration dialog state
    var showCustomDialog by remember { mutableStateOf(false) }

    // Permission state checks
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

    // Timer loop to update live clock and countdown timers every second
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
                "Failed to set alarm. Check permissions.",
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

    val presets = listOf(
        QuickPreset(15, "+15 Min", "Short Break", GradientPreset15m),
        QuickPreset(30, "+30 Min", "Power Nap", GradientPreset30m),
        QuickPreset(60, "+1 Hr", "Focus Session", GradientPreset1h),
        QuickPreset(120, "+2 Hrs", "Deep Work", GradientPreset2h),
        QuickPreset(240, "+4 Hrs", "Half Day", GradientPreset4h),
        QuickPreset(360, "+6 Hrs", "Full Sleep", GradientPreset6h)
    )

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
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Top App Header & Live Clock
                item {
                    HeaderClockSection(currentTimeMillis = currentTimeMillis)
                }

                // Permission Banners if required
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

                // Success Scheduled Confirmation Card
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

                // Section Title: Quick One-Tap Alarms
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ONE-TAP ALARMS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF312E81))
                                .clickable { setTestAlarm() }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test +5s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Grid of One-Tap Preset Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (row in presets.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (preset in row) {
                                    QuickAlarmButton(
                                        preset = preset,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            setAlarm(preset.minutes, "${preset.title} Quick Alarm")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Duration Button
                item {
                    CustomAlarmButton(
                        onClick = { showCustomDialog = true }
                    )
                }

                // Section Title: Active Alarms
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
                }

                // Active Alarms List
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

                item { Spacer(modifier = Modifier.height(24.dp)) }
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
                        Text(
                            text = "Quick Alarm",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Instant Offline Timer",
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
fun QuickAlarmButton(
    preset: QuickPreset,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
                            preset.gradient[0].copy(alpha = 0.25f),
                            Color(0xFF1E293B)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(preset.gradient[0].copy(alpha = 0.6f), Color.Transparent)
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
                            .background(preset.gradient[0].copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = preset.gradient[0],
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
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AlarmOff,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Active Alarms",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any quick duration button above to schedule an alarm instantly.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
