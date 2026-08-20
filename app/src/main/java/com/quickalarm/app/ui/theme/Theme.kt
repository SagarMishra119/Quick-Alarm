package com.quickalarm.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryCyan,
    tertiary = AccentAmber,
    background = DarkAppPalette.background,
    surface = DarkAppPalette.surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkAppPalette.textPrimary,
    onSurface = DarkAppPalette.textPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryCyan,
    tertiary = AccentAmber,
    background = LightAppPalette.background,
    surface = LightAppPalette.surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightAppPalette.textPrimary,
    onSurface = LightAppPalette.textPrimary
)

@Composable
fun QuickAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appPalette = if (darkTheme) DarkAppPalette else LightAppPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appPalette.background.toArgb()
            window.navigationBarColor = appPalette.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppPalette provides appPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
