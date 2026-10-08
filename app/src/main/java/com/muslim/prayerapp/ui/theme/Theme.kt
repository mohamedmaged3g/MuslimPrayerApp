package com.muslim.prayerapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MuslimLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = LightSurface,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldDark,
    secondary = GoldAccent,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = TextPrimaryDark,
    surface = LightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = LightCardBorder
)

private val MuslimDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = DarkBackground,
    primaryContainer = EmeraldMedium,
    onPrimaryContainer = TextPrimaryLight,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimaryLight,
    surface = DarkSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = DarkCardBorder
)

@Composable
fun MuslimPrayerAppTheme(
    isLightTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isLightTheme) MuslimLightColorScheme else MuslimDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
