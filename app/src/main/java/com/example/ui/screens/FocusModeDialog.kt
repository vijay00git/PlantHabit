package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitWithStatus
import com.example.ui.components.CelebrationBurst
import com.example.ui.components.IconBubble
import com.example.ui.components.ProgressRing
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors
import com.example.ui.theme.dreamGradient
import com.example.ui.theme.leafGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FocusModeDialog(
    activeHabits: List<HabitWithStatus>,
    onCompleteHabit: (HabitWithStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()

    var selectedHabitIndex by remember { mutableIntStateOf(0) }
    val currentHabit = activeHabits.getOrNull(selectedHabitIndex) ?: activeHabits.firstOrNull()

    val totalDurationSeconds = 25 * 60 // 25 min pomodoro
    var remainingSeconds by remember { mutableIntStateOf(totalDurationSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    // Completion celebration state: keep the finished habit's title on screen while the burst plays.
    var burstTrigger by remember { mutableIntStateOf(0) }
    var completedTitle by remember { mutableStateOf<String?>(null) }
    val isCompleting = completedTitle != null

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds == 0) {
            if (isRunning) haptics.success()
            isRunning = false
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)
    val progressRatio = (totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds

    // Gentle "breathing" while the timer runs: ~4s in, ~4s out.
    val breathing = rememberInfiniteTransition(label = "focusBreath")
    val breathWave by breathing.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "focusBreathWave"
    )
    val breathStrength by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = tween(700),
        label = "focusBreathStrength"
    )

    val displayTitle = completedTitle ?: currentHabit?.habit?.title ?: "Mindful Focus"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background)
                .background(c.dreamGradient, alpha = if (c.isDark) 0.16f else 0.22f)
        ) {
            // Soft glow behind the timer
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(380.dp)
                    .graphicsLayer {
                        val s = 1f + 0.08f * breathWave * breathStrength
                        scaleX = s
                        scaleY = s
                        alpha = 0.6f + 0.4f * breathWave * breathStrength
                    }
                    .background(
                        Brush.radialGradient(
                            listOf(c.lavender.copy(alpha = 0.28f), c.sky.copy(alpha = 0.10f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top close button & Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconBubble(background = c.lavenderSoft, size = 38.dp) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = c.lavender,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Focus Mode",
                            style = MaterialTheme.typography.titleLarge,
                            color = c.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .bounceClick(pressedScale = 0.88f) { onDismiss() }
                            .clip(CircleShape)
                            .background(c.card.copy(alpha = 0.8f))
                            .border(1.dp, c.border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = c.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle: Timer Circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = displayTitle,
                        transitionSpec = {
                            (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }) togetherWith
                                fadeOut(tween(200))
                        },
                        label = "focusTitle"
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = c.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "Single-task focus • No distractions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary
                    )

                    // Habit picker (only when there's a choice)
                    if (activeHabits.size > 1 && !isCompleting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeHabits.forEachIndexed { i, hws ->
                                val accent = c.categoryColors(hws.habit.category)
                                val selected = hws.habit.id == currentHabit?.habit?.id
                                val chipBg by animateColorAsState(
                                    targetValue = if (selected) accent.color else accent.soft,
                                    animationSpec = tween(250),
                                    label = "focusChipBg"
                                )
                                val chipText by animateColorAsState(
                                    targetValue = if (selected) c.onAccent else accent.color,
                                    animationSpec = tween(250),
                                    label = "focusChipText"
                                )
                                Box(
                                    modifier = Modifier
                                        .bounceClick(haptic = !selected) { selectedHabitIndex = i }
                                        .clip(AppShapes.pill)
                                        .background(chipBg)
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = hws.habit.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = chipText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier.graphicsLayer {
                            val s = 1f + 0.035f * breathWave * breathStrength
                            scaleX = s
                            scaleY = s
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        ProgressRing(
                            progress = progressRatio,
                            size = 240.dp,
                            strokeWidth = 14.dp,
                            colors = listOf(c.lavender, c.sky, c.primary),
                            trackColor = c.cardMuted.copy(alpha = 0.7f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AnimatedTimeText(text = timeFormatted)
                                Crossfade(
                                    targetState = isRunning,
                                    animationSpec = tween(400),
                                    label = "focusStatus"
                                ) { running ->
                                    Text(
                                        text = if (running) "Deep in flow" else "Paused",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (running) c.lavender else c.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .bounceClick(pressedScale = 0.88f) {
                                    remainingSeconds = totalDurationSeconds
                                    isRunning = false
                                }
                                .clip(CircleShape)
                                .background(c.card.copy(alpha = 0.85f))
                                .border(1.dp, c.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = c.textPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .bounceClick(pressedScale = 0.9f) { isRunning = !isRunning }
                                .clip(CircleShape)
                                .background(c.dreamGradient)
                                .testTag("btn_focus_play_pause"),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(
                                targetState = isRunning,
                                animationSpec = tween(250),
                                label = "focusPlayPause"
                            ) { running ->
                                Icon(
                                    imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (running) "Pause" else "Start",
                                    tint = c.onAccent,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        // Balances the reset button so play/pause stays centred
                        Spacer(modifier = Modifier.size(52.dp))
                    }

                    if (currentHabit != null || isCompleting) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .bounceClick(pressedScale = 0.97f, haptic = false, enabled = !isCompleting) {
                                    val habit = currentHabit ?: return@bounceClick
                                    completedTitle = habit.habit.title
                                    isRunning = false
                                    haptics.success()
                                    burstTrigger++
                                    onCompleteHabit(habit)
                                    scope.launch {
                                        delay(950L)
                                        onDismiss()
                                    }
                                }
                                .clip(AppShapes.large)
                                .background(c.leafGradient)
                                .testTag("btn_complete_focus"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCompleting) "Nicely done! 🌱" else "Mark Habit Completed 🎉",
                                style = MaterialTheme.typography.titleSmall,
                                color = c.onPrimary
                            )
                        }
                    }
                }
            }

            // Celebration overlay (doesn't intercept touches)
            CelebrationBurst(
                trigger = burstTrigger,
                particleCount = 40,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** "mm:ss" where each digit rolls down independently as it changes. */
@Composable
private fun AnimatedTimeText(text: String) {
    val c = PlantTheme.colors
    val style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum")
    Row(verticalAlignment = Alignment.CenterVertically) {
        text.forEach { ch ->
            if (!ch.isDigit()) {
                Text(text = ch.toString(), style = style, color = c.textPrimary)
            } else {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = {
                        (slideInVertically(tween(320)) { -it / 2 } + fadeIn(tween(320))) togetherWith
                            (slideOutVertically(tween(260)) { it / 2 } + fadeOut(tween(220))) using
                            SizeTransform(clip = false)
                    },
                    label = "focusDigit"
                ) { digit ->
                    Text(text = digit.toString(), style = style, color = c.textPrimary)
                }
            }
        }
    }
}
