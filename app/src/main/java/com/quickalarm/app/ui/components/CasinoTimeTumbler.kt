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

/**
 * 🎰 Casino-Style 3-Column Time Tumbler Roller Composable.
 * Features 3 kinetic vertical wheels for Hours (00-23), Minutes (00-59), and Seconds (00-59).
 * Styled with frosted smoked glass, vibrant neon laser sightlines, and smooth snapping physics.
 */
@OptIn(ExperimentalFoundationApi::class)
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

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hours)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minutes)
    val secondState = rememberLazyListState(initialFirstVisibleItemIndex = seconds)

    // Sync programmatically when hours/minutes/seconds change externally (e.g. quick chips)
    LaunchedEffect(hours) {
        if (!hourState.isScrollInProgress && hourState.firstVisibleItemIndex != hours) {
            hourState.animateScrollToItem(hours.coerceIn(0, 23))
        }
    }
    LaunchedEffect(minutes) {
        if (!minuteState.isScrollInProgress && minuteState.firstVisibleItemIndex != minutes) {
            minuteState.animateScrollToItem(minutes.coerceIn(0, 59))
        }
    }
    LaunchedEffect(seconds) {
        if (!secondState.isScrollInProgress && secondState.firstVisibleItemIndex != seconds) {
            secondState.animateScrollToItem(seconds.coerceIn(0, 59))
        }
    }

    // Monitor scroll state and emit updates
    LaunchedEffect(hourState.firstVisibleItemIndex, minuteState.firstVisibleItemIndex, secondState.firstVisibleItemIndex) {
        val h = hourState.firstVisibleItemIndex.coerceIn(0, 23)
        val m = minuteState.firstVisibleItemIndex.coerceIn(0, 59)
        val s = secondState.firstVisibleItemIndex.coerceIn(0, 59)
        onTimeChange(h, m, s)
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
        // Main Casino Tumbler Housing Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (colors.isDark) Color(0xFF0F172A).copy(alpha = 0.45f) else Color(0xFFF1F5F9).copy(alpha = 0.65f)
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            SecondaryCyan.copy(alpha = 0.6f),
                            colors.surfaceBorder.copy(alpha = 0.4f),
                            AccentAmber.copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal Active Magnifier Lens (Center Row)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (colors.isDark) Color(0xFF1E293B).copy(alpha = 0.70f) else Color(0xFFFFFFFF).copy(alpha = 0.85f)
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                SecondaryCyan.copy(alpha = 0.5f),
                                PrimaryIndigo.copy(alpha = 0.3f),
                                AccentAmber.copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
            )

            // 3 Vertical Reels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: HOURS (00-23)
                TumblerReelColumn(
                    title = "HOURS",
                    count = 24,
                    state = hourState,
                    accentColor = SecondaryCyan,
                    modifier = Modifier.weight(1f)
                )

                // Divider Colon
                Text(
                    text = ":",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryCyan.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Column 2: MINUTES (00-59)
                TumblerReelColumn(
                    title = "MINS",
                    count = 60,
                    state = minuteState,
                    accentColor = SecondaryCyan,
                    modifier = Modifier.weight(1f)
                )

                // Divider Colon
                Text(
                    text = ":",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentAmber.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Column 3: SECONDS (00-59)
                TumblerReelColumn(
                    title = "SECS",
                    count = 60,
                    state = secondState,
                    accentColor = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Top & Bottom Gradient Fog Overlay for 3D Roller Cylindrical Depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)).copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Transparent,
                                (if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)).copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Jump Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickChips.forEach { (timeTriple, label) ->
                val isCurrent = hours == timeTriple.first && minutes == timeTriple.second && seconds == timeTriple.third
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
                                hourState.animateScrollToItem(timeTriple.first)
                                minuteState.animateScrollToItem(timeTriple.second)
                                secondState.animateScrollToItem(timeTriple.third)
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
 * Single vertical rolling tumbler reel with momentum snapping.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TumblerReelColumn(
    title: String,
    count: Int,
    state: LazyListState,
    accentColor: Color,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 56.dp
) {
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val colors = AppTheme.colors

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
            contentPadding = PaddingValues(vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 3)
        ) {
            items(count) { index ->
                val isSelected = state.firstVisibleItemIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 0.82f,
                    animationSpec = tween(120),
                    label = "reelScale"
                )
                val alphaVal by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.35f,
                    animationSpec = tween(120),
                    label = "reelAlpha"
                )

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", index),
                        fontSize = if (isSelected) 26.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
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
