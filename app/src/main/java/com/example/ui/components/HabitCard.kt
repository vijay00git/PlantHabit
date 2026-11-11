package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitWithStatus
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggleCompleted: () -> Unit,
    onIncrement: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStatus.habit
    val currentProgress = habitWithStatus.currentProgress
    val target = habit.targetValue.coerceAtLeast(1)
    val progressRatio = (currentProgress.toFloat() / target).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressRatio, label = "habit_progress")
    val isDone = habitWithStatus.isCompleted || progressRatio >= 1f

    val isStepOrWater = habit.unit.lowercase() in listOf("ml", "steps", "glasses")
    val incrementStep = when (habit.unit.lowercase()) {
        "ml" -> 250
        "steps" -> 500
        else -> 1
    }

    // Gradient styling for water / step habits matching screenshot
    val cardBackgroundBrush = if (habit.unit == "ml") {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF231D1C),
                Color(0xFF3B2A17).copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                SproutCardDark,
                SproutCardDark
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBackgroundBrush)
            .border(
                1.dp,
                if (isDone) SproutAccentGreen.copy(alpha = 0.4f) else SproutCardBorderDark,
                RoundedCornerShape(22.dp)
            )
            .clickable {
                if (isStepOrWater && !isDone) {
                    onIncrement(incrementStep)
                } else {
                    onToggleCompleted()
                }
            }
            .padding(14.dp)
            .testTag("habit_card_${habit.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Badge
            val iconVector = when (habit.iconName) {
                "water" -> Icons.Default.WaterDrop
                "walk" -> Icons.Default.DirectionsWalk
                "sun" -> Icons.Default.WbSunny
                "moon" -> Icons.Default.NightsStay
                else -> Icons.Default.SelfImprovement
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDone) SproutAccentGreen.copy(alpha = 0.2f)
                        else SproutAccentAmber.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = habit.title,
                    tint = if (isDone) SproutAccentGreenGlow else SproutAccentAmber,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = habit.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val progressText = if (isStepOrWater) {
                    "%,d / %,d %s".format(currentProgress, target, habit.unit)
                } else {
                    if (isDone) "Completed" else "Tap to complete"
                }

                Text(
                    text = progressText,
                    fontSize = 12.sp,
                    color = if (isDone) SproutAccentGreenGlow else Color(0xFFD4D4D8),
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Action / Circular Progress Badge on Right
            if (isStepOrWater) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(46.dp),
                        color = Color(0xFF332D42),
                        strokeWidth = 3.5.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(46.dp),
                        color = if (isDone) SproutAccentGreenGlow else SproutAccentAmber,
                        strokeWidth = 3.5.dp
                    )

                    // Text display e.g. "1.1k"
                    val label = when {
                        currentProgress >= 1000 -> "%.1fk".format(currentProgress / 1000f)
                        else -> "$currentProgress"
                    }
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) SproutAccentGreenGlow else SproutAccentAmber
                    )
                }
            } else {
                // Checkbox toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) SproutAccentGreen else Color.Transparent)
                        .border(
                            2.dp,
                            if (isDone) SproutAccentGreen else Color(0xFF64748B),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
