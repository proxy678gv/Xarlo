package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = TextPrimaryDark,
    primaryContainer = CosmicPanelElevated,
    onPrimaryContainer = AccentVioletLight,
    secondary = AccentCyan,
    onSecondary = CosmicBackground,
    secondaryContainer = CosmicBorder,
    onSecondaryContainer = AccentCyanLight,
    tertiary = AccentBlue,
    onTertiary = TextPrimaryDark,
    background = CosmicBackground,
    onBackground = TextPrimaryDark,
    surface = CosmicPanel,
    onSurface = TextPrimaryDark,
    surfaceVariant = CosmicPanelElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = CosmicBorder,
    outlineVariant = CosmicBorderSubtle,
    error = StatusDanger,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = AccentViolet,
    onPrimary = TextPrimaryDark,
    primaryContainer = LightPanelElevated,
    onPrimaryContainer = AccentViolet,
    secondary = AccentCyanDark,
    onSecondary = LightBackground,
    secondaryContainer = LightBorder,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentBlue,
    onTertiary = TextPrimaryDark,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightPanel,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightPanelElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = StatusDanger,
    onError = TextPrimaryDark
)

@Composable
fun XarloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    XarloTheme(darkTheme = darkTheme, content = content)
}
