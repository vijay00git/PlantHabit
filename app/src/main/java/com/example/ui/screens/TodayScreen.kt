package com.example.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitWithStatus
import com.example.data.model.MoodLogEntity
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.EmptyState
import com.example.ui.components.GoalCounterCard
import com.example.ui.components.HabitCard
import com.example.ui.components.HorizontalDateStrip
import com.example.ui.components.IconBubble
import com.example.ui.components.MoodTrackerCard
import com.example.ui.components.PlantCard
import com.example.ui.components.PlantStreakCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.RoutineCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.backgroundGlow
import com.example.ui.theme.dreamGradient
import com.example.ui.theme.heroGradient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TodayScreen(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    habitsWithStatus: List<HabitWithStatus>,
    onToggleHabit: (HabitWithStatus) -> Unit,
    onOpenHabitDetails: (com.example.data.model.HabitEntity) -> Unit = {},
    onIncrementHabit: (HabitWithStatus, Int) -> Unit,
    onOpenFocusMode: () -> Unit,
    activeStreakDays: Int,
    currentMood: MoodLogEntity? = null,
    onSelectMood: (Int, String) -> Unit = { _, _ -> },
    goalChallenges: List<ChallengeEntity> = emptyList(),
    moodGoodPercent: Int? = null,
    onIncrementGoal: (String, Float) -> Unit = { _, _ -> },
    onOpenChallengeDetails: (ChallengeEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    layout: TodayLayout = TodayLayout(),
    onLayoutChange: (TodayLayout) -> Unit = {},
    onSaveHabitOrder: (List<Long>) -> Unit = {},
) {
    val c = PlantTheme.colors

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning ☀️"
            hour < 17 -> "Good afternoon 🌤️"
            else -> "Good evening 🌙"
        }
    }

    val displayDateHeader = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                SimpleDateFormat("EEEE · d MMMM", Locale.getDefault()).format(date).uppercase()
            } else "SUNDAY · 8 NOVEMBER"
        } catch (e: Exception) {
            "SUNDAY · 8 NOVEMBER"
        }
    }

    // Split habits
    val routineHabits = habitsWithStatus.filter { it.habit.routineGroup != null }
    val standaloneHabits = habitsWithStatus.filter { it.habit.routineGroup == null }

    val filteredHabits = if (currentFilter == "ALL") {
        standaloneHabits
    } else {
        standaloneHabits.filter { it.habit.timeOfDay.equals(currentFilter, ignoreCase = true) }
    }

    val completedCount = habitsWithStatus.count { it.isCompleted }
    val totalCount = habitsWithStatus.size
    val joinedGoals = goalChallenges.filter { it.isJoined && it.goalTarget > 0 }

    var reorderMode by rememberSaveable { mutableStateOf(false) }
    var reorderList by remember(standaloneHabits.map { it.habit.id }, reorderMode) {
        mutableStateOf(standaloneHabits.map { it.habit })
    }
    var customizing by remember { mutableStateOf(false) }
    if (customizing) {
        CustomizeTodaySheet(layout = layout, onChange = onLayoutChange, onDismiss = { customizing = false })
    }

    val headerSection: LazyListScope.() -> Unit = {
        // Top Header over a soft green glow
        item(key = "header") {
            Column(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .background(c.backgroundGlow)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
                    .staggeredEntrance(0)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayDateHeader,
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textSecondary,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconButton(
                            icon = Icons.Default.RemoveRedEye,
                            description = "Focus Mode",
                            onClick = onOpenFocusMode,
                            testTag = "btn_focus_mode"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = c.textPrimary
                )
                Text(
                    text = "Let's help your habits grow today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
            }
        }

        // Horizontal Date Strip
        item(key = "date_strip") {
            Box(modifier = Modifier.animateItem().staggeredEntrance(1)) {
                HorizontalDateStrip(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
            }
        }

        // Daily progress hero card
        item(key = "daily_progress") {
            Box(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .staggeredEntrance(2)
            ) {
                DailyProgressCard(completed = completedCount, total = totalCount)
            }
        }


    }

    val habitsSection: LazyListScope.() -> Unit = {
        // Filter Pills (ALL, MORNING, AFTERNOON, EVENING)
        item(key = "filters") {
            val filters = listOf("ALL", "MORNING", "AFTERNOON", "EVENING")
            Row(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .staggeredEntrance(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterPill(
                        filter = filter,
                        isSelected = currentFilter == filter,
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }
        }

        // Section Title
        item(key = "habits_title") {
            SectionHeader(
                title = "Today's habits",
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                action = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (reorderMode) {
                            Box(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.primary)
                                    .bounceClick {
                                        onSaveHabitOrder(reorderList.map { it.id })
                                        reorderMode = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                    .testTag("btn_reorder_done")
                            ) {
                                Text("Done", style = MaterialTheme.typography.labelLarge, color = c.onPrimary)
                            }
                        } else {
                            if (standaloneHabits.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .clip(AppShapes.pill)
                                        .bounceClick {
                                            onFilterSelected("ALL")
                                            reorderMode = true
                                        }
                                        .background(c.cardMuted)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("btn_reorder"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reorder", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.primarySoft)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                AnimatedCounter(
                                    value = filteredHabits.size,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.primary
                                )
                            }
                        }
                    }
                }
            )
        }

        if (filteredHabits.isEmpty()) {
            item(key = "habits_empty") {
                val (emoji, title, message) = when (currentFilter) {
                    "MORNING" -> Triple("🌅", "No morning habits", "Mornings are a great time to plant a new routine.")
                    "AFTERNOON" -> Triple("🌤️", "Nothing this afternoon", "Enjoy the free time, or add a midday habit.")
                    "EVENING" -> Triple("🌙", "Quiet evening", "Wind down with a calming evening habit.")
                    else -> Triple("🌱", "Your garden is empty", "Tap the + button to plant your first habit.")
                }
                EmptyState(
                    emoji = emoji,
                    title = title,
                    message = message,
                    modifier = Modifier.animateItem()
                )
            }
        }


        if (reorderMode) {
            itemsIndexed(reorderList, key = { _, h -> "reorder_${h.id}" }) { index, h ->
                val icon = com.example.ui.components.AppIcon.of(h.iconName)
                Row(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(AppShapes.medium)
                        .background(c.card)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon.vector, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(h.title, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary, modifier = Modifier.weight(1f))
                    MoveButton(Icons.Default.KeyboardArrowUp, "Move up", enabled = index > 0) {
                        reorderList = reorderList.swap(index, index - 1)
                    }
                    MoveButton(Icons.Default.KeyboardArrowDown, "Move down", enabled = index < reorderList.lastIndex) {
                        reorderList = reorderList.swap(index, index + 1)
                    }
                }
            }
        } else {
            // Habits List
            itemsIndexed(filteredHabits, key = { _, h -> "habit_${h.habit.id}" }) { index, habitWithStatus ->
                Box(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .staggeredEntrance(index)
                ) {
                    HabitCard(
                        habitWithStatus = habitWithStatus,
                        onToggleCompleted = { onToggleHabit(habitWithStatus) },
                        onIncrement = { amount -> onIncrementHabit(habitWithStatus, amount) },
                        onOpenDetails = { onOpenHabitDetails(habitWithStatus.habit) }
                    )
                }
            }

        }
    }

    val cardsSection: LazyListScope.() -> Unit = {
        layout.visible.forEach { card ->
            when (card) {
                TodayCard.STREAK -> {
                // Plant Streak Card
                item(key = "plant_streak") {
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .staggeredEntrance(3)
                    ) {
                        PlantStreakCard(
                            completedCount = completedCount,
                            totalCount = totalCount,
                            activeStreakDays = activeStreakDays
                        )
                    }
                }


                }
                TodayCard.GOALS -> {
                // Active Goal Counter Cards
                itemsIndexed(joinedGoals, key = { _, goal -> "goal_${goal.id}" }) { index, goalChallenge ->
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .staggeredEntrance(4 + index)
                    ) {
                        GoalCounterCard(
                            goalChallenge = goalChallenge,
                            onIncrement = { delta -> onIncrementGoal(goalChallenge.id, delta) },
                            onClickDetails = { onOpenChallengeDetails(goalChallenge) }
                        )
                    }
                }


                }
                TodayCard.ROUTINES -> {
                // One routine card per group (a joined challenge or a named routine)
                routineHabits.groupBy { it.habit.routineGroup!! }.forEach { (group, groupHabits) ->
                    val linked = goalChallenges.firstOrNull { ch ->
                        ch.isJoined && (groupHabits.any { it.habit.challengeId == ch.id } || ch.title == group)
                    }
                    item(key = "routine_$group") {
                        Box(
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .staggeredEntrance(5)
                        ) {
                            RoutineCard(
                                title = group,
                                dayProgressText = linked?.let { "Day ${it.currentDay} of ${it.durationDays}" } ?: "Daily routine",
                                routineHabits = groupHabits,
                                onToggleHabit = onToggleHabit,
                                challenge = linked
                            )
                        }
                    }
                }


                }
                TodayCard.MOOD -> {
                // Daily Mood Tracker Card
                item(key = "mood") {
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .staggeredEntrance(6)
                    ) {
                        MoodTrackerCard(
                            currentMood = currentMood,
                            onSelectMood = onSelectMood,
                            goodPercent = moodGoodPercent
                        )
                    }
                }


                }
                TodayCard.FOCUS -> {
                // Focus & Flow card
                item(key = "focus") {
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .staggeredEntrance(7)
                    ) {
                        FocusCard(onOpenFocusMode = onOpenFocusMode)
                    }
                }


                }
            }
        }
        item(key = "customize_today") {
            Row(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(AppShapes.pill)
                    .bounceClick { customizing = true }
                    .background(c.cardMuted)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("btn_customize_today"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Customize Today", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
    }

    // Wide screens (tablets, the 14" display): cards beside the habits
    if (LocalConfiguration.current.screenWidthDp >= 840) {
        Row(modifier = modifier.fillMaxSize().background(c.background)) {
            LazyColumn(
                modifier = Modifier.weight(0.45f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                headerSection()
                cardsSection()
            }
            LazyColumn(
                modifier = Modifier.weight(0.55f).fillMaxHeight(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
            ) {
                habitsSection()
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(c.background),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            headerSection()
            habitsSection()
            cardsSection()
        }
    }
}

@Composable
private fun DailyProgressCard(completed: Int, total: Int) {
    val c = PlantTheme.colors
    val ratio = if (total > 0) completed.toFloat() / total else 0f
    val percent = (ratio * 100).toInt()
    val remaining = (total - completed).coerceAtLeast(0)
    val message = when {
        total == 0 -> "No habits yet. Plant your first one 🌱"
        remaining == 0 -> "All done! Your garden is thriving 🌿"
        completed == 0 -> "Fresh start. Let's grow something today."
        remaining == 1 -> "Just 1 more to go. You've got this!"
        else -> "$remaining more to go. Keep it growing!"
    }

    PlantCard(
        brush = c.heroGradient,
        borderColor = Color.Transparent,
        contentPadding = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = ratio,
                size = 104.dp,
                strokeWidth = 12.dp,
                colors = listOf(c.onPrimary, c.onPrimary.copy(alpha = 0.85f)),
                trackColor = c.onPrimary.copy(alpha = 0.22f)
            ) {
                AnimatedCounter(
                    value = percent,
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.onPrimary,
                    suffix = "%"
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.onPrimary.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedCounter(
                        value = completed,
                        style = MaterialTheme.typography.displaySmall,
                        color = c.onPrimary
                    )
                    Text(
                        text = " / $total",
                        style = MaterialTheme.typography.titleLarge,
                        color = c.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onPrimary.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun FocusCard(onOpenFocusMode: () -> Unit) {
    val c = PlantTheme.colors
    PlantCard(
        onClick = onOpenFocusMode,
        brush = c.dreamGradient,
        borderColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(AppShapes.medium)
                        .background(c.onAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Momentum",
                        tint = c.onAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "DAILY HABIT MOMENTUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.onAccent.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Focus & Flow State",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.onAccent
                    )
                    Text(
                        text = "Tap to launch distraction-free focus timer",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onAccent.copy(alpha = 0.85f)
                    )
                }
            }

            IconBubble(background = c.onAccent.copy(alpha = 0.18f), size = 36.dp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = c.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    filter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val c = PlantTheme.colors
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) c.primary else c.card,
        animationSpec = tween(300),
        label = "pill_bg_$filter"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) c.onPrimary else c.textSecondary,
        animationSpec = tween(300),
        label = "pill_fg_$filter"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) c.primary else c.border,
        animationSpec = tween(300),
        label = "pill_border_$filter"
    )
    val icon: ImageVector = when (filter) {
        "MORNING" -> Icons.Default.WbSunny
        "AFTERNOON" -> Icons.Default.WbTwilight
        "EVENING" -> Icons.Default.NightsStay
        else -> Icons.Default.AllInclusive
    }
    val label = filter.lowercase().replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .clip(AppShapes.pill)
            .background(pillColor)
            .border(1.dp, borderColor, AppShapes.pill)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("filter_pill_$filter"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    val c = PlantTheme.colors
    Box(
        modifier = Modifier
            .bounceClick(pressedScale = 0.88f, onClick = onClick)
            .size(38.dp)
            .clip(CircleShape)
            .background(c.card)
            .border(1.dp, c.border, CircleShape)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = c.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
