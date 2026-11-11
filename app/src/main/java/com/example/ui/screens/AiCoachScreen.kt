package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.components.CelebrationBurst
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors
import com.example.ui.theme.dreamGradient
import kotlinx.coroutines.delay

data class AiRecommendation(
    val id: String,
    val title: String,
    val rationale: String,
    val habitName: String,
    val timeOfDay: String,
    val scheduledTime: String,
    val category: String
)

private fun aiCategoryIcon(category: String): ImageVector = when (category.trim().lowercase()) {
    "mindfulness" -> Icons.Default.SelfImprovement
    "fitness" -> Icons.Default.FitnessCenter
    "health" -> Icons.Default.Favorite
    "productivity" -> Icons.Default.ElectricBolt
    else -> Icons.Default.Spa
}

@Composable
fun AiCoachScreen(
    onAdoptRecommendation: (AiRecommendation) -> Unit,
    onOpenAiPlanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    var adoptedIds by remember { mutableStateOf(setOf<String>()) }

    val recommendations = remember {
        listOf(
            AiRecommendation(
                id = "habit_stack_1",
                title = "Smart Habit Stacking",
                rationale = "Link a new micro-habit directly after your existing 'Drink a glass of water' routine to boost retention by 2.4x.",
                habitName = "5-Minute Breathwork",
                timeOfDay = "MORNING",
                scheduledTime = "07:20",
                category = "Mindfulness"
            ),
            AiRecommendation(
                id = "circadian_walk",
                title = "Circadian Alignment",
                rationale = "Your calendar shows a 30m break before lunch. An outdoor sunlight walk at 11:30 am regulates evening melatonin.",
                habitName = "Midday Sun Walk",
                timeOfDay = "MORNING",
                scheduledTime = "11:30",
                category = "Fitness"
            ),
            AiRecommendation(
                id = "evening_screen_sunset",
                title = "Sleep Debt Recovery",
                rationale = "Based on your Wind Down schedule at 23:00, dimming screens 45 minutes prior will accelerate deep REM cycle onset.",
                habitName = "Digital Sunset & Reading",
                timeOfDay = "EVENING",
                scheduledTime = "22:15",
                category = "Health"
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero header
        item(key = "hero") {
            AiCoachHero(modifier = Modifier.staggeredEntrance(0))
        }

        // Natural Language Routine Planner Card
        item(key = "planner") {
            PlantCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1)
                    .testTag("card_ai_natural_language_planner"),
                onClick = onOpenAiPlanner,
                containerColor = c.card,
                borderColor = c.lavender.copy(alpha = 0.35f),
                contentPadding = 20.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.pill)
                                .background(c.lavenderSoft)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "JUST SAY IT. AI PLANS IT",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.lavender
                            )
                        }
                        IconBubble(background = c.sunSoft, size = 36.dp) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = c.sun,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Type or speak your plans",
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.textPrimary
                    )

                    Text(
                        text = "e.g., \"Wake up at 6am, run 5km, read 20 pages, wind down by 10pm\". The AI turns your words into structured habits & reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .bounceClick(pressedScale = 0.97f, onClick = onOpenAiPlanner)
                            .clip(AppShapes.medium)
                            .background(c.dreamGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = c.onAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Launch Natural Language Planner",
                                style = MaterialTheme.typography.labelLarge,
                                color = c.onAccent
                            )
                        }
                    }
                }
            }
        }

        // Today's insight
        item(key = "insight") {
            AiInsightCard(modifier = Modifier.staggeredEntrance(2))
        }

        // Section Title
        item(key = "section") {
            SectionHeader(
                title = "Recommended for you",
                modifier = Modifier.staggeredEntrance(3)
            )
        }

        // Recommendations List
        itemsIndexed(recommendations, key = { _, rec -> rec.id }) { index, rec ->
            RecommendationCard(
                rec = rec,
                isAdopted = adoptedIds.contains(rec.id),
                onAdopt = {
                    if (!adoptedIds.contains(rec.id)) {
                        haptics.success()
                        adoptedIds = adoptedIds + rec.id
                        onAdoptRecommendation(rec)
                    }
                },
                modifier = Modifier.staggeredEntrance(index + 4)
            )
        }
    }
}

@Composable
private fun AiCoachHero(modifier: Modifier = Modifier) {
    val c = PlantTheme.colors
    val pulse = rememberInfiniteTransition(label = "heroPulse")
    val sparkleScale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sparkleScale"
    )
    val haloAlpha by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "haloAlpha"
    )
    val sparkleRotation by pulse.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sparkleRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.xl)
            .background(c.dreamGradient)
            .padding(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer { scaleX = sparkleScale * 1.08f; scaleY = sparkleScale * 1.08f }
                        .clip(CircleShape)
                        .background(c.onAccent.copy(alpha = haloAlpha))
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = c.onAccent,
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer {
                            scaleX = sparkleScale
                            scaleY = sparkleScale
                            rotationZ = sparkleRotation
                        }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PlantHabit AI Coach",
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.onAccent
                )
                Text(
                    text = "Intelligent routine optimization and natural language planning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onAccent.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun AiInsightCard(modifier: Modifier = Modifier) {
    val c = PlantTheme.colors
    var thinking by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (thinking) {
            delay(900)
            thinking = false
        }
    }

    PlantCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(350)),
        containerColor = c.lavenderSoft,
        borderColor = c.lavender.copy(alpha = 0.3f),
        contentPadding = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconBubble(background = c.lavender, size = 34.dp) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = c.onAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "TODAY'S AI INSIGHT",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.lavender
                )
            }

            AnimatedContent(
                targetState = thinking,
                transitionSpec = {
                    (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }) togetherWith fadeOut(tween(200))
                },
                label = "insightThinking"
            ) { isThinking ->
                if (isThinking) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        AiThinkingDots(color = c.lavender)
                        Text(
                            text = "Coach is reviewing your week…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary
                        )
                    }
                } else {
                    Text(
                        text = "Your morning routine is 85% consistent! To maintain your streak through the weekend, stack an evening hydration anchor.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    rec: AiRecommendation,
    isAdopted: Boolean,
    onAdopt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(rec.category)

    PlantCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_rec_${rec.id}"),
        containerColor = c.card,
        contentPadding = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBubble(background = accent.soft, size = 44.dp) {
                    Icon(
                        imageVector = aiCategoryIcon(rec.category),
                        contentDescription = null,
                        tint = accent.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary
                    )
                    Text(
                        text = rec.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent.color
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.sunSoft)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = c.sun,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = rec.scheduledTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = c.sun
                    )
                }
            }

            Text(
                text = rec.rationale,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clip(AppShapes.small)
                        .background(accent.soft)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = accent.color, modifier = Modifier.size(14.dp))
                    Text(
                        text = rec.habitName,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent.color,
                        maxLines = 1
                    )
                }

                AdoptButton(isAdopted = isAdopted, onAdopt = onAdopt)
            }
        }
    }
}

private enum class AdoptState { Idle, JustAdded, Scheduled }

/**
 * "Adopt" pill: on tap it pops into a bright "Added ✓" state with a confetti burst,
 * then settles into a calmer "Scheduled" state after a moment.
 */
@Composable
private fun AdoptButton(isAdopted: Boolean, onAdopt: () -> Unit) {
    val c = PlantTheme.colors
    var justAdded by remember { mutableStateOf(false) }
    var burst by remember { mutableIntStateOf(0) }

    LaunchedEffect(justAdded) {
        if (justAdded) {
            delay(1600)
            justAdded = false
        }
    }

    val state = when {
        !isAdopted -> AdoptState.Idle
        justAdded -> AdoptState.JustAdded
        else -> AdoptState.Scheduled
    }

    val bg by animateColorAsState(
        targetValue = when (state) {
            AdoptState.Idle -> c.primary
            AdoptState.JustAdded -> c.primaryDeep
            AdoptState.Scheduled -> c.primarySoft
        },
        animationSpec = tween(320),
        label = "adoptBg"
    )
    val fg = when (state) {
        AdoptState.Scheduled -> c.primary
        else -> c.onPrimary
    }

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .bounceClick(enabled = !isAdopted, haptic = false) {
                    justAdded = true
                    burst++
                    onAdopt()
                }
                .clip(AppShapes.pill)
                .background(bg)
                .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (scaleIn(initialScale = 0.7f, animationSpec = tween(260)) + fadeIn(tween(260))) togetherWith
                        (scaleOut(targetScale = 0.7f, animationSpec = tween(180)) + fadeOut(tween(180)))
                },
                label = "adoptContent"
            ) { s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (icon, label) = when (s) {
                        AdoptState.Idle -> Icons.Default.Add to "Adopt"
                        AdoptState.JustAdded -> Icons.Default.Check to "Added ✓"
                        AdoptState.Scheduled -> Icons.Default.EventAvailable to "Scheduled"
                    }
                    if (s != AdoptState.JustAdded) {
                        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                    }
                    Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
                }
            }
        }
        CelebrationBurst(
            trigger = burst,
            modifier = Modifier.matchParentSize(),
            particleCount = 18
        )
    }
}

/**
 * Three softly bouncing dots, used wherever the AI is "thinking".
 * Shared with [AiNaturalLanguagePlannerDialog].
 */
@Composable
internal fun AiThinkingDots(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "thinkingDots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize * 0.6f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val phase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, delayMillis = 0, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = androidx.compose.animation.core.StartOffset(i * 160)
                ),
                label = "dot$i"
            )
            // Triangle wave 0 -> 1 -> 0 over the first half, rest in the second half
            val wave = if (phase < 0.5f) 1f - kotlin.math.abs(phase * 4f - 1f) else 0f
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        translationY = -wave * dotSize.toPx() * 0.6f
                        alpha = 0.45f + 0.55f * wave
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
