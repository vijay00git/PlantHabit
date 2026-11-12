package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.leafGradient
import com.example.ui.theme.sunriseGradient

@Composable
fun PlantStreakCard(
    completedCount: Int,
    totalCount: Int,
    activeStreakDays: Int,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors

    val progressRatio = if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "plant_progress"
    )
    val remaining = (totalCount - completedCount).coerceAtLeast(0)

    // Plant grows with the streak (caps at 30 days) and sways gently in the breeze.
    val growth = (activeStreakDays.coerceIn(0, 30) / 30f)
    val plantSize by animateDpAsState(
        targetValue = 44.dp + 28.dp * growth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "plant_size"
    )
    val breeze = rememberInfiniteTransition(label = "plant_breeze")
    val sway by breeze.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plant_sway"
    )

    val stage = when {
        activeStreakDays <= 0 -> "Seed"
        activeStreakDays < 3 -> "Sprout"
        activeStreakDays < 7 -> "Seedling"
        activeStreakDays < 21 -> "Sapling"
        else -> "Blooming"
    }

    PlantCard(modifier = modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant pot
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(AppShapes.large)
                    .background(c.primarySoft),
                contentAlignment = Alignment.BottomCenter
            ) {
                PlantStageImage(
                    streakDays = activeStreakDays,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .size(plantSize)
                        .graphicsLayer {
                            rotationZ = sway
                            transformOrigin = TransformOrigin(0.5f, 0.88f)
                        }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Streak badge on a sunrise gradient
                Row(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.sunriseGradient)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", style = MaterialTheme.typography.labelLarge)
                    AnimatedCounter(
                        value = activeStreakDays,
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onAccent
                    )
                    Text(
                        text = "day streak",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onAccent
                    )
                }

                Text(
                    text = if (activeStreakDays > 0) "$stage · your plant is thriving 🌱" else "Plant a streak to grow 🌱",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary
                )
                Text(
                    text = when {
                        totalCount == 0 -> "Add a habit to start growing."
                        remaining > 0 -> "$remaining more to keep the streak alive."
                        else -> "Streak secured for today! 🔥"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )

                // Animated growth bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(AppShapes.pill)
                        .background(c.cardMuted)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(AppShapes.pill)
                            .background(c.leafGradient)
                    )
                }
            }
        }
    }
}
