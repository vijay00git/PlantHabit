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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutBackgroundDark
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

data class AiRecommendation(
    val id: String,
    val title: String,
    val rationale: String,
    val habitName: String,
    val timeOfDay: String,
    val scheduledTime: String,
    val category: String
)

@Composable
fun AiCoachScreen(
    onAdoptRecommendation: (AiRecommendation) -> Unit,
    onOpenAiPlanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
            .background(SproutBackgroundDark),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SproutPrimaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Sprout AI Coach",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Intelligent routine optimization and natural language planning",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Natural Language Routine Planner Card (Screenshot 2: "Just say it. AI plans it.")
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B1A54),
                                Color(0xFF1E1738)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .clickable { onOpenAiPlanner() }
                    .padding(20.dp)
                    .testTag("card_ai_natural_language_planner")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFC084FC).copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "JUST SAY IT. AI PLANS IT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF),
                                letterSpacing = 1.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = SproutAccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Type or speak your plans",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "e.g., \"Wake up at 6am, run 5km, read 20 pages, wind down by 10pm\". The AI turns your words into structured habits & reminders.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = onOpenAiPlanner,
                        colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = "Launch Natural Language Planner ✨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Hero Coaching Insight
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF28133C),
                                Color(0xFF1E1638)
                            )
                        )
                    )
                    .border(1.dp, SproutPrimaryLight.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SproutPrimary.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "TODAY'S AI INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SproutPrimaryLight,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Your morning routine is 85% consistent! To maintain your streak through the weekend, stack an evening hydration anchor.",
                        fontSize = 14.sp,
                        color = Color(0xFFF1F5F9),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "PERSONALIZED RECOMMENDATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )
        }

        // Recommendations List
        items(recommendations.size) { index ->
            val rec = recommendations[index]
            val isAdopted = adoptedIds.contains(rec.id)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(22.dp))
                    .padding(18.dp)
                    .testTag("ai_rec_${rec.id}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rec.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF262335))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = rec.scheduledTime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SproutAccentAmber
                            )
                        }
                    }

                    Text(
                        text = rec.rationale,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ ${rec.habitName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SproutAccentGreenGlow
                        )

                        Button(
                            onClick = {
                                if (!isAdopted) {
                                    adoptedIds = adoptedIds + rec.id
                                    onAdoptRecommendation(rec)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdopted) Color(0xFF14532D) else SproutPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isAdopted) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SproutAccentGreenGlow, modifier = Modifier.size(16.dp))
                                    Text("Added", fontSize = 12.sp, color = SproutAccentGreenGlow)
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Add to Schedule", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
