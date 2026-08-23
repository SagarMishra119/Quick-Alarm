package com.quickalarm.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickalarm.app.ui.theme.AccentAmber
import com.quickalarm.app.ui.theme.AppTheme
import com.quickalarm.app.ui.theme.PrimaryIndigo
import com.quickalarm.app.ui.theme.SecondaryCyan
import kotlinx.coroutines.launch

private const val VIRTUAL_MULTIPLIER = 1000

/**
 * 🎰 Casino-Style Infinite Looping 3-Column Time Tumbler Roller.
 * Features 3 continuous 360° virtual reels for Hours (00-23), Minutes (00-59), and Seconds (00-59).
 * Fully centralized with pixel-perfect alignment, smooth inertia snapping, and glowing laser magnifier lens.
 */
@Composable
fun CasinoTimeTumbler(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onTimeChange: (h: Int, m: Int, s: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()

    val hourBaseOffset = remember { (VIRTUAL_MULTIPLIER / 2) * 24 }
    val minuteBaseOffset = remember { (VIRTUAL_MULTIPLIER / 2) * 60 }
    val secondBaseOffset = remember { (VIRTUAL_MULTIPLIER / 2) * 60 }

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hourBaseOffset + (hours % 24))
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minuteBaseOffset + (minutes % 60))
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = secondBaseOffset + (seconds % 60))

    // Derive current selected values from first visible snapped items
    val currentSelectedHour by remember { derivedStateOf { hourState.firstVisibleItemIndex % 24 } }
    val currentSelectedMinute by remember { derivedStateOf { minuteState.firstVisibleItemIndex % 60 } }
    val currentSelectedSecond by remember { derivedStateOf { secondState.firstVisibleItemIndex % 60 } }

    // Emit live changes when snapped state updates
    LaunchedEffect(currentSelectedHour, currentSelectedMinute, currentSelectedSecond) {
        onTimeChange(currentSelectedHour, currentSelectedMinute, currentSelectedSecond)
    }

    // Programmatic sync when values change externally (e.g. quick chips)
    LaunchedEffect(hours) {
        val targetVal = hours % 24
        if (!hourState.isScrollInProgress && currentSelectedHour != targetVal) {
            val currentIdx = hourState.firstVisibleItemIndex
            val currentMod = currentIdx % 24
            val delta = targetVal - currentMod
            hourState.animateScrollToItem(currentIdx + delta)
        }
    }
    LaunchedEffect(minutes) {
        val targetVal = minutes % 60
        if (!minuteState.isScrollInProgress && currentSelectedMinute != targetVal) {
            val currentIdx = minuteState.firstVisibleItemIndex
            val currentMod = currentIdx % 60
            val delta = targetVal - currentMod
            minuteState.animateScrollToItem(currentIdx + delta)
        }
    }
    LaunchedEffect(seconds) {
        val targetVal = seconds % 60
        if (!secondState.isScrollInProgress && currentSelectedSecond != targetVal) {
            val currentIdx = secondState.firstVisibleItemIndex
            val currentMod = currentIdx % 60
            val delta = targetVal - currentMod
            secondState.animateScrollToItem(currentIdx + delta)
        }
    }

    val quickChips = listOf(
        Triple(0, 15, 0) to "15m",
        Triple(0, 30, 0) to "30m",
        Triple(1, 0, 0) to "1h",
        Triple(2, 0, 0) to "2h",
        Triple(4, 0, 0) to "4h",
        Triple(8, 0, 0) to "8h"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Main Casino Tumbler Housing Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (colors.isDark) Color(0xFF0F172A).copy(alpha = 0.50f) else Color(0xFFF1F5F9).copy(alpha = 0.70f)
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            SecondaryCyan.copy(alpha = 0.55f),
                            colors.surfaceBorder.copy(alpha = 0.40f),
                            AccentAmber.copy(alpha = 0.55f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal Center Laser Magnifier Lens (Active Row)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (colors.isDark) Color(0xFF1E293B).copy(alpha = 0.75f) else Color(0xFFFFFFFF).copy(alpha = 0.90f)
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                SecondaryCyan.copy(alpha = 0.6f),
                                PrimaryIndigo.copy(alpha = 0.35f),
                                AccentAmber.copy(alpha = 0.6f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
            )

            // 3 Looping Wheels in Perfect Alignment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: HOURS (00-23 looping)
                LoopingTumblerReel(
                    title = "HOURS",
                    count = 24,
                    state = hourState,
                    selectedModValue = currentSelectedHour,
                    accentColor = SecondaryCyan,
                    modifier = Modifier.weight(1f)
                )

                // Colon Separator
                Text(
                    text = ":",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryCyan.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(14.dp)
                )

                // Column 2: MINUTES (00-59 looping)
                LoopingTumblerReel(
                    title = "MINS",
                    count = 60,
                    state = minuteState,
                    selectedModValue = currentSelectedMinute,
                    accentColor = SecondaryCyan,
                    modifier = Modifier.weight(1f)
                )

                // Colon Separator
                Text(
                    text = ":",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentAmber.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(14.dp)
                )

                // Column 3: SECONDS (00-59 looping)
                LoopingTumblerReel(
                    title = "SECS",
                    count = 60,
                    state = secondState,
                    selectedModValue = currentSelectedSecond,
                    accentColor = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Top & Bottom Gradient Fog Overlays for 3D Roller Curvature
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)).copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Transparent,
                                (if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)).copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Jump Preset Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickChips.forEach { (timeTriple, label) ->
                val isCurrent = currentSelectedHour == timeTriple.first &&
                        currentSelectedMinute == timeTriple.second &&
                        currentSelectedSecond == timeTriple.third

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isCurrent) SecondaryCyan else (if (colors.isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFE2E8F0))
                        )
                        .border(
                            1.dp,
                            if (isCurrent) SecondaryCyan else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                val curH = hourState.firstVisibleItemIndex
                                hourState.animateScrollToItem(curH + (timeTriple.first - (curH % 24)))

                                val curM = minuteState.firstVisibleItemIndex
                                minuteState.animateScrollToItem(curM + (timeTriple.second - (curM % 60)))

                                val curS = secondState.firstVisibleItemIndex
                                secondState.animateScrollToItem(curS + (timeTriple.third - (curS % 60)))
                            }
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color.Black else colors.textPrimary
                    )
                }
            }
        }
    }
}

/**
 * High-precision infinite looping reel with vertical snapping and exact geometric centering.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoopingTumblerReel(
    title: String,
    count: Int,
    state: LazyListState,
    selectedModValue: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp
) {
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val colors = AppTheme.colors
    val totalVirtualCount = count * VIRTUAL_MULTIPLIER

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        LazyColumn(
            state = state,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 3)
        ) {
            items(
                count = totalVirtualCount,
                key = { index -> index }
            ) { index ->
                val modValue = index % count
                val isSelected = modValue == selectedModValue

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 0.80f,
                    animationSpec = tween(100),
                    label = "reelScale"
                )
                val alphaVal by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.30f,
                    animationSpec = tween(100),
                    label = "reelAlpha"
                )

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", modValue),
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) accentColor else colors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .scale(scale)
                            .alpha(alphaVal)
                    )
                }
            }
        }
    }
}
