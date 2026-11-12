package com.planthabit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.planthabit.R

/** Nunito (variable font, bundled in res/font) — rounded and friendly. */
@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: FontWeight) = Font(
    R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val Nunito = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold),
    nunito(FontWeight.Black),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = Nunito,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

val Typography = Typography(
    displayLarge = style(52, 60, FontWeight.Black, -1.0),
    displayMedium = style(42, 50, FontWeight.Black, -0.8),
    displaySmall = style(34, 42, FontWeight.ExtraBold, -0.5),
    headlineLarge = style(30, 38, FontWeight.ExtraBold, -0.4),
    headlineMedium = style(26, 34, FontWeight.ExtraBold, -0.3),
    headlineSmall = style(22, 30, FontWeight.ExtraBold, -0.2),
    titleLarge = style(20, 28, FontWeight.Bold),
    titleMedium = style(17, 24, FontWeight.Bold),
    titleSmall = style(15, 22, FontWeight.Bold),
    bodyLarge = style(16, 24, FontWeight.Medium),
    bodyMedium = style(14, 21, FontWeight.Medium),
    bodySmall = style(12, 17, FontWeight.Medium),
    labelLarge = style(14, 20, FontWeight.Bold, 0.1),
    labelMedium = style(12, 16, FontWeight.Bold, 0.3),
    labelSmall = style(11, 14, FontWeight.Bold, 0.5),
)
