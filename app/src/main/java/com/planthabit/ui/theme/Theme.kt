package com.planthabit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

private fun AppColors.toMaterialScheme() = if (isDark) {
    darkColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primarySoft, onPrimaryContainer = primary,
        secondary = sunrise, onSecondary = onAccent,
        secondaryContainer = sunriseSoft, onSecondaryContainer = sunrise,
        tertiary = sky, onTertiary = onAccent,
        tertiaryContainer = skySoft, onTertiaryContainer = sky,
        error = berry, onError = onAccent,
        errorContainer = berrySoft, onErrorContainer = berry,
        background = background, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = cardMuted, onSurfaceVariant = textSecondary,
        surfaceContainer = card, surfaceContainerHigh = cardMuted,
        outline = border, outlineVariant = divider,
        scrim = scrim,
        inverseSurface = textPrimary, inverseOnSurface = background,
    )
} else {
    lightColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primarySoft, onPrimaryContainer = primaryDeep,
        secondary = sunrise, onSecondary = onAccent,
        secondaryContainer = sunriseSoft, onSecondaryContainer = sunrise,
        tertiary = sky, onTertiary = onAccent,
        tertiaryContainer = skySoft, onTertiaryContainer = sky,
        error = berry, onError = onAccent,
        errorContainer = berrySoft, onErrorContainer = berry,
        background = background, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = cardMuted, onSurfaceVariant = textSecondary,
        surfaceContainer = card, surfaceContainerHigh = cardMuted,
        outline = border, outlineVariant = divider,
        scrim = scrim,
        inverseSurface = textPrimary, inverseOnSurface = background,
    )
}

/** Rounded, friendly corner scale. Prefer these over ad-hoc RoundedCornerShape values. */
object AppShapes {
    val small = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(18.dp)
    val large = RoundedCornerShape(24.dp)
    val xl = RoundedCornerShape(32.dp)
    val pill = RoundedCornerShape(percent = 50)
}

/** Accessor for PlantHabit design tokens: `PlantTheme.colors.primary`. */
object PlantTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

/**
 * @param themeMode "LIGHT", "DARK" or "SYSTEM" (follow the phone setting).
 */
@Composable
fun PlantHabitTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = Typography,
            shapes = Shapes(
                extraSmall = AppShapes.small,
                small = AppShapes.small,
                medium = AppShapes.medium,
                large = AppShapes.large,
                extraLarge = AppShapes.xl,
            ),
            content = content
        )
    }
}
