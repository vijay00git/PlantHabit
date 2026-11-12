package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitWithStatus
import com.example.ui.components.AppIcon
import com.example.ui.components.CelebrationBurst
import com.example.ui.components.IconBubble
import com.example.ui.components.ProgressRing
import com.example.ui.components.bounceClick
import com.example.ui.components.iconAccent
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.backgroundGlow
import kotlinx.coroutines.delay

/**
 * Countdown for minute-based habits. Counts the minutes still needed today; logs what was
 * done when stopped, and completes the habit (with a celebration) when the time is up.
 * Time is measured from timestamps, so it stays right even if the screen sleeps.
 */
@Composable
fun HabitTimerDialog(
    habit: HabitWithStatus,
    reduceMotion: Boolean,
    onLogMinutes: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val icon = AppIcon.of(habit.habit.iconName)
    val accent = c.iconAccent(icon)
    val remainingMinutes = (habit.habit.targetValue - habit.currentProgress).coerceAtLeast(1)
    val totalMs = remainingMinutes * 60_000L

    var running by remember { mutableStateOf(true) }
    var elapsedBefore by remember { mutableLongStateOf(0L) }     // ms banked from earlier runs
    var startedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var finished by remember { mutableStateOf(false) }
    var burst by remember { mutableIntStateOf(0) }

    val elapsed = (elapsedBefore + if (running) now - startedAt else 0L).coerceAtMost(totalMs)
    val left = totalMs - elapsed

    // Keep the screen on while the timer is open
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(running, finished) {
        while (running && !finished) {
            now = System.currentTimeMillis()
            if (elapsedBefore + (now - startedAt) >= totalMs) {
                finished = true
                running = false
                haptics.success()
                burst++
                onLogMinutes(remainingMinutes)
                delay(1600)
                onDismiss()
            }
            delay(250)
        }
    }

    fun stopAndLog() {
        val minutes = (elapsed / 60_000L).toInt()
        if (minutes > 0) onLogMinutes(minutes)
        onDismiss()
    }

    val breathe = rememberInfiniteTransition(label = "timerBreathe")
    val scale by breathe.animateFloat(
        1f, if (reduceMotion || !running) 1f else 1.04f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "timerScale"
    )

    Dialog(
        onDismissRequest = { stopAndLog() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
                .background(c.backgroundGlow)
                .testTag("habit_timer"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                IconBubble(background = accent.soft, size = 56.dp) {
                    Icon(icon.vector, contentDescription = null, tint = accent.color, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(habit.habit.title, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary, textAlign = TextAlign.Center)
                Text(
                    if (habit.currentProgress > 0) "${habit.currentProgress} of ${habit.habit.targetValue} min done today"
                    else "${habit.habit.targetValue} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
                Spacer(Modifier.height(32.dp))
                ProgressRing(
                    progress = elapsed.toFloat() / totalMs,
                    size = 240.dp,
                    strokeWidth = 14.dp,
                    colors = listOf(accent.color, c.primary),
                    modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                ) {
                    AnimatedContent(
                        targetState = finished,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                        label = "timerCenter"
                    ) { done ->
                        if (done) {
                            Text("🎉", style = MaterialTheme.typography.displayMedium)
                        } else {
                            val secs = (left / 1000L).toInt()
                            Text(
                                "%d:%02d".format(secs / 60, secs % 60),
                                style = MaterialTheme.typography.displayMedium,
                                color = c.textPrimary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(36.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundButton(Icons.Default.Close, "Stop and log", c.cardMuted, c.textPrimary, 56) { haptics.tick(); stopAndLog() }
                    RoundButton(
                        if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (running) "Pause" else "Resume",
                        accent.color, c.onAccent, 76,
                        Modifier.testTag("timer_play_pause")
                    ) {
                        if (finished) return@RoundButton
                        haptics.tick()
                        if (running) {
                            elapsedBefore += System.currentTimeMillis() - startedAt
                            running = false
                        } else {
                            startedAt = System.currentTimeMillis()
                            now = startedAt
                            running = true
                        }
                    }
                    RoundButton(Icons.Default.Check, "Finish now", c.primarySoft, c.primary, 56) {
                        // Count the whole session as done
                        if (finished) return@RoundButton
                        finished = true
                        haptics.success()
                        onLogMinutes(remainingMinutes)
                        onDismiss()
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "✕ stops and logs the minutes so far · ✓ marks it done",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted
                )
            }
            if (!reduceMotion) CelebrationBurst(trigger = burst, modifier = Modifier.fillMaxSize(), particleCount = 40)
        }
    }
}

@Composable
private fun RoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    bg: androidx.compose.ui.graphics.Color,
    tint: androidx.compose.ui.graphics.Color,
    sizeDp: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(AppShapes.pill)
            .background(bg)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size((sizeDp / 2.3).dp))
    }
}
