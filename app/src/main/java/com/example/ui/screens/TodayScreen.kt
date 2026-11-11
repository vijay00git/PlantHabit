package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitWithStatus
import com.example.data.model.MoodLogEntity
import com.example.ui.components.GoalCounterCard
import com.example.ui.components.HabitCard
import com.example.ui.components.HorizontalDateStrip
import com.example.ui.components.MoodTrackerCard
import com.example.ui.components.PlantStreakCard
import com.example.ui.components.RoutineCard
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutBackgroundDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    habitsWithStatus: List<HabitWithStatus>,
    onToggleHabit: (HabitWithStatus) -> Unit,
    onIncrementHabit: (HabitWithStatus, Int) -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalendarSync: () -> Unit,
    onOpenFocusMode: () -> Unit,
    activeStreakDays: Int,
    currentMood: MoodLogEntity? = null,
    onSelectMood: (Int, String) -> Unit = { _, _ -> },
    goalChallenges: List<ChallengeEntity> = emptyList(),
    onIncrementGoal: (String, Float) -> Unit = { _, _ -> },
    onOpenChallengeDetails: (ChallengeEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning."
            hour < 17 -> "Good afternoon."
            else -> "Good evening."
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SproutBackgroundDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayDateHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    // Action Icons Row
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
                        HeaderIconButton(
                            icon = Icons.Default.BarChart,
                            description = "Visual Statistics",
                            onClick = onOpenAnalytics,
                            testTag = "btn_statistics"
                        )
                        HeaderIconButton(
                            icon = Icons.Default.CalendarMonth,
                            description = "Calendar Sync",
                            onClick = onOpenCalendarSync,
                            testTag = "btn_calendar_sync"
                        )
                        HeaderIconButton(
                            icon = Icons.Default.Settings,
                            description = "Settings",
                            onClick = onOpenSettings,
                            testTag = "btn_settings"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = greeting,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Horizontal Date Strip
        item {
            HorizontalDateStrip(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }

        // Plant Streak Card
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                PlantStreakCard(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    activeStreakDays = activeStreakDays
                )
            }
        }

        // Active Goal Counter Cards (Screenshot 1: 100 km, 1,000 pages, savings goal)
        items(goalChallenges.filter { it.isJoined && it.goalTarget > 0 }, key = { it.id }) { goalChallenge ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                GoalCounterCard(
                    goalChallenge = goalChallenge,
                    onIncrement = { delta -> onIncrementGoal(goalChallenge.id, delta) },
                    onClickDetails = { onOpenChallengeDetails(goalChallenge) }
                )
            }
        }

        // Routine Card (Happy Mornings)
        if (routineHabits.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    RoutineCard(
                        title = "Happy Mornings",
                        dayProgressText = "Day 1 of 7",
                        routineHabits = routineHabits,
                        onToggleHabit = onToggleHabit
                    )
                }
            }
        }

        // Daily Mood Tracker Card (Screenshot 3: 😄 Great, 🙂 Good, 😐 Okay, 🙁 Meh, 😣 Bad)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                MoodTrackerCard(
                    currentMood = currentMood,
                    onSelectMood = onSelectMood
                )
            }
        }

        // Daily Momentum & Focus Card (100% Free)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1A2E),
                                Color(0xFF261938)
                            )
                        )
                    )
                    .clickable { onOpenFocusMode() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SproutPrimary.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Momentum",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "DAILY HABIT MOMENTUM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC084FC),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Focus & Flow State",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tap to launch distraction-free focus timer",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262335)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Filter Pills (ALL, MORNING, AFTERNOON, EVENING)
        item {
            val filters = listOf("ALL", "MORNING", "AFTERNOON", "EVENING")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = currentFilter == filter
                    val pillColor = if (isSelected) Color(0xFFF1F5F9) else Color(0xFF1E1C28)
                    val textColor = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                    val icon = when (filter) {
                        "MORNING" -> Icons.Default.WbSunny
                        "AFTERNOON" -> Icons.Default.Lightbulb
                        "EVENING" -> Icons.Default.NightsStay
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillColor)
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_pill_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Section Title: TODAY'S HABITS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TODAY'S HABITS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${filteredHabits.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Habits List
        items(filteredHabits, key = { it.habit.id }) { habitWithStatus ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                HabitCard(
                    habitWithStatus = habitWithStatus,
                    onToggleCompleted = { onToggleHabit(habitWithStatus) },
                    onIncrement = { amount -> onIncrementHabit(habitWithStatus, amount) }
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF211E2E))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
    }
}
