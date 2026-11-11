package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChallengeEntity
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme

@Composable
fun GoalCounterCard(
    goalChallenge: ChallengeEntity,
    onIncrement: (Float) -> Unit,
    onClickDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val current = goalChallenge.goalCurrent
    val target = goalChallenge.goalTarget.coerceAtLeast(1f)
    val ratio = (current / target).coerceIn(0f, 1f)
    val percentage = (ratio * 100).toInt()

    PlantCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_goal_counter"),
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconBubble(background = c.sunriseSoft, size = 40.dp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = c.sunrise,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = goalChallenge.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goalChallenge.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .bounceClick(onClick = onClickDetails)
                        .clip(AppShapes.pill)
                        .background(c.cardMuted)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary
                    )
                }
            }

            // Middle Stats Row: Progress Ring & Target Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProgressRing(
                    progress = ratio,
                    size = 76.dp,
                    strokeWidth = 8.dp,
                    colors = listOf(c.sun, c.sunrise)
                ) {
                    AnimatedCounter(
                        value = percentage,
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        suffix = "%"
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "SO FAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMuted
                    )
                    Text(
                        text = "%.0f %s of %.0f %s".format(
                            current,
                            goalChallenge.goalUnit,
                            target,
                            goalChallenge.goalUnit
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = c.textPrimary
                    )
                    Text(
                        text = "On track • About 8.7 %s a week to finish on time".format(goalChallenge.goalUnit),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.primary
                    )
                }
            }

            // Quick Increment Chips: +1, +5, +10, Custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1f, 5f, 10f).forEach { amount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(pressedScale = 0.9f) { onIncrement(amount) }
                            .clip(AppShapes.small)
                            .background(c.sunriseSoft)
                            .padding(vertical = 10.dp)
                            .testTag("chip_inc_${amount.toInt()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${amount.toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = c.sunrise
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .bounceClick(pressedScale = 0.9f) { onIncrement(2.5f) }
                        .clip(AppShapes.small)
                        .background(c.cardMuted)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+2.5 km",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textSecondary
                    )
                }
            }
        }
    }
}
