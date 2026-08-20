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
    val surface: Color,
    val surfaceBorder: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val chipBackground: Color,
    val headerGradient: List<Color>,
    val customButtonBg: List<Color>
)

// Dark Palette (Soft Modern Dark Slate - less harsh than pitch black)
val DarkAppPalette = AppPalette(
    isDark = true,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    surfaceBorder = Color(0xFF374151),
    cardBackground = Color(0xFF1F2937),
    cardBackgroundElevated = Color(0xFF1E293B),
    textPrimary = Color(0xFFF9FAFB),
    textSecondary = Color(0xFF9CA3AF),
    textMuted = Color(0xFF6B7280),
    chipBackground = Color(0xFF374151),
    headerGradient = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
    customButtonBg = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF1E293B))
)

// Light Palette (Clean Modern Crisp Light)
val LightAppPalette = AppPalette(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceBorder = Color(0xFFE2E8F0),
    cardBackground = Color(0xFFFFFFFF),
    cardBackgroundElevated = Color(0xFFF1F5F9),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    chipBackground = Color(0xFFE2E8F0),
    headerGradient = listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF)),
    customButtonBg = listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFEEF2FF))
)

val LocalAppPalette = staticCompositionLocalOf { DarkAppPalette }

object AppTheme {
    val colors: AppPalette
        @Composable
        get() = LocalAppPalette.current
}
