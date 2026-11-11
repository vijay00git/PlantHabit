package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * PlantHabit colour system — "fresh green + sunrise".
 *
 * Never hardcode Color(0x...) in screens. Read colours through [PlantTheme.colors] so that
 * light and dark mode both work, e.g. `PlantTheme.colors.textPrimary`.
 *
 * Every accent has a `*Soft` partner: a tinted background that pairs with the accent as
 * foreground (chips, icon bubbles, badges).
 */
@Immutable
data class AppColors(
    val isDark: Boolean,

    // Surfaces
    val background: Color,
    val surface: Color,        // sheets, dialogs, bottom bar
    val card: Color,           // standard card
    val cardMuted: Color,      // nested/inner panels, input fields, unselected chips
    val border: Color,
    val divider: Color,
    val scrim: Color,

    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    // Brand: leaf green
    val primary: Color,
    val primaryDeep: Color,
    val primarySoft: Color,
    val onPrimary: Color,

    // Sunrise accents
    val sunrise: Color,        // warm orange, secondary brand accent
    val sunriseSoft: Color,
    val sun: Color,            // golden yellow, streaks / fire / stars
    val sunSoft: Color,
    val sky: Color,            // calm blue, water / sleep / info
    val skySoft: Color,
    val berry: Color,          // pink-red, danger / heart / leave
    val berrySoft: Color,
    val lavender: Color,       // purple, mind / AI
    val lavenderSoft: Color,
    val onAccent: Color,       // text/icon on top of a saturated accent fill
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF4FAF2),
    surface = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    cardMuted = Color(0xFFECF5E9),
    border = Color(0xFFDCEADA),
    divider = Color(0xFFE6F0E3),
    scrim = Color(0x99102016),
    textPrimary = Color(0xFF13261A),
    textSecondary = Color(0xFF4D6254),
    textMuted = Color(0xFF889A8D),
    primary = Color(0xFF1FA65A),
    primaryDeep = Color(0xFF13803F),
    primarySoft = Color(0xFFD9F4E1),
    onPrimary = Color(0xFFFFFFFF),
    sunrise = Color(0xFFFF7F3F),
    sunriseSoft = Color(0xFFFFE7D8),
    sun = Color(0xFFF2A80F),
    sunSoft = Color(0xFFFFF2CC),
    sky = Color(0xFF2AA7E8),
    skySoft = Color(0xFFDDF2FD),
    berry = Color(0xFFEF4F72),
    berrySoft = Color(0xFFFFE1E8),
    lavender = Color(0xFF7C6CF0),
    lavenderSoft = Color(0xFFEBE8FE),
    onAccent = Color(0xFFFFFFFF),
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0B1410),
    surface = Color(0xFF111C17),
    card = Color(0xFF16231D),
    cardMuted = Color(0xFF1D2D25),
    border = Color(0xFF263A30),
    divider = Color(0xFF1F3028),
    scrim = Color(0xB3000000),
    textPrimary = Color(0xFFEDF7EF),
    textSecondary = Color(0xFFA3B9AA),
    textMuted = Color(0xFF6C8476),
    primary = Color(0xFF4ADE80),
    primaryDeep = Color(0xFF22C55E),
    primarySoft = Color(0xFF173826),
    onPrimary = Color(0xFF04240F),
    sunrise = Color(0xFFFF9A5C),
    sunriseSoft = Color(0xFF3A2518),
    sun = Color(0xFFFACC32),
    sunSoft = Color(0xFF362E12),
    sky = Color(0xFF5CC6F7),
    skySoft = Color(0xFF102E3C),
    berry = Color(0xFFFB7193),
    berrySoft = Color(0xFF3B1823),
    lavender = Color(0xFFA597FF),
    lavenderSoft = Color(0xFF241F45),
    onAccent = Color(0xFF0B1410),
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

/** Ready-made gradients built from the active palette. */
val AppColors.heroGradient: Brush
    get() = Brush.linearGradient(listOf(primaryDeep, primary, sky))

val AppColors.sunriseGradient: Brush
    get() = Brush.linearGradient(listOf(sun, sunrise, berry))

val AppColors.leafGradient: Brush
    get() = Brush.linearGradient(listOf(primary, primaryDeep))

val AppColors.dreamGradient: Brush
    get() = Brush.linearGradient(listOf(lavender, sky))

/** Soft wash used behind screen headers. */
val AppColors.backgroundGlow: Brush
    get() = Brush.verticalGradient(
        listOf(primarySoft.copy(alpha = if (isDark) 0.55f else 0.9f), background)
    )

/** A (foreground, soft background) colour pair. */
@Immutable
data class AccentPair(val color: Color, val soft: Color)

/** Stable colour per habit category, so every screen colours a category the same way. */
fun AppColors.categoryColors(category: String): AccentPair = when (category.trim().lowercase()) {
    "health", "nutrition", "hydration", "water" -> AccentPair(sky, skySoft)
    "fitness", "exercise", "sport", "movement" -> AccentPair(sunrise, sunriseSoft)
    "mindfulness", "mind", "meditation", "mental" -> AccentPair(lavender, lavenderSoft)
    "productivity", "work", "learning", "study" -> AccentPair(sun, sunSoft)
    "social", "relationships", "love", "self care", "self-care" -> AccentPair(berry, berrySoft)
    else -> AccentPair(primary, primarySoft)
}
