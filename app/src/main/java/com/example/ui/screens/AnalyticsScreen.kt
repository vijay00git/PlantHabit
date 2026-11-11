package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.AnalyticsData
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutBackgroundDark
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

@Composable
fun AnalyticsScreen(
    analyticsData: AnalyticsData?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val data = analyticsData ?: AnalyticsData(
        totalHabits = 6,
        overallCompletionRate = 78,
        currentPlantStage = "Blooming Sprout",
        activeStreakDays = 7,
        longestStreakDays = 12,
        weeklyStats = emptyList(),
        categoryStats = mapOf("Health" to 12, "Mindfulness" to 8, "Fitness" to 6),
        timeOfDayStats = mapOf("Morning" to 14, "Afternoon" to 4, "Evening" to 8),
        totalHabitsChecked = 666,
        totalTimeInvestedMinutes = 2262,
        moodGoodPercentage = 85,
        moodTotalDays = 7
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SproutBackgroundDark),
        contentPadding = PaddingValues(bottom = 60.dp)
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
                Column {
                    Text(
                        text = "Visual Statistics",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Consistency & Deep Habit Analytics",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF221E33))
                        .testTag("btn_close_analytics")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Hero Plant & Streak Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1B2E),
                                Color(0xFF2D1B4E)
                            )
                        )
                    )
                    .border(1.dp, SproutPrimaryLight.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(SproutPrimary.copy(alpha = 0.3f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GARDEN STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutPrimaryLight,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = data.currentPlantStage,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = SproutAccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${data.activeStreakDays} Days Active Streak",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SproutAccentAmber
                            )
                        }

                        Text(
                            text = "Best Streak: ${data.longestStreakDays} days • ${data.overallCompletionRate}% completion rate",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.sprout_launcher_icon),
                        contentDescription = "Sprout plant",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }
        }

        // Time Invested Breakdown (Screenshot 6: "37 h 42 min" category breakdown)
        item {
            val totalHours = data.totalTimeInvestedMinutes / 60
            val totalMinutes = data.totalTimeInvestedMinutes % 60
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Time Invested",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${totalHours} h ${totalMinutes} min",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutPrimaryLight
                        )
                    }

                    Text(
                        text = "12-Week Time Investment in Self-Growth",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // Breakdown bars
                    data.timeInvestedBreakdown.forEach { (habitTitle, minutes) ->
                        val itemHours = minutes / 60
                        val itemMins = minutes % 60
                        val ratio = (minutes.toFloat() / data.totalTimeInvestedMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = habitTitle,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${itemHours} h ${itemMins} min",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SproutPrimary,
                                trackColor = Color(0xFF2E2A40)
                            )
                        }
                    }
                }
            }
        }

        // Mood Balance & Trends (Screenshot 3: "85% Good or great" Ring Chart)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(80.dp),
                            color = Color(0xFF2D253E),
                            strokeWidth = 8.dp
                        )
                        CircularProgressIndicator(
                            progress = { data.moodGoodPercentage / 100f },
                            modifier = Modifier.size(80.dp),
                            color = SproutAccentGreenGlow,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${data.moodGoodPercentage}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mood,
                                contentDescription = null,
                                tint = SproutAccentGreenGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Mood Balance",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${data.moodGoodPercentage}% Good or great",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SproutAccentGreenGlow
                        )

                        Text(
                            text = "Based on daily check-ins over the past ${data.moodTotalDays} days",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Weekly Completion Chart
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "7-Day Velocity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Target: 100%",
                            fontSize = 12.sp,
                            color = SproutAccentGreenGlow,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = if (data.weeklyStats.isNotEmpty()) data.weeklyStats else listOf(
                            com.example.data.repository.WeeklyStat("Sun", "1", 0.7f, 4, 6),
                            com.example.data.repository.WeeklyStat("Mon", "2", 0.85f, 5, 6),
                            com.example.data.repository.WeeklyStat("Tue", "3", 1.0f, 6, 6),
                            com.example.data.repository.WeeklyStat("Wed", "4", 0.5f, 3, 6),
                            com.example.data.repository.WeeklyStat("Thu", "5", 0.85f, 5, 6),
                            com.example.data.repository.WeeklyStat("Fri", "6", 1.0f, 6, 6),
                            com.example.data.repository.WeeklyStat("Sat", "7", 0.65f, 4, 6)
                        )

                        days.forEach { stat ->
                            val heightFraction = stat.completionRate.coerceIn(0.1f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${(stat.completionRate * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = if (stat.completionRate >= 0.8f) SproutAccentGreenGlow else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height((100 * heightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            if (stat.completionRate >= 0.8f) {
                                                Brush.verticalGradient(listOf(SproutAccentGreenGlow, SproutAccentGreen))
                                            } else {
                                                Brush.verticalGradient(listOf(SproutPrimaryLight, SproutPrimary))
                                            }
                                        )
                                )

                                Text(
                                    text = stat.dayLabel,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Consistency by Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    val categories = listOf(
                        Triple("Health", 0.85f, SproutAccentAmber),
                        Triple("Mindfulness", 0.70f, SproutPrimaryLight),
                        Triple("Fitness", 0.90f, SproutAccentGreenGlow),
                        Triple("Productivity", 0.60f, Color(0xFF38BDF8))
                    )

                    categories.forEach { (category, ratio, color) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(ratio * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = color,
                                trackColor = Color(0xFF2E2A40)
                            )
                        }
                    }
                }
            }
        }

        // Habit Milestones
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SproutAccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Streak Milestones",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MilestoneBadge(title = "3 Days", subtitle = "Spark", isUnlocked = true)
                        MilestoneBadge(title = "7 Days", subtitle = "Sprout", isUnlocked = data.activeStreakDays >= 7)
                        MilestoneBadge(title = "14 Days", subtitle = "Bloom", isUnlocked = data.activeStreakDays >= 14)
                        MilestoneBadge(title = "30 Days", subtitle = "Tree", isUnlocked = data.activeStreakDays >= 30)
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneBadge(
    title: String,
    subtitle: String,
    isUnlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) SproutAccentAmber.copy(alpha = 0.2f) else Color(0xFF221E33))
                .border(
                    1.dp,
                    if (isUnlocked) SproutAccentAmber else Color(0xFF3B3550),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.Park else Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = if (isUnlocked) SproutAccentAmber else Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color.White else Color(0xFF94A3B8)
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = if (isUnlocked) SproutAccentAmber else Color(0xFF64748B)
        )
    }
}
