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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.MoodLogEntity
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme

data class MoodOption(
    val score: Int,
    val emoji: String,
    val label: String
)

@Composable
fun MoodTrackerCard(
    currentMood: MoodLogEntity?,
    onSelectMood: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    /** % of Good/Great check-ins this week; null hides the badge (no check-ins yet). */
    goodPercent: Int? = null,
) {
    val c = PlantTheme.colors
    val moods = listOf(
        MoodOption(5, "😄", "Great"),
        MoodOption(4, "🙂", "Good"),
        MoodOption(3, "😐", "Okay"),
        MoodOption(2, "🙁", "Meh"),
        MoodOption(1, "😣", "Bad")
    )

    PlantCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_mood_tracker"),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Mood Check-in",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary
                    )
                    Text(
                        text = if (currentMood != null) "Logged: ${currentMood.moodLabel} today" else "How are you feeling today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary
                    )
                }

                // Balance badge (real data only)
                if (goodPercent != null) Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.primarySoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$goodPercent% Good or great",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.primary
                    )
                }
            }

            // Emoji Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                moods.forEach { option ->
                    val accent = when (option.score) {
                        5 -> AccentPair(c.primary, c.primarySoft)
                        4 -> AccentPair(c.sky, c.skySoft)
                        3 -> AccentPair(c.sun, c.sunSoft)
                        2 -> AccentPair(c.sunrise, c.sunriseSoft)
                        else -> AccentPair(c.berry, c.berrySoft)
                    }
                    MoodBubble(
                        option = option,
                        accent = accent,
                        isSelected = currentMood?.moodScore == option.score,
                        onClick = { onSelectMood(option.score, option.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodBubble(
    option: MoodOption,
    accent: AccentPair,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "mood_scale"
    )
    val bubbleColor by animateColorAsState(
        targetValue = if (isSelected) accent.soft else c.cardMuted,
        animationSpec = tween(300),
        label = "mood_bg"
    )
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) accent.color else Color.Transparent,
        animationSpec = tween(300),
        label = "mood_ring"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) accent.color else c.textMuted,
        animationSpec = tween(300),
        label = "mood_label"
    )

    Column(
        modifier = modifier
            .bounceClick(pressedScale = 0.88f, onClick = onClick)
            .testTag("mood_option_${option.label}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .size(50.dp)
                .clip(CircleShape)
                .background(bubbleColor)
                .border(2.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option.emoji,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor
        )
    }
}
