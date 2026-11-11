package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.HabitWithStatus
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme

@Composable
fun RoutineCard(
    title: String,
    dayProgressText: String,
    routineHabits: List<HabitWithStatus>,
    onToggleHabit: (HabitWithStatus) -> Unit,
    modifier: Modifier = Modifier,
    /** The challenge this routine belongs to, for its banner; null = default morning photo. */
    challenge: com.example.data.model.ChallengeEntity? = null,
) {
    val c = PlantTheme.colors
    val completedCount = routineHabits.count { it.isCompleted }
    val totalCount = routineHabits.size.coerceAtLeast(1)
    val progress = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.large)
            .testTag("routine_card_happy_mornings")
    ) {
        // Photo header with gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
        ) {
            val banner = challenge?.let { rememberBannerSource(it) }
            if (banner != null) {
                ChallengeBannerImage(source = banner, contentDescription = "$title banner", modifier = Modifier.fillMaxSize())
            } else {
                Image(
                    painter = painterResource(id = R.drawable.routine_morning),
                    contentDescription = "Routine banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.05f),
                            0.45f to Color.Black.copy(alpha = 0.2f),
                            1f to Color.Black.copy(alpha = 0.72f)
                        )
                    )
            )

            // Medal with progress ring
            ProgressRing(
                progress = progress,
                size = 44.dp,
                strokeWidth = 4.dp,
                colors = listOf(c.sun, c.sunrise),
                trackColor = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = "Challenge badge",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PhotoChip(text = dayProgressText)
                    PhotoChip(text = "$completedCount/$totalCount tasks done")
                }
            }
        }

        // Routine tasks
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            routineHabits.forEach { habitWithStatus ->
                RoutineTaskRow(
                    habitWithStatus = habitWithStatus,
                    onToggle = { onToggleHabit(habitWithStatus) }
                )
            }

        }
    }
}

@Composable
private fun PhotoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
private fun RoutineTaskRow(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val habit = habitWithStatus.habit
    val isDone = habitWithStatus.isCompleted

    val checkFill by animateColorAsState(
        targetValue = if (isDone) c.sun else Color.Transparent,
        animationSpec = tween(250),
        label = "routine_check_fill"
    )
    val checkBorder by animateColorAsState(
        targetValue = if (isDone) c.sun else c.textMuted,
        animationSpec = tween(250),
        label = "routine_check_border"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "routine_check_scale"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isDone) c.textMuted else c.textPrimary,
        animationSpec = tween(300),
        label = "routine_title"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(pressedScale = 0.98f, haptic = false) {
                if (isDone) haptics.tick() else haptics.success()
                onToggle()
            }
            .clip(AppShapes.small)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(checkFill)
                .border(1.5.dp, checkBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (isDone) "Done" else null,
                tint = c.onAccent,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = checkScale
                        scaleY = checkScale
                        alpha = checkScale.coerceIn(0f, 1f)
                    }
            )
        }

        // Habit icon
        val iconColors = c.iconAccent(AppIcon.of(habit.iconName))
        IconBubble(background = if (isDone) c.sunSoft else iconColors.soft, size = 30.dp) {
            Icon(
                imageVector = AppIcon.of(habit.iconName).vector,
                contentDescription = null,
                tint = if (isDone) c.sun else iconColors.color,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
