package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HabitWithStatus
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark

@Composable
fun RoutineCard(
    title: String,
    dayProgressText: String,
    routineHabits: List<HabitWithStatus>,
    onToggleHabit: (HabitWithStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = routineHabits.count { it.isCompleted }
    val totalCount = routineHabits.size.coerceAtLeast(1)
    val progress = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SproutCardDark)
            .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("routine_card_happy_mornings")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header: Image + Title + Medal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.routine_morning),
                        contentDescription = "Routine banner",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$dayProgressText · $completedCount/$totalCount tasks done",
                            fontSize = 12.sp,
                            color = SproutAccentAmber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Medal with circular progress ring
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(38.dp),
                        color = Color(0xFF332D42),
                        strokeWidth = 3.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(38.dp),
                        color = SproutAccentAmber,
                        strokeWidth = 3.dp
                    )
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "Challenge badge",
                        tint = SproutAccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Routine tasks
            routineHabits.forEach { habitWithStatus ->
                val habit = habitWithStatus.habit
                val isDone = habitWithStatus.isCompleted

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleHabit(habitWithStatus) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isDone) SproutAccentAmber else Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isDone) SproutAccentAmber else Color(0xFF64748B),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color(0xFF1E1C28),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Habit Icon
                    Icon(
                        imageVector = if (habit.iconName == "alarm") Icons.Default.Alarm else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isDone) SproutAccentAmber else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )

                    // Title
                    Text(
                        text = habit.title,
                        fontSize = 14.sp,
                        fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                        color = if (isDone) Color(0xFF94A3B8) else Color(0xFFF1F5F9),
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }

            // Social motivation indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SproutAccentGreen)
                )
                Text(
                    text = "You and 81 others today",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
