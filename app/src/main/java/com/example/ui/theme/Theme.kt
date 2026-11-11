package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SproutPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SproutPrimaryContainer,
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = SproutAccentGreenGlow,
    onSecondary = Color(0xFF052E16),
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFFBBF7D0),
    tertiary = SproutAccentAmber,
    onTertiary = Color(0xFF451A03),
    background = SproutBackgroundDark,
    onBackground = SproutTextPrimaryDark,
    surface = SproutSurfaceDark,
    onSurface = SproutTextPrimaryDark,
    surfaceVariant = SproutSurfaceVariantDark,
    onSurfaceVariant = SproutTextSecondaryDark,
    outline = SproutCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = SproutPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = SproutAccentGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF14532D),
    tertiary = SproutAccentAmber,
    onTertiary = Color.White,
    background = SproutBackgroundLight,
    onBackground = SproutTextPrimaryLight,
    surface = SproutSurfaceLight,
    onSurface = SproutTextPrimaryLight,
    surfaceVariant = SproutCardLight,
    onSurfaceVariant = SproutTextSecondaryLight,
    outline = SproutCardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark theme matching Sprout aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
