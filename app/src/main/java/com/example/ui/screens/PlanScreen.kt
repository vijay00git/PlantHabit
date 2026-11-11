package com.example.ui.screens

import com.example.ui.components.iconAccent
import com.example.ui.components.AppIcon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarEventItem
import com.example.data.model.HabitWithStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.HorizontalDateStrip
import com.example.ui.components.IconBubble
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.backgroundGlow
import com.example.ui.theme.categoryColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PlanScreen(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    viewMode: String, // "Day", "Week"
    onViewModeChange: (String) -> Unit,
    habitsWithStatus: List<HabitWithStatus>,
    calendarEvents: List<CalendarEventItem>,
    isCalendarPermissionGranted: Boolean,
    onRequestCalendarPermission: () -> Unit,
    onSyncWithCalendar: () -> Unit,
    onToggleHabit: (HabitWithStatus) -> Unit,
    onIncrementHabit: (HabitWithStatus, Int) -> Unit,
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors

    val monthYearText = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            } else "November 2026"
        } catch (e: Exception) {
            "November 2026"
        }
    }

    val nowTimeString = remember {
        SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())
    }

    // Sort habits chronologically by scheduledTime
    val sortedHabits = remember(habitsWithStatus) {
        habitsWithStatus.sortedBy { it.habit.scheduledTime }
    }
    // Index of the last habit scheduled at or before the current time; "Now" goes right after it
    // (-1 = everything is later today, so "Now" goes above the first habit).
    val nowIndex = remember(sortedHabits) {
        val now24 = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        sortedHabits.indexOfLast { it.habit.scheduledTime.padStart(5, '0').take(5) <= now24 }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(c.backgroundGlow)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top Header: Month Year + Day/Week Toggle
            item(key = "plan_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = monthYearText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ViewModeToggle(
                            modes = listOf("Day", "Week"),
                            selected = viewMode,
                            onSelect = onViewModeChange
                        )

                        // Calendar View Icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .bounceClick(pressedScale = 0.88f) { onRequestCalendarPermission() }
                                .clip(CircleShape)
                                .background(c.card)
                                .border(1.dp, c.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Month Calendar",
                                tint = c.sky,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Horizontal Date Strip
            item(key = "plan_date_strip") {
                HorizontalDateStrip(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )
            }

            item(key = "plan_timeline_top") { Spacer(Modifier.height(12.dp)) }

            // Vertical Connected Schedule Timeline
            itemsIndexed(
                items = sortedHabits,
                key = { _, h -> "habit_${h.habit.id}" }
            ) { index, habitWithStatus ->
                val habit = habitWithStatus.habit
                val isCompleted = habitWithStatus.isCompleted
                val isLast = index == sortedHabits.lastIndex

                Column(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .staggeredEntrance(index)
                ) {
                    if (index == 0 && nowIndex == -1) {
                        NowIndicatorRow(timeString = nowTimeString, onAddTask = onAddTaskClick)
                    }
                    TimelineRow(
                        timeLabel = habit.scheduledTime,
                        title = habit.title,
                        subtitle = habit.description.ifEmpty {
                            if (habit.targetValue > 1) "%,d / %,d %s".format(habitWithStatus.currentProgress, habit.targetValue, habit.unit)
                            else "Scheduled habit"
                        },
                        iconName = habit.iconName,
                        accent = c.iconAccent(AppIcon.of(habit.iconName)),
                        isCompleted = isCompleted,
                        onToggle = { onToggleHabit(habitWithStatus) },
                        onIncrement = {
                            val step = if (habit.unit == "ml") 250 else if (habit.unit == "steps") 500 else 1
                            onIncrementHabit(habitWithStatus, step)
                        },
                        isStepOrWater = habit.targetValue > 1,
                        isLast = isLast
                    )

                    // "Now" marker sits at the real current time
                    if (index == nowIndex) {
                        NowIndicatorRow(
                            timeString = nowTimeString,
                            onAddTask = onAddTaskClick
                        )
                    }
                }
            }

            // If habits empty
            if (sortedHabits.isEmpty()) {
                item(key = "plan_empty") {
                    EmptyState(
                        emoji = "🌱",
                        title = "Nothing scheduled",
                        message = "No scheduled habits for this time slot.",
                        modifier = Modifier.animateItem(),
                        action = {
                            Row(
                                modifier = Modifier
                                    .bounceClick { onAddTaskClick() }
                                    .clip(AppShapes.pill)
                                    .background(c.primary)
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = c.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Add task",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.onPrimary
                                )
                            }
                        }
                    )
                }
            }

            // Calendar Events if synced
            if (calendarEvents.isNotEmpty()) {
                item(key = "plan_events_header") {
                    Text(
                        text = "GOOGLE CALENDAR EVENTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMuted,
                        modifier = Modifier
                            .animateItem()
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                    )
                }

                itemsIndexed(
                    items = calendarEvents,
                    key = { i, e -> "event_${e.id}_${e.startTime}_$i" }
                ) { i, event ->
                    val timeStr = remember(event.startTime) {
                        val startCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(startCal.time)
                    }
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .staggeredEntrance(i)
                            .clip(AppShapes.medium)
                            .background(c.card)
                            .border(1.dp, c.border, AppShapes.medium)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconBubble(background = c.skySoft, size = 38.dp) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = c.sky,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textPrimary
                            )
                            Text(
                                text = "$timeStr ${event.location?.let { "• $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary
                            )
                        }
                    }
                }
            }

            // Google Calendar Sync Prompt Card
            item(key = "plan_calendar_banner") {
                Row(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .bounceClick(pressedScale = 0.97f) {
                            if (!isCalendarPermissionGranted) {
                                onRequestCalendarPermission()
                            } else {
                                onSyncWithCalendar()
                            }
                        }
                        .clip(AppShapes.large)
                        .background(c.skySoft)
                        .border(1.dp, c.sky.copy(alpha = 0.3f), AppShapes.large)
                        .padding(16.dp)
                        .testTag("btn_sync_google_calendar"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(AppShapes.small)
                            .background(c.sky),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCalendarPermissionGranted) Icons.Default.Sync else Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = c.onAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCalendarPermissionGranted) "Google Calendar synced" else "Show my calendar events",
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textPrimary
                        )
                        Text(
                            text = if (isCalendarPermissionGranted) "Tap to push all habits as reminders to your device Calendar" else "Read-only, from your phone. Nothing leaves your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = c.sky,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/** Segmented control with a pill indicator that slides between options. */
@Composable
private fun ViewModeToggle(
    modes: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val c = PlantTheme.colors
    val segmentWidth = 58.dp
    val segmentHeight = 32.dp
    val selectedIndex = modes.indexOf(selected).coerceAtLeast(0)
    val indicatorOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "viewModeIndicator"
    )

    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.pill)
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .size(width = segmentWidth, height = segmentHeight)
                .clip(AppShapes.pill)
                .background(c.primary)
        )
        Row {
            modes.forEach { mode ->
                val isSelected = mode == selected
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) c.onPrimary else c.textSecondary,
                    animationSpec = tween(250),
                    label = "viewModeText"
                )
                Box(
                    modifier = Modifier
                        .size(width = segmentWidth, height = segmentHeight)
                        .bounceClick(pressedScale = 0.92f, haptic = !isSelected) {
                            if (!isSelected) onSelect(mode)
                        }
                        .testTag("plan_toggle_$mode"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    timeLabel: String,
    title: String,
    subtitle: String,
    iconName: String,
    accent: AccentPair,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    isStepOrWater: Boolean,
    isLast: Boolean
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()

    val bubbleColor by animateColorAsState(
        targetValue = if (isCompleted) accent.color else accent.soft,
        animationSpec = tween(350),
        label = "timelineBubble"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isCompleted) c.onAccent else accent.color,
        animationSpec = tween(350),
        label = "timelineIcon"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        // Time Label on left
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 12.dp)
        )

        // Connected timeline vertical axis
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            val iconVector = AppIcon.of(iconName).vector

            IconBubble(background = bubbleColor, size = 40.dp) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(3.dp)
                        .weight(1f)
                        .clip(AppShapes.pill)
                        .background(
                            Brush.verticalGradient(
                                listOf(accent.color.copy(alpha = 0.55f), accent.color.copy(alpha = 0.08f))
                            )
                        )
                )
            }
        }

        // Habit details and action
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, bottom = 14.dp)
                .clip(AppShapes.medium)
                .background(c.card)
                .border(1.dp, if (isCompleted) accent.color.copy(alpha = 0.45f) else c.border, AppShapes.medium)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )

                AnimatedVisibility(
                    visible = isCompleted,
                    enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.9f),
                    exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f)
                ) {
                    Text(
                        text = "🏆 You owned that time.",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.sunrise,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Action button: (+) or Checkbox
            if (isStepOrWater && !isCompleted) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .bounceClick(pressedScale = 0.85f) { onIncrement() }
                        .clip(CircleShape)
                        .background(accent.soft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increment",
                        tint = accent.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                val checkFill by animateColorAsState(
                    targetValue = if (isCompleted) accent.color else Color.Transparent,
                    animationSpec = tween(300),
                    label = "checkFill"
                )
                val checkBorder by animateColorAsState(
                    targetValue = if (isCompleted) accent.color else c.border,
                    animationSpec = tween(300),
                    label = "checkBorder"
                )
                val checkScale by animateFloatAsState(
                    targetValue = if (isCompleted) 1f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "checkScale"
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .bounceClick(pressedScale = 0.85f, haptic = false) {
                            if (isCompleted) haptics.tick() else haptics.success()
                            onToggle()
                        }
                        .clip(CircleShape)
                        .background(checkFill)
                        .border(2.dp, checkBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (checkScale > 0.01f) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = c.onAccent,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = checkScale
                                    scaleY = checkScale
                                    alpha = checkScale.coerceIn(0f, 1f)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowIndicatorRow(
    timeString: String,
    onAddTask: () -> Unit
) {
    val c = PlantTheme.colors
    val pulse = rememberInfiniteTransition(label = "nowPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "nowPulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                style = MaterialTheme.typography.labelMedium,
                color = c.primary,
                modifier = Modifier.width(44.dp)
            )

            // Pulsing dot on the timeline
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = (1.8f - pulseScale) / 0.8f * 0.5f
                        }
                        .clip(CircleShape)
                        .background(c.primary)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(c.primary)
                )
            }

            // "Now" label & horizontal line
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.primarySoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Now",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(AppShapes.pill)
                        .background(
                            Brush.horizontalGradient(listOf(c.primary, c.primary.copy(alpha = 0f)))
                        )
                )
            }
        }

        // Add Task button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 54.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .bounceClick { onAddTask() }
                    .clip(AppShapes.pill)
                    .background(c.primarySoft)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("btn_add_task_inline"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Add task",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.primary
                )
            }
        }
    }
}
