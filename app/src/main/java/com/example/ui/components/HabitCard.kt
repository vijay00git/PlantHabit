package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitWithStatus
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors

private fun habitIcon(iconName: String): ImageVector = when (iconName) {
    "water" -> Icons.Default.WaterDrop
    "walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
    "sun" -> Icons.Default.WbSunny
    "moon" -> Icons.Default.NightsStay
    "book" -> Icons.AutoMirrored.Filled.MenuBook
    "sparkle" -> Icons.Default.AutoAwesome
    "trophy" -> Icons.Default.EmojiEvents
    else -> Icons.Default.SelfImprovement
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggleCompleted: () -> Unit,
    onIncrement: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()

    val habit = habitWithStatus.habit
    val accent = c.categoryColors(habit.category)
    val currentProgress = habitWithStatus.currentProgress
    val target = habit.targetValue.coerceAtLeast(1)
    val progressRatio = (currentProgress.toFloat() / target).coerceIn(0f, 1f)
    val isDone = habitWithStatus.isCompleted || progressRatio >= 1f

    val isStepOrWater = habit.unit.lowercase() in listOf("ml", "steps", "glasses")
    val incrementStep = when (habit.unit.lowercase()) {
        "ml" -> 250
        "steps" -> 500
        else -> 1
    }

    // Fires the confetti burst; bumped from tap/swipe handlers when an action completes the habit.
    var burst by remember { mutableIntStateOf(0) }

    fun celebrate() {
        burst++
        haptics.success()
    }

    val toggle: () -> Unit = {
        if (!isDone) celebrate() else haptics.tick()
        onToggleCompleted()
    }
    val increment: (Int) -> Unit = { amount ->
        if (!isDone && currentProgress + amount >= target) celebrate() else haptics.tick()
        onIncrement(amount)
    }
    val onCardClick: () -> Unit = {
        if (isStepOrWater && !isDone) increment(incrementStep) else toggle()
    }

    // ---- animated values ----
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "habit_progress"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "check_scale"
    )
    val checkFill by animateColorAsState(
        targetValue = if (isDone) c.primary else Color.Transparent,
        animationSpec = tween(250),
        label = "check_fill"
    )
    val checkBorder by animateColorAsState(
        targetValue = if (isDone) c.primary else c.textMuted,
        animationSpec = tween(250),
        label = "check_border"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isDone) c.textMuted else c.textPrimary,
        animationSpec = tween(300),
        label = "title_color"
    )
    val cardBorder by animateColorAsState(
        targetValue = if (isDone) c.primary.copy(alpha = 0.45f) else c.border,
        animationSpec = tween(300),
        label = "card_border"
    )
    val bubbleColor by animateColorAsState(
        targetValue = if (isDone) c.primarySoft else accent.soft,
        animationSpec = tween(300),
        label = "bubble_color"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isDone) c.primary else accent.color,
        animationSpec = tween(300),
        label = "icon_tint"
    )

    // ---- swipe right to complete ----
    val currentIsDone by rememberUpdatedState(isDone)
    val currentToggle by rememberUpdatedState(toggle)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && !currentIsDone) {
                currentToggle()
            }
            false // always snap back; the card itself stays in the list
        }
    )

    val cardBrush = if (isStepOrWater && !isDone) {
        Brush.horizontalGradient(listOf(c.card, c.card, accent.soft))
    } else {
        Brush.horizontalGradient(listOf(c.card, c.card))
    }

    Box(modifier = modifier.fillMaxWidth()) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = !isDone,
            enableDismissFromEndToStart = false,
            backgroundContent = {
                val armed = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                val swipeBg by animateColorAsState(
                    targetValue = if (armed) c.primary else c.primarySoft,
                    animationSpec = tween(200),
                    label = "swipe_bg"
                )
                val swipeIcon by animateColorAsState(
                    targetValue = if (armed) c.onPrimary else c.primary,
                    animationSpec = tween(200),
                    label = "swipe_icon"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (armed) 1.2f else 0.9f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "swipe_icon_scale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(AppShapes.large)
                        .background(swipeBg)
                        .padding(start = 24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = swipeIcon,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(pressedScale = 0.98f, haptic = false, onClick = onCardClick)
                    .clip(AppShapes.large)
                    .background(cardBrush)
                    .border(1.dp, cardBorder, AppShapes.large)
                    .padding(14.dp)
                    .testTag("habit_card_${habit.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Category-coloured icon bubble
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(AppShapes.medium)
                            .background(bubbleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = habitIcon(habit.iconName),
                            contentDescription = habit.title,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Info column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = titleColor,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val progressText = if (isStepOrWater) {
                            "%,d / %,d %s".format(currentProgress, target, habit.unit)
                        } else {
                            if (isDone) "Completed · nice work!" else "Tap or swipe → to complete"
                        }
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) c.primary else c.textSecondary
                        )

                        if (isStepOrWater) {
                            // Animated progress bar
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(AppShapes.pill)
                                    .background(if (isDone) c.primarySoft else accent.soft)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(AppShapes.pill)
                                        .background(if (isDone) c.primary else accent.color)
                                )
                            }
                        }
                    }

                    if (isStepOrWater && !isDone) {
                        // Quick add pill
                        Box(
                            modifier = Modifier
                                .bounceClick(pressedScale = 0.9f, haptic = false) { increment(incrementStep) }
                                .clip(AppShapes.pill)
                                .background(accent.soft)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+$incrementStep",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent.color
                            )
                        }
                    } else {
                        // Check toggle with springy pop
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(checkFill)
                                .border(2.dp, checkBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = if (isDone) "Completed" else null,
                                tint = c.onPrimary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        scaleX = checkScale
                                        scaleY = checkScale
                                        alpha = checkScale.coerceIn(0f, 1f)
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Confetti bursts from the check area
        CelebrationBurst(
            trigger = burst,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(64.dp)
        )
    }
}
