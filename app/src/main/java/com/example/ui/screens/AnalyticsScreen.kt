package com.example.ui.screens

import com.example.ui.components.MoodCalendar
import com.example.ui.components.HabitHeatmaps
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitEntity
import androidx.compose.material.icons.filled.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.AnalyticsData
import com.example.data.repository.WeeklyStat
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.backgroundGlow
import com.example.ui.theme.categoryColors
import com.example.ui.theme.heroGradient
import com.example.ui.theme.sunriseGradient
import kotlinx.coroutines.delay

@Composable
fun AnalyticsScreen(
    analyticsData: AnalyticsData?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    habits: List<HabitEntity> = emptyList(),
    logs: List<HabitLogEntity> = emptyList(),
    onOpenHabit: (HabitEntity) -> Unit = {},
    moods: List<com.example.data.model.MoodLogEntity> = emptyList(),
    unlockedAchievements: Map<String, Long> = emptyMap(),
    weeklyRecap: com.example.data.repository.WeeklyRecap? = null,
) {
    val c = PlantTheme.colors
    // Shown for the moment before real analytics load
    val data = analyticsData ?: AnalyticsData(
        totalHabits = 0,
        overallCompletionRate = 0,
        currentPlantStage = "Seedling",
        activeStreakDays = 0,
        longestStreakDays = 0,
        weeklyStats = emptyList(),
        categoryStats = emptyMap(),
        timeOfDayStats = emptyMap()
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        // Soft green wash behind the header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(c.backgroundGlow)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Top Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stats",
                            style = MaterialTheme.typography.headlineMedium,
                            color = c.textPrimary
                        )
                        Text(
                            text = "Your consistency, streaks and history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .bounceClick(pressedScale = 0.88f) { onOpenSettings() }
                            .clip(CircleShape)
                            .background(c.card)
                            .border(1.dp, c.border, CircleShape)
                            .testTag("btn_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = c.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Hero: plant growth stage & streak
            item {
                PlantGrowthHero(
                    data = data,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(0)
                )
            }

            // This week's recap
            if (weeklyRecap != null) {
                item(key = "weekly_recap") {
                    com.example.ui.components.WeeklyRecapCard(
                        recap = weeklyRecap,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .staggeredEntrance(1)
                    )
                }
            }

            // Per-habit monthly heat maps
            item(key = "heatmaps") {
                PlantCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(1),
                    contentPadding = 16.dp
                ) {
                    HabitHeatmaps(habits = habits, logs = logs, onOpenHabit = onOpenHabit)
                }
            }

            // Achievements
            item(key = "achievements") {
                PlantCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(2),
                    contentPadding = 16.dp
                ) {
                    com.example.ui.components.AchievementsGrid(unlocked = unlockedAchievements)
                }
            }

            // Monthly mood calendar (emoji per day)
            item(key = "mood_calendar") {
                PlantCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(2),
                    contentPadding = 16.dp
                ) {
                    MoodCalendar(moods = moods)
                }
            }

            // Stat tiles
            item {
                val tiles = listOf(
                    StatTileSpec("Habits", data.totalHabits, "", Icons.Default.Spa, AccentPair(c.primary, c.primarySoft)),
                    StatTileSpec("Check-ins", data.totalHabitsChecked, "", Icons.Default.CheckCircle, AccentPair(c.sky, c.skySoft)),
                    StatTileSpec("Day streak", data.activeStreakDays, "", Icons.Default.LocalFireDepartment, AccentPair(c.sunrise, c.sunriseSoft)),
                    StatTileSpec("Completion", data.overallCompletionRate, "%", Icons.Default.Percent, AccentPair(c.lavender, c.lavenderSoft)),
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tiles.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEachIndexed { colIndex, spec ->
                                val index = rowIndex * 2 + colIndex
                                StatTile(
                                    spec = spec,
                                    index = index,
                                    modifier = Modifier
                                        .weight(1f)
                                        .staggeredEntrance(index + 1)
                                )
                            }
                        }
                    }
                }
            }

            // Time Invested Breakdown
            item {
                val totalHours = data.totalTimeInvestedMinutes / 60
                val totalMinutes = data.totalTimeInvestedMinutes % 60
                val accents = listOf(
                    AccentPair(c.primary, c.primarySoft),
                    AccentPair(c.sky, c.skySoft),
                    AccentPair(c.sunrise, c.sunriseSoft),
                    AccentPair(c.lavender, c.lavenderSoft),
                    AccentPair(c.sun, c.sunSoft),
                    AccentPair(c.berry, c.berrySoft),
                )
                val reveal = rememberDrawIn(durationMs = 900, delayMs = 120)
                val shownHours = rememberCountUp(totalHours)
                val shownMinutes = rememberCountUp(totalMinutes)

                AnalyticsCard(index = 5) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CardHeader(
                            icon = Icons.Default.Schedule,
                            accent = AccentPair(c.sky, c.skySoft),
                            title = "Time Invested"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedCounter(
                                    value = shownHours,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.sky,
                                    suffix = " h "
                                )
                                AnimatedCounter(
                                    value = shownMinutes,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.sky,
                                    suffix = " min"
                                )
                            }
                        }

                        Text(
                            text = "12-Week Time Investment in Self-Growth",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )

                        val breakdown = data.timeInvestedBreakdown
                        if (breakdown.isEmpty()) {
                            Text(
                                text = "Log progress on minute-based habits (like \"15 mins\") to see where your time goes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMuted
                            )
                        }
                        breakdown.forEachIndexed { i, (habitTitle, minutes) ->
                            val itemHours = minutes / 60
                            val itemMins = minutes % 60
                            val ratio = (minutes.toFloat() / data.totalTimeInvestedMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                            val accent = accents[i % accents.size]
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(accent.color)
                                        )
                                        Text(
                                            text = habitTitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textPrimary
                                        )
                                    }
                                    Text(
                                        text = "$itemHours h $itemMins min",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = c.textSecondary
                                    )
                                }
                                GradientBar(
                                    fraction = ratio * staggered(reveal, i, breakdown.size),
                                    color = accent.color,
                                    trackColor = c.cardMuted,
                                    height = 8.dp
                                )
                            }
                        }
                    }
                }
            }

            // Mood Balance & Trends
            item {
                var moodTarget by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(data.moodGoodPercentage) {
                    delay(200)
                    moodTarget = data.moodGoodPercentage / 100f
                }
                val shownMood = rememberCountUp(data.moodGoodPercentage)

                AnalyticsCard(index = 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProgressRing(
                            progress = moodTarget,
                            size = 84.dp,
                            strokeWidth = 9.dp,
                            colors = listOf(c.sun, c.primary)
                        ) {
                            AnimatedCounter(
                                value = shownMood,
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textPrimary,
                                suffix = "%"
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = null,
                                    tint = c.sun,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Mood Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.textPrimary
                                )
                            }

                            Text(
                                text = if (data.moodTotalDays == 0) "No check-ins yet" else "${data.moodGoodPercentage}% Good or great",
                                style = MaterialTheme.typography.titleSmall,
                                color = c.primary
                            )

                            Text(
                                text = if (data.moodTotalDays == 0) "Log your mood on the Today screen" else "Based on ${data.moodTotalDays} check-ins in the past 7 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary
                            )
                        }
                    }
                }
            }

            // Weekly Completion Chart
            item {
                val days = data.weeklyStats.ifEmpty {
                    val fmt = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                    (6 downTo 0).map { back ->
                        val day = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -back) }
                        WeeklyStat(fmt.format(day.time), "", 0f, 0, 0)
                    }
                }

                AnalyticsCard(index = 7) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CardHeader(
                            icon = Icons.Default.Timeline,
                            accent = AccentPair(c.primary, c.primarySoft),
                            title = "7-Day Velocity"
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.primarySoft)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Target: 100%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.primary
                                )
                            }
                        }

                        WeeklyVelocityChart(days = days)
                    }
                }
            }

            // Category Breakdown
            item {
                val categories = data.categoryCompletion.map { (name, pct) -> name to pct / 100f }
                val reveal = rememberDrawIn(durationMs = 900, delayMs = 120)

                AnalyticsCard(index = 8) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CardHeader(
                            icon = Icons.Default.Insights,
                            accent = AccentPair(c.lavender, c.lavenderSoft),
                            title = "Consistency by Category"
                        )
                        if (categories.isEmpty()) {
                            Text(
                                text = "Add habits to see how consistent you are in each area.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMuted
                            )
                        }

                        categories.forEachIndexed { i, (category, ratio) ->
                            val accent = c.categoryColors(category)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(AppShapes.pill)
                                            .background(accent.soft)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accent.color
                                        )
                                    }
                                    Text(
                                        text = "${(ratio * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = accent.color
                                    )
                                }

                                GradientBar(
                                    fraction = ratio * staggered(reveal, i, categories.size),
                                    color = accent.color,
                                    trackColor = c.cardMuted,
                                    height = 10.dp
                                )
                            }
                        }
                    }
                }
            }

            // Habit Milestones
            item {
                AnalyticsCard(index = 9) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CardHeader(
                            icon = Icons.Default.EmojiEvents,
                            accent = AccentPair(c.sun, c.sunSoft),
                            title = "Streak Milestones"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MilestoneBadge(title = "3 Days", subtitle = "Spark", isUnlocked = data.longestStreakDays >= 3, index = 0)
                            MilestoneBadge(title = "7 Days", subtitle = "Sprout", isUnlocked = data.longestStreakDays >= 7, index = 1)
                            MilestoneBadge(title = "14 Days", subtitle = "Bloom", isUnlocked = data.longestStreakDays >= 14, index = 2)
                            MilestoneBadge(title = "30 Days", subtitle = "Tree", isUnlocked = data.longestStreakDays >= 30, index = 3)
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Animation helpers                                                                            */
/* ------------------------------------------------------------------------------------------ */

/** 0 → 1 draw-in progress that plays once when the composable first appears. */
@Composable
private fun rememberDrawIn(durationMs: Int = 900, delayMs: Long = 0L): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs)
        anim.animateTo(1f, tween(durationMs, easing = FastOutSlowInEasing))
    }
    return anim.value
}

/** Starts at 0 and jumps to [target] after a beat so [AnimatedCounter] rolls up on first show. */
@Composable
private fun rememberCountUp(target: Int, delayMs: Long = 180L): Int {
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(target) {
        delay(delayMs)
        shown = target
    }
    return shown
}

/** Per-item progress for a staggered reveal: item [index] of [count] starts a little after the previous. */
private fun staggered(progress: Float, index: Int, count: Int, step: Float = 0.12f): Float {
    val span = 1f + step * (count - 1).coerceAtLeast(0)
    val local = (progress * span - index * step).coerceIn(0f, 1f)
    return FastOutSlowInEasing.transform(local)
}

/* ------------------------------------------------------------------------------------------ */
/* Building blocks                                                                              */
/* ------------------------------------------------------------------------------------------ */

@Composable
private fun AnalyticsCard(
    index: Int,
    content: @Composable () -> Unit
) {
    PlantCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .staggeredEntrance(index),
        contentPadding = 20.dp
    ) {
        content()
    }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    accent: AccentPair,
    title: String,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = PlantTheme.colors
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
            IconBubble(background = accent.soft, size = 36.dp) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary
            )
        }
        trailing?.invoke()
    }
}

/** Rounded horizontal bar with a soft-to-solid gradient fill. [fraction] is already animated. */
@Composable
private fun GradientBar(
    fraction: Float,
    color: Color,
    trackColor: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(AppShapes.pill)
            .background(trackColor)
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(height)
                    .clip(AppShapes.pill)
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), color)))
            )
        }
    }
}

private data class StatTileSpec(
    val label: String,
    val value: Int,
    val suffix: String,
    val icon: ImageVector,
    val accent: AccentPair
)

@Composable
private fun StatTile(
    spec: StatTileSpec,
    index: Int,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val shown = rememberCountUp(spec.value, delayMs = 220L + index * 70L)
    Column(
        modifier = modifier
            .clip(AppShapes.large)
            .background(spec.accent.soft)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconBubble(background = c.card, size = 38.dp) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                tint = spec.accent.color,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedCounter(
            value = shown,
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
            suffix = spec.suffix
        )
        Text(
            text = spec.label,
            style = MaterialTheme.typography.labelMedium,
            color = spec.accent.color
        )
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Plant growth hero                                                                            */
/* ------------------------------------------------------------------------------------------ */

private data class GrowthStage(val name: String, val minDays: Int)

private val growthStages = listOf(
    GrowthStage("Seedling", 0),
    GrowthStage("Thriving Sprout", 3),
    GrowthStage("Blooming Sprout", 7),
    GrowthStage("Flourishing Plant", 14),
    GrowthStage("Golden Tree", 30),
)

@Composable
private fun PlantGrowthHero(
    data: AnalyticsData,
    modifier: Modifier = Modifier
) {
    val reduceMotionLocal = com.example.ui.components.LocalReduceMotion.current
    val c = PlantTheme.colors
    val onHero = c.onPrimary

    val streak = data.activeStreakDays
    val currentIndex = growthStages.indexOfLast { streak >= it.minDays }.coerceAtLeast(0)
    val next = growthStages.getOrNull(currentIndex + 1)
    val stageProgress = if (next == null) 1f else {
        val from = growthStages[currentIndex].minDays
        ((streak - from).toFloat() / (next.minDays - from).coerceAtLeast(1)).coerceIn(0f, 1f)
    }
    val reveal = rememberDrawIn(durationMs = 1000, delayMs = 250)

    val sway = rememberInfiniteTransition(label = "plantSway")
    val angle by sway.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "plantSwayAngle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.xl)
            .background(c.heroGradient)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(AppShapes.pill)
                            .background(onHero.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GARDEN STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = onHero
                        )
                    }

                    Text(
                        text = data.currentPlantStage,
                        style = MaterialTheme.typography.headlineSmall,
                        color = onHero
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = c.sun,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${data.activeStreakDays} Days Active Streak",
                            style = MaterialTheme.typography.titleSmall,
                            color = onHero
                        )
                    }

                    Text(
                        text = "Best Streak: ${data.longestStreakDays} days • ${data.overallCompletionRate}% completion rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = onHero.copy(alpha = 0.85f)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(onHero.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.PlantStageImage(
                        streakDays = streak,
                        modifier = Modifier
                            .size(84.dp)
                            .graphicsLayer {
                                rotationZ = if (reduceMotionLocal) 0f else angle
                                transformOrigin = TransformOrigin(0.5f, 0.88f)
                            }
                    )
                }
            }

            // Growth progress towards the next stage
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(AppShapes.pill)
                        .background(onHero.copy(alpha = 0.18f))
                ) {
                    val shown = stageProgress * reveal
                    if (shown > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(shown)
                                .height(8.dp)
                                .clip(AppShapes.pill)
                                .background(c.sunriseGradient)
                        )
                    }
                }
                Text(
                    text = if (next == null) "Fully grown — your garden is thriving 🌳"
                    else "${(next.minDays - streak).coerceAtLeast(0)} more days to ${next.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = onHero.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Weekly chart                                                                                 */
/* ------------------------------------------------------------------------------------------ */

@Composable
private fun WeeklyVelocityChart(days: List<WeeklyStat>) {
    val c = PlantTheme.colors
    val reveal = rememberDrawIn(durationMs = 1100, delayMs = 150)

    // Capture theme colours outside of DrawScope.
    val highTop = c.primary
    val highBottom = c.primaryDeep
    val lowTop = c.sun
    val lowBottom = c.sunrise
    val lineColor = c.sky
    val dotFill = c.card
    val count = days.size

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Percent labels
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEachIndexed { i, stat ->
                Text(
                    text = "${(stat.completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (stat.completionRate >= 0.8f) c.primary else c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = staggered(reveal, i, count) }
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (count == 0) return@Canvas
            val slot = size.width / count
            val barWidth = minOf(slot * 0.5f, 26.dp.toPx())
            val topPad = 6.dp.toPx()
            val maxH = size.height - topPad
            val fractions = days.map { it.completionRate.coerceIn(0.06f, 1f) }

            // Soft area + trend line (rises with reveal)
            val points = fractions.mapIndexed { i, f ->
                Offset(slot * (i + 0.5f), size.height - maxH * f * reveal)
            }
            fun Path.smoothThrough(pts: List<Offset>) {
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val cur = pts[i]
                    val midX = (prev.x + cur.x) / 2f
                    cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                }
            }
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                smoothThrough(points)
            }
            val area = Path().apply {
                moveTo(points.first().x, points.first().y)
                smoothThrough(points)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            clipRect(right = size.width * reveal.coerceAtLeast(0.001f)) {
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0f)),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }

            // Gradient bars, staggered
            fractions.forEachIndexed { i, f ->
                val t = staggered(reveal, i, count)
                if (t <= 0f) return@forEachIndexed
                val h = maxH * f * t
                val left = slot * (i + 0.5f) - barWidth / 2f
                val top = size.height - h
                val high = days[i].completionRate >= 0.8f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = if (high) listOf(highTop, highBottom) else listOf(lowTop, lowBottom),
                        startY = top,
                        endY = size.height
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(barWidth / 2.6f, barWidth / 2.6f)
                )
            }

            // Trend line on top
            clipRect(right = size.width * reveal.coerceAtLeast(0.001f)) {
                drawPath(
                    path = line,
                    color = lineColor.copy(alpha = 0.9f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            points.forEachIndexed { i, p ->
                val t = staggered(reveal, i, count)
                if (t <= 0f) return@forEachIndexed
                drawCircle(color = dotFill, radius = 4.dp.toPx() * t, center = p)
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx() * t,
                    center = p,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Day labels
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { stat ->
                Text(
                    text = stat.dayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Milestones                                                                                   */
/* ------------------------------------------------------------------------------------------ */

@Composable
private fun MilestoneBadge(
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    index: Int = 0
) {
    val c = PlantTheme.colors
    val pop = rememberDrawIn(durationMs = 500, delayMs = 200L + index * 90L)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    val s = 0.6f + 0.4f * pop
                    scaleX = s
                    scaleY = s
                    alpha = pop
                }
                .clip(CircleShape)
                .then(
                    if (isUnlocked) Modifier.background(c.sunriseGradient)
                    else Modifier
                        .background(c.cardMuted)
                        .border(1.dp, c.border, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.Park else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) c.onAccent else c.textMuted,
                modifier = Modifier.size(if (isUnlocked) 26.dp else 20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isUnlocked) c.textPrimary else c.textMuted
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) c.sunrise else c.textMuted
        )
    }
}
