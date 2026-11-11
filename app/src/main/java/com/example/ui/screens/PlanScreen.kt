package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEventItem
import com.example.data.model.HabitWithStatus
import com.example.ui.components.HorizontalDateStrip
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutBackgroundDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SproutBackgroundDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header: Month Year + Day/Week Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthYearText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day / Week pill toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF211E2E))
                            .padding(3.dp)
                    ) {
                        Row {
                            listOf("Day", "Week").forEach { mode ->
                                val isSelected = viewMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) SproutPrimary else Color.Transparent)
                                        .clickable { onViewModeChange(mode) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .testTag("plan_toggle_$mode"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Calendar View Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF211E2E))
                            .clickable { onRequestCalendarPermission() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Month Calendar",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Date Strip
        item {
            HorizontalDateStrip(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }

        // Vertical Connected Schedule Timeline
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                sortedHabits.forEachIndexed { index, habitWithStatus ->
                    val habit = habitWithStatus.habit
                    val isCompleted = habitWithStatus.isCompleted
                    val isLast = index == sortedHabits.lastIndex

                    TimelineRow(
                        timeLabel = habit.scheduledTime,
                        title = habit.title,
                        subtitle = habit.description.ifEmpty {
                            if (habit.targetValue > 1) "%,d / %,d %s".format(habitWithStatus.currentProgress, habit.targetValue, habit.unit)
                            else "Scheduled habit"
                        },
                        iconName = habit.iconName,
                        isCompleted = isCompleted,
                        onToggle = { onToggleHabit(habitWithStatus) },
                        onIncrement = {
                            val step = if (habit.unit == "ml") 250 else if (habit.unit == "steps") 500 else 1
                            onIncrementHabit(habitWithStatus, step)
                        },
                        isStepOrWater = habit.targetValue > 1,
                        isLast = isLast
                    )

                    // Render "Now" indicator line after 9:00 am or matching screenshot
                    if (index == 2 || (index == sortedHabits.size / 2)) {
                        NowIndicatorRow(
                            timeString = nowTimeString,
                            onAddTask = onAddTaskClick
                        )
                    }
                }

                // If habits empty
                if (sortedHabits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No scheduled habits for this time slot.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Calendar Events if synced
        if (calendarEvents.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "GOOGLE CALENDAR EVENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    calendarEvents.forEach { event ->
                        val startCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(startCal.time)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF221E33))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SproutPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = SproutPrimaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = event.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$timeStr ${event.location?.let { "• $it" } ?: ""}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Google Calendar Sync Prompt Card matching Screenshot 2
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1B1925))
                    .border(1.dp, Color(0xFF2B283A), RoundedCornerShape(22.dp))
                    .clickable {
                        if (!isCalendarPermissionGranted) {
                            onRequestCalendarPermission()
                        } else {
                            onSyncWithCalendar()
                        }
                    }
                    .padding(16.dp)
                    .testTag("btn_sync_google_calendar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SproutPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCalendarPermissionGranted) Icons.Default.Sync else Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isCalendarPermissionGranted) "Google Calendar synced" else "Show my calendar events",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isCalendarPermissionGranted) "Tap to push all habits as reminders to your device Calendar" else "Read-only, from your phone. Nothing leaves your device.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
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
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    isStepOrWater: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Label on left
        Text(
            text = timeLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .width(44.dp)
                .padding(top = 10.dp)
        )

        // Connected timeline vertical axis
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            val iconVector = when (iconName) {
                "sun" -> Icons.Default.WbSunny
                "walk" -> Icons.Default.DirectionsWalk
                "water" -> Icons.Default.WaterDrop
                "moon" -> Icons.Default.NightsStay
                else -> Icons.Default.SelfImprovement
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262335)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isCompleted) SproutAccentAmber else Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(54.dp)
                        .background(Color(0xFF4C1D95).copy(alpha = 0.6f))
                )
            }
        }

        // Habit details and action
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                if (isCompleted) {
                    Text(
                        text = "🏆 You owned that time.",
                        fontSize = 11.sp,
                        color = SproutAccentAmber,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Action button: (+) or Checkbox
            if (isStepOrWater && !isCompleted) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF221E33))
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increment",
                        tint = SproutAccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) SproutAccentAmber else Color.Transparent)
                        .border(
                            2.dp,
                            if (isCompleted) SproutAccentAmber else Color(0xFF4B5563),
                            CircleShape
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SproutPrimaryLight,
                modifier = Modifier.width(44.dp)
            )

            // Small purple circle on timeline
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SproutPrimaryLight)
            )

            // "Now" label & horizontal line
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SproutPrimaryLight
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(SproutPrimaryLight)
                )
            }
        }

        // Countdown and Add Task button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 54.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF221E33))
                    .clickable { onAddTask() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("btn_add_task_inline"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = SproutPrimaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Add task",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SproutPrimaryLight
                    )
                }
            }
        }
    }
}
