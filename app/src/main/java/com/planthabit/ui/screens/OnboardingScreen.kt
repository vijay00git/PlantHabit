package com.planthabit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.planthabit.R
import com.planthabit.data.model.ChallengeEntity
import com.planthabit.ui.components.AppIcon
import com.planthabit.ui.components.ChallengeBannerImage
import com.planthabit.ui.components.IconBubble
import com.planthabit.ui.components.bounceClick
import com.planthabit.ui.components.iconAccent
import com.planthabit.ui.components.rememberBannerSource
import com.planthabit.ui.components.rememberHaptics
import com.planthabit.ui.theme.AppShapes
import com.planthabit.ui.theme.PlantTheme
import com.planthabit.ui.theme.backgroundGlow
import com.planthabit.ui.theme.leafGradient

/** A goal offered during onboarding, with the habit and challenge it suggests. */
enum class StarterGoal(
    val label: String,
    val icon: AppIcon,
    val habitTitle: String,
    val category: String,
    val timeOfDay: String,
    val time: String,
    val target: Int,
    val unit: String,
    val challengeId: String,
) {
    SLEEP("Sleep better", AppIcon.BED, "In bed by 22:30", "Sleep", "EVENING", "22:30", 1, "tasks", "better_sleep"),
    MOVE("Move more", AppIcon.WALK, "Walk 8,000 steps", "Fitness", "ANYTIME", "18:00", 8000, "steps", "steps_10k"),
    WATER("Drink more water", AppIcon.WATER, "Drink 2,000 ml water", "Health", "ANYTIME", "10:00", 2000, "ml", "hydration_hero"),
    READ("Read more", AppIcon.BOOK, "Read 10 pages", "Learning", "EVENING", "21:30", 10, "pages", "read_daily"),
    MINDFUL("Be mindful", AppIcon.MEDITATE, "Meditate 10 mins", "Mindfulness", "MORNING", "07:30", 10, "mins", "meditation_21"),
    FIT("Get fit", AppIcon.GYM, "Workout 30 mins", "Fitness", "MORNING", "07:00", 30, "mins", "stretch_daily"),
    LEARN("Learn something", AppIcon.STUDY, "Practice a skill 15 mins", "Learning", "EVENING", "19:00", 15, "mins", "language_30"),
    MONEY("Save money", AppIcon.SAVINGS, "Track today's spending", "Finance", "EVENING", "20:30", 1, "tasks", "savings_jar"),
}

data class OnboardingResult(
    val goals: Set<StarterGoal>,
    val challengeId: String?,
    val keepExamples: Boolean,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    challenges: List<ChallengeEntity>,
    hasExistingHabits: Boolean,
    onRequestNotifications: () -> Unit,
    onFinish: (OnboardingResult) -> Unit,
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var goals by remember { mutableStateOf(setOf<StarterGoal>()) }
    var challengeId by remember { mutableStateOf<String?>(null) }
    var keepExamples by remember { mutableStateOf(hasExistingHabits) }
    val lastStep = 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .background(c.backgroundGlow)
            .safeDrawingPadding()
            .testTag("onboarding")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Progress dots + skip
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(lastStep + 1) { i ->
                    val color by animateColorAsState(if (i <= step) c.primary else c.border, tween(250), label = "dot")
                    Box(
                        Modifier
                            .padding(end = 6.dp)
                            .height(6.dp)
                            .width(if (i == step) 28.dp else 12.dp)
                            .clip(AppShapes.pill)
                            .background(color)
                    )
                }
                Spacer(Modifier.weight(1f))
                if (step < lastStep) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textSecondary,
                        modifier = Modifier
                            .clip(AppShapes.pill)
                            .bounceClick { onFinish(OnboardingResult(emptySet(), null, keepExamples = true)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("onboarding_skip")
                    )
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(350)) { dir * it / 3 } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(300)) { -dir * it / 3 } + fadeOut(tween(200)))
                },
                label = "onboardingStep",
                modifier = Modifier.weight(1f)
            ) { s ->
                BoxWithConstraints(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (s) {
                        0 -> WelcomeStep()
                        1 -> {
                            StepTitle("What would you like to grow?", "Pick a few — we'll plant a habit for each.")
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StarterGoal.entries.forEach { g ->
                                    GoalChip(g, selected = g in goals) {
                                        haptics.tick()
                                        goals = if (g in goals) goals - g else goals + g
                                    }
                                }
                            }
                        }
                        2 -> {
                            StepTitle("Want a challenge to start?", "Optional — a guided plan with daily tasks. You can join more later.")
                            val suggested = (goals.map { it.challengeId } + listOf("happy_mornings", "gratitude_month"))
                                .distinct()
                                .mapNotNull { id -> challenges.firstOrNull { it.id == id && !it.isHidden } }
                                .take(4)
                            suggested.forEach { ch ->
                                StarterChallengeRow(ch, selected = challengeId == ch.id) {
                                    haptics.tick()
                                    challengeId = if (challengeId == ch.id) null else ch.id
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                        else -> {
                            StepTitle("Stay on track", "Gentle reminders at the times you choose — nothing else.")
                            IconBubble(background = c.sunriseSoft, size = 96.dp) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = c.sunrise, modifier = Modifier.size(48.dp))
                            }
                            Spacer(Modifier.height(18.dp))
                            PrimaryButton("Allow reminders", Modifier.testTag("onboarding_allow_notifications")) {
                                onRequestNotifications()
                            }
                            Spacer(Modifier.height(28.dp))
                            OptionRow(
                                selected = !keepExamples,
                                title = if (hasExistingHabits) "Start fresh" else "Start with just my picks",
                                subtitle = if (hasExistingHabits) "Clears your current habits and history" else "An empty garden plus the habits you chose"
                            ) { keepExamples = false }
                            Spacer(Modifier.height(10.dp))
                            OptionRow(
                                selected = keepExamples,
                                title = if (hasExistingHabits) "Keep my current habits" else "Keep the example habits too",
                                subtitle = if (hasExistingHabits) "Add your picks alongside them" else "Handy for exploring the app"
                            ) { keepExamples = true }
                        }
                    }
                }
                }
            }

            // Bottom buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) {
                    Text(
                        "Back",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textSecondary,
                        modifier = Modifier
                            .clip(AppShapes.pill)
                            .bounceClick { step-- }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    text = when (step) {
                        0 -> "Let's begin"
                        lastStep -> "Start growing 🌱"
                        else -> "Continue"
                    },
                    modifier = Modifier.testTag("onboarding_next")
                ) {
                    if (step < lastStep) step++ else {
                        haptics.success()
                        onFinish(OnboardingResult(goals, challengeId, keepExamples))
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    val reduceMotionLocal = com.planthabit.ui.components.LocalReduceMotion.current
    val c = PlantTheme.colors
    val sway = rememberInfiniteTransition(label = "welcomeSway")
    val angle by sway.animateFloat(-3f, 3f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "welcomeAngle")
    Image(
        painter = painterResource(R.drawable.plant_stage_2),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(220.dp)
            .graphicsLayer {
                rotationZ = if (reduceMotionLocal) 0f else angle
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.88f)
            }
    )
    Spacer(Modifier.height(16.dp))
    Text("Welcome to PlantHabit", style = MaterialTheme.typography.headlineLarge, color = c.textPrimary, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Text(
        "Small daily habits, grown like a garden. Tick them off, keep your streaks, and watch your plant flourish.",
        style = MaterialTheme.typography.bodyLarge,
        color = c.textSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    val c = PlantTheme.colors
    Text(title, style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, textAlign = TextAlign.Center)
    Spacer(Modifier.height(6.dp))
    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun GoalChip(goal: StarterGoal, selected: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    val accent = c.iconAccent(goal.icon)
    val bg by animateColorAsState(if (selected) accent.color else accent.soft, tween(200), label = "goalBg")
    val fg by animateColorAsState(if (selected) c.onAccent else accent.color, tween(200), label = "goalFg")
    Row(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(bg)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("goal_${goal.name}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (selected) Icons.Default.Check else goal.icon.vector, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(goal.label, style = MaterialTheme.typography.titleSmall, color = fg)
    }
}

@Composable
private fun StarterChallengeRow(challenge: ChallengeEntity, selected: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    val banner = rememberBannerSource(challenge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(c.card)
            .border(2.dp, if (selected) c.primary else c.border, AppShapes.large)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(10.dp)
            .testTag("starter_${challenge.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(width = 96.dp, height = 64.dp).clip(AppShapes.medium).background(c.cardMuted)) {
            if (banner != null) ChallengeBannerImage(banner, contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(challenge.title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Text("${challenge.durationDays} days · ${challenge.subtitle}", style = MaterialTheme.typography.bodySmall, color = c.textSecondary, maxLines = 2)
        }
        if (selected) {
            IconBubble(background = c.primary, size = 28.dp) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = c.onPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun OptionRow(selected: Boolean, title: String, subtitle: String, onClick: () -> Unit) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(if (selected) c.primarySoft else c.card)
            .border(1.dp, if (selected) c.primary else c.border, AppShapes.medium)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = c.primary)
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = PlantTheme.colors
    Box(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(c.leafGradient)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = c.onPrimary)
    }
}
