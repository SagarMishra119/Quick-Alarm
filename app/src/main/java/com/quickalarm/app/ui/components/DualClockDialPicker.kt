package com.quickalarm.app.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickalarm.app.ui.theme.AccentAmber
import com.quickalarm.app.ui.theme.AppTheme
import com.quickalarm.app.ui.theme.PrimaryIndigo
import com.quickalarm.app.ui.theme.SecondaryCyan
import kotlin.math.*

/**
 * Ultra-translucent liquid circular clock dial.
 * Supports asymmetric sizing, 12-hour or 24-hour concentric tracks, and smooth 60fps touch physics.
 */
@Composable
fun LiquidClockDial(
    selectedValue: Int,
    range: IntRange,
    title: String,
    dialRadiusDp: Int = 78,
    isHourDial: Boolean = false,
    is24HourPreset: Boolean = false,
    accentColor: Color = SecondaryCyan,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.colors.isDark
    val numberColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val ringColor = if (isDark) Color(0xFF475569).copy(alpha = 0.40f) else Color(0xFFCBD5E1).copy(alpha = 0.60f)
    val liquidDialBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.28f) else Color(0xFFFFFFFF).copy(alpha = 0.45f)

    // Calculate angle (0 deg is top / 12 o'clock)
    val targetAngle = when {
        is24HourPreset -> {
            ((selectedValue % 12).toFloat() / 12f) * 360f
        }
        isHourDial && range.first == 1 && range.last == 12 -> {
            ((selectedValue % 12).toFloat() / 12f) * 360f
        }
        else -> {
            // Minutes or Seconds: 0..59 (60 steps)
            (selectedValue.toFloat() / 60f) * 360f
        }
    }

    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 100),
        label = "clockHandAngle"
    )

    // Hand length ratio for concentric 24-hour dial (inner track if >= 12, outer if < 12)
    val isInnerTrack = is24HourPreset && selectedValue >= 12
    val animatedHandRatio by animateFloatAsState(
        targetValue = if (isInnerTrack) 0.60f else 0.88f,
        animationSpec = tween(durationMillis = 150),
        label = "handLengthRatio"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            color = AppTheme.colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size((dialRadiusDp * 2).dp)
                .clip(CircleShape)
                .background(liquidDialBg)
                .border(
                    width = 1.2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            ringColor,
                            accentColor.copy(alpha = 0.3f),
                            ringColor,
                            accentColor.copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(range, isHourDial, is24HourPreset) {
                    val updateValue = { offset: Offset, size: androidx.compose.ui.unit.IntSize ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        val maxRadius = min(cx, cy)

                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angleDeg < 0) angleDeg += 360f

                        val fraction = (angleDeg % 360f) / 360f

                        when {
                            is24HourPreset -> {
                                var hourBase = (fraction * 12).roundToInt()
                                if (hourBase >= 12) hourBase = 0

                                // If touched inside inner 62% radius -> hours 12..23, outer -> 0..11
                                val isInnerTouch = distance < (maxRadius * 0.68f)
                                val finalHour = if (isInnerTouch) hourBase + 12 else hourBase
                                onValueChange(finalHour.coerceIn(0, 23))
                            }
                            isHourDial && range.first == 1 && range.last == 12 -> {
                                var hour = (fraction * 12).roundToInt()
                                if (hour == 0) hour = 12
                                onValueChange(hour.coerceIn(1, 12))
                            }
                            else -> {
                                var value = (fraction * 60).roundToInt()
                                if (value >= 60) value = 0
                                onValueChange(value.coerceIn(range.first, range.last))
                            }
                        }
                    }

                    detectTapGestures { offset -> updateValue(offset, size) }
                }
                .pointerInput(range, isHourDial, is24HourPreset) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = change.position.x - cx
                        val dy = change.position.y - cy
                        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        val maxRadius = min(cx, cy)

                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angleDeg < 0) angleDeg += 360f

                        val fraction = (angleDeg % 360f) / 360f

                        when {
                            is24HourPreset -> {
                                var hourBase = (fraction * 12).roundToInt()
                                if (hourBase >= 12) hourBase = 0
                                val isInnerTouch = distance < (maxRadius * 0.68f)
                                val finalHour = if (isInnerTouch) hourBase + 12 else hourBase
                                onValueChange(finalHour.coerceIn(0, 23))
                            }
                            isHourDial && range.first == 1 && range.last == 12 -> {
                                var hour = (fraction * 12).roundToInt()
                                if (hour == 0) hour = 12
                                onValueChange(hour.coerceIn(1, 12))
                            }
                            else -> {
                                var value = (fraction * 60).roundToInt()
                                if (value >= 60) value = 0
                                onValueChange(value.coerceIn(range.first, range.last))
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerRadius = min(cx, cy) - 6.dp.toPx()
                val handRadius = outerRadius * animatedHandRatio

                // 1. Draw outer tick marks
                val tickCount = if (isHourDial || is24HourPreset) 12 else 60
                for (i in 0 until tickCount) {
                    val tickAngle = (i.toFloat() / tickCount.toFloat()) * 2 * PI.toFloat() - (PI / 2).toFloat()
                    val isMajor = if (isHourDial || is24HourPreset) true else (i % 5 == 0)
                    val tickLen = if (isMajor) 6.dp.toPx() else 3.dp.toPx()
                    val tickColor = if (isMajor) accentColor.copy(alpha = 0.65f) else ringColor.copy(alpha = 0.40f)
                    val strokeW = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()

                    val startX = cx + (outerRadius - tickLen) * cos(tickAngle)
                    val startY = cy + (outerRadius - tickLen) * sin(tickAngle)
                    val endX = cx + outerRadius * cos(tickAngle)
                    val endY = cy + outerRadius * sin(tickAngle)

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW
                    )
                }

                // If 24-hour preset dial: Draw subtle inner concentric orbit ring
                if (is24HourPreset) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.15f),
                        radius = outerRadius * 0.60f,
                        center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Draw Hand & Glowing Selector Pin
                val handRad = Math.toRadians((animatedAngle - 90f).toDouble()).toFloat()
                val thumbX = cx + handRadius * cos(handRad)
                val thumbY = cy + handRadius * sin(handRad)

                // Glow halo
                drawCircle(
                    color = accentColor.copy(alpha = 0.28f),
                    radius = 14.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )

                // Hand Line
                drawLine(
                    color = accentColor,
                    start = Offset(cx, cy),
                    end = Offset(thumbX, thumbY),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Thumb pin
                drawCircle(
                    color = accentColor,
                    radius = 7.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )

                // Center pivot
                drawCircle(
                    color = accentColor,
                    radius = 4.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // 3. Draw Number Labels
                val labelPaint = Paint().apply {
                    color = numberColor.toArgb()
                    textSize = (if (isHourDial) 11.5f else 10f).sp.toPx()
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }

                val innerLabelPaint = Paint().apply {
                    color = numberColor.copy(alpha = 0.70f).toArgb()
                    textSize = 8.5.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                drawIntoCanvas { canvas ->
                    if (is24HourPreset) {
                        // Outer Track: 0 .. 11
                        val outerNumbers = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
                        val outerLabelRadius = outerRadius - 13.dp.toPx()

                        for ((idx, num) in outerNumbers.withIndex()) {
                            val numAngle = (idx.toFloat() / 12f) * 2 * PI.toFloat() - (PI / 2).toFloat()
                            val numX = cx + outerLabelRadius * cos(numAngle)
                            val numY = cy + outerLabelRadius * sin(numAngle) + (labelPaint.textSize / 3f)

                            val isSelected = selectedValue == num
                            labelPaint.color = if (isSelected) accentColor.toArgb() else numberColor.toArgb()
                            canvas.nativeCanvas.drawText("$num", numX, numY, labelPaint)
                        }

                        // Inner Track: 12 .. 23
                        val innerNumbers = listOf(12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)
                        val innerLabelRadius = outerRadius * 0.60f

                        for ((idx, num) in innerNumbers.withIndex()) {
                            val numAngle = (idx.toFloat() / 12f) * 2 * PI.toFloat() - (PI / 2).toFloat()
                            val numX = cx + innerLabelRadius * cos(numAngle)
                            val numY = cy + innerLabelRadius * sin(numAngle) + (innerLabelPaint.textSize / 3f)

                            val isSelected = selectedValue == num
                            innerLabelPaint.color = if (isSelected) accentColor.toArgb() else numberColor.copy(alpha = 0.6f).toArgb()
                            canvas.nativeCanvas.drawText("$num", numX, numY, innerLabelPaint)
                        }
                    } else {
                        val numbersToDraw = if (isHourDial) {
                            listOf(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
                        } else {
                            listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
                        }

                        val numberRadius = outerRadius - 13.dp.toPx()

                        for ((idx, num) in numbersToDraw.withIndex()) {
                            val numAngle = (idx.toFloat() / numbersToDraw.size.toFloat()) * 2 * PI.toFloat() - (PI / 2).toFloat()
                            val numX = cx + numberRadius * cos(numAngle)
                            val numY = cy + numberRadius * sin(numAngle) + (labelPaint.textSize / 3f)

                            val isSelected = if (isHourDial) {
                                num == selectedValue || (num == 12 && selectedValue == 0)
                            } else {
                                abs(num - selectedValue) < 3
                            }

                            labelPaint.color = if (isSelected) accentColor.toArgb() else numberColor.toArgb()
                            canvas.nativeCanvas.drawText(
                                if (num < 10 && !isHourDial) "0$num" else "$num",
                                numX,
                                numY,
                                labelPaint
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Precision Liquid Seconds Selector Bar.
 * Provides instant second-level setting (00s, 15s, 30s, 45s) and +/- fine stepper.
 */
@Composable
fun LiquidSecondsSelectorBar(
    seconds: Int,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val quickSeconds = listOf(0, 15, 30, 45)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (colors.isDark) Color(0xFF1E293B).copy(alpha = 0.35f) else Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
            .border(
                1.dp,
                if (colors.isDark) Color(0xFF334155).copy(alpha = 0.40f) else Color(0xFFCBD5E1),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRECISION SECONDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSecondsChange((seconds - 1 + 60) % 60) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "-1s",
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = String.format("%02ds", seconds),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentAmber,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onSecondsChange((seconds + 1) % 60) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "+1s",
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickSeconds.forEach { s ->
                val isSelected = seconds == s
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) AccentAmber else (if (colors.isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFE2E8F0))
                        )
                        .clickable { onSecondsChange(s) }
                        .padding(vertical = 5.dp)
                ) {
                    Text(
                        text = String.format("%02ds", s),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else colors.textPrimary
                    )
                }
            }
        }
    }
}

/**
 * Liquid frosted glass AM / PM segmented toggle switch.
 */
@Composable
fun AmPmSegmentedTab(
    isAm: Boolean,
    onAmSelected: () -> Unit,
    onPmSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.colors.isDark
    val bgTrack = if (isDark) Color(0xFF1E293B).copy(alpha = 0.50f) else Color(0xFFE2E8F0).copy(alpha = 0.70f)
    val activePillBg = SecondaryCyan
    val activeTextColor = Color.White
    val inactiveTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgTrack)
            .border(1.dp, AppTheme.colors.surfaceBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        // AM Pill
        val amBg by animateColorAsState(if (isAm) activePillBg else Color.Transparent, label = "amBg")
        val amText by animateColorAsState(if (isAm) activeTextColor else inactiveTextColor, label = "amText")
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(amBg)
                .clickable { onAmSelected() }
                .padding(horizontal = 24.dp, vertical = 7.dp)
        ) {
            Text(
                text = "AM",
                color = amText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // PM Pill
        val pmBg by animateColorAsState(if (!isAm) activePillBg else Color.Transparent, label = "pmBg")
        val pmText by animateColorAsState(if (!isAm) activeTextColor else inactiveTextColor, label = "pmText")
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pmBg)
                .clickable { onPmSelected() }
                .padding(horizontal = 24.dp, vertical = 7.dp)
        ) {
            Text(
                text = "PM",
                color = pmText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
