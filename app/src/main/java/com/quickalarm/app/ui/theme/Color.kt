package com.quickalarm.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Base Brand Colors (Consistent in both modes)
val PrimaryIndigo = Color(0xFF6366F1)
val SecondaryCyan = Color(0xFF06B6D4)
val AccentAmber   = Color(0xFFF59E0B)
val AccentEmerald = Color(0xFF10B981)
val AccentRose    = Color(0xFFF43F5E)
val AccentPurple  = Color(0xFF8B5CF6)

// Button Gradients
val GradientPreset15m = listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
val GradientPreset30m = listOf(Color(0xFF06B6D4), Color(0xFF0D9488))
val GradientPreset1h  = listOf(Color(0xFF10B981), Color(0xFF059669))
val GradientPreset2h  = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
val GradientPreset4h  = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
val GradientPreset6h  = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
val GradientCustom    = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))

// Adaptive App Color Scheme Data Class
data class AppPalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundGradient: List<Color>,
    val surface: Color,
    val surfaceBorder: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val sectionHeaderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val chipBackground: Color,
    val headerGradient: List<Color>,
    val customButtonBg: List<Color>
)

// Dark Palette (Frosted Glass Surfaces Over Full Moon Night Sky)
val DarkAppPalette = AppPalette(
    isDark = true,
    background = Color(0xFF0B0F19),
    backgroundGradient = listOf(
        Color(0xFF1E293B),
        Color(0xFF111827),
        Color(0xFF0B0F19)
    ),
    surface = Color(0xFF1E293B).copy(alpha = 0.85f),
    surfaceBorder = Color(0xFF475569).copy(alpha = 0.55f),
    cardBackground = Color(0xFF1E293B).copy(alpha = 0.88f),
    cardBackgroundElevated = Color(0xFF263346).copy(alpha = 0.92f),
    sectionHeaderColor = Color(0xFFFFFFFF), // Pure white for high contrast
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    chipBackground = Color(0xFF334155).copy(alpha = 0.85f),
    headerGradient = listOf(
        Color(0xFF1E293B).copy(alpha = 0.88f),
        Color(0xFF0F172A).copy(alpha = 0.92f)
    ),
    customButtonBg = listOf(
        Color(0xFF1E1B4B).copy(alpha = 0.92f),
        Color(0xFF0F172A).copy(alpha = 0.88f),
        Color(0xFF1E293B).copy(alpha = 0.92f)
    )
)

// Light Palette (Early Morning Sunrise Sky Glow & Crisp High-Contrast Cards)
val LightAppPalette = AppPalette(
    isDark = false,
    background = Color(0xFFF0F9FF),
    backgroundGradient = listOf(
        Color(0xFFBAE6FD), // Sky blue top
        Color(0xFFE0F2FE), // Soft light blue middle
        Color(0xFFF0F9FF)  // Clean light blue-white bottom
    ),
    surface = Color(0xFFFFFFFF).copy(alpha = 0.92f),
    surfaceBorder = Color(0xFFCBD5E1),
    cardBackground = Color(0xFFFFFFFF).copy(alpha = 0.95f),
    cardBackgroundElevated = Color(0xFFF8FAFC),
    sectionHeaderColor = Color(0xFF0F172A), // Bold deep slate for 100% visibility
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    chipBackground = Color(0xFFE2E8F0),
    headerGradient = listOf(
        Color(0xFFFEF3C7).copy(alpha = 0.85f), // Soft golden morning sun glow
        Color(0xFFE0F2FE).copy(alpha = 0.90f)  // Dawn sky blue
    ),
    customButtonBg = listOf(
        Color(0xFFE0E7FF),
        Color(0xFFF8FAFC),
        Color(0xFFE0E7FF)
    )
)

val LocalAppPalette = staticCompositionLocalOf { DarkAppPalette }

object AppTheme {
    val colors: AppPalette
        @Composable
        get() = LocalAppPalette.current
}
