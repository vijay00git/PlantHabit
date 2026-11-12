package com.planthabit.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.planthabit.data.repository.Achievement
import com.planthabit.data.repository.Achievements
import com.planthabit.ui.theme.AppShapes
import com.planthabit.ui.theme.PlantTheme
import com.planthabit.ui.theme.sunriseGradient
import kotlinx.coroutines.delay

/** Celebration popup for newly earned badges. */
@Composable
fun AchievementUnlockedDialog(achievements: List<Achievement>, onDismiss: () -> Unit) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val reduceMotion = LocalReduceMotion.current
    var burst by remember { mutableIntStateOf(0) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120)
        shown = true
        haptics.success()
        burst++
    }
    val scale by animateFloatAsState(
        if (shown || reduceMotion) 1f else 0.4f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "badgePop"
    )
    Dialog(onDismissRequest = onDismiss) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.xl)
                    .background(c.surface)
                    .padding(24.dp)
                    .testTag("achievement_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (achievements.size == 1) "Achievement unlocked!" else "${achievements.size} achievements unlocked!",
                    style = MaterialTheme.typography.titleLarge,
                    color = c.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                achievements.take(4).forEach { a ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(c.sunriseGradient),
                            contentAlignment = Alignment.Center
                        ) { Text(a.emoji, style = MaterialTheme.typography.headlineSmall) }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(a.title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                            Text(a.description, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                }
                if (achievements.size > 4) {
                    Text("…and ${achievements.size - 4} more on the Stats page", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
                Spacer(Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.primary)
                        .bounceClick(onClick = onDismiss)
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                        .testTag("btn_achievement_ok")
                ) { Text("Awesome!", style = MaterialTheme.typography.titleSmall, color = c.onPrimary) }
            }
            if (!reduceMotion) CelebrationBurst(trigger = burst, modifier = Modifier.matchParentSize(), particleCount = 40)
        }
    }
}

/** Grid of all badges; earned ones in colour, others locked. */
@Composable
fun AchievementsGrid(unlocked: Map<String, Long>, modifier: Modifier = Modifier) {
    val c = PlantTheme.colors
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 5 else 3
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(background = c.sunSoft, size = 40.dp) { Text("🏆", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Achievements", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                Text("${unlocked.size} of ${Achievements.all.size} earned", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        Achievements.all.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { a ->
                    val earned = a.id in unlocked
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(AppShapes.medium)
                            .background(if (earned) c.sunSoft else c.cardMuted)
                            .border(1.dp, if (earned) c.sun.copy(alpha = 0.4f) else c.border, AppShapes.medium)
                            .padding(10.dp)
                            .testTag("badge_${a.id}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (earned) a.emoji else "🔒",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.graphicsLayer { alpha = if (earned) 1f else 0.5f }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            a.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (earned) c.textPrimary else c.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            a.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
