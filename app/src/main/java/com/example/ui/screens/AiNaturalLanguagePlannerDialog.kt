package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitEntity
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

data class PlannedItem(
    val id: String,
    val type: String, // "HABIT" or "TASK"
    val title: String,
    val scheduleSubtitle: String,
    val bullets: List<String>,
    val scheduledTime: String,
    val category: String,
    val targetValue: Int,
    val unit: String
)

@Composable
fun AiNaturalLanguagePlannerDialog(
    onDismiss: () -> Unit,
    onAddPlannedHabit: (title: String, category: String, scheduledTime: String, target: Int, unit: String) -> Unit
) {
    var promptText by remember {
        mutableStateOf("Dentist Tuesday at 3pm, finish my slides by Friday, gym after work 3 times a week, and I want to read every night")
    }
    var plannedItems by remember { mutableStateOf<List<PlannedItem>>(emptyList()) }
    var addedIds by remember { mutableStateOf(setOf<String>()) }

    fun generatePlan(prompt: String) {
        val list = mutableListOf<PlannedItem>()
        val lower = prompt.lowercase()

        if (lower.contains("slide") || lower.contains("work")) {
            list.add(
                PlannedItem(
                    id = "task_slides",
                    type = "TASK",
                    title = "Work on presentation slides",
                    scheduleSubtitle = "Wednesday · 10:00 · 1 h 30 min",
                    bullets = listOf(
                        "Outline main points & key findings",
                        "Add visual charts & clean typography",
                        "Review and polish final deck"
                    ),
                    scheduledTime = "10:00",
                    category = "Productivity",
                    targetValue = 90,
                    unit = "mins"
                )
            )
        }

        if (lower.contains("gym") || lower.contains("workout") || lower.contains("fitness")) {
            list.add(
                PlannedItem(
                    id = "habit_gym",
                    type = "HABIT",
                    title = "Gym & Resistance Workout",
                    scheduleSubtitle = "3x / week · 18:00 · 1 h",
                    bullets = listOf(
                        "Warm-up and dynamic stretching",
                        "Strength training routine",
                        "Post-workout hydration"
                    ),
                    scheduledTime = "18:00",
                    category = "Fitness",
                    targetValue = 60,
                    unit = "mins"
                )
            )
        }

        if (lower.contains("read") || lower.contains("book")) {
            list.add(
                PlannedItem(
                    id = "habit_read",
                    type = "HABIT",
                    title = "Read before bed",
                    scheduleSubtitle = "Daily · 22:15 · 20 mins",
                    bullets = listOf(
                        "Read physical book or e-ink paper",
                        "Support natural melatonin release"
                    ),
                    scheduledTime = "22:15",
                    category = "Health",
                    targetValue = 20,
                    unit = "mins"
                )
            )
        }

        if (list.isEmpty()) {
            list.add(
                PlannedItem(
                    id = "habit_generic",
                    type = "HABIT",
                    title = "Priority Focus Block",
                    scheduleSubtitle = "Daily · 09:00 · 45 mins",
                    bullets = listOf("Execute primary high-impact objective"),
                    scheduledTime = "09:00",
                    category = "Productivity",
                    targetValue = 45,
                    unit = "mins"
                )
            )
        }

        plannedItems = list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF151320))
                .border(1.dp, SproutCardBorderDark, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Natural Language Planner",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Just type or speak. AI extracts tasks & habits.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF221E33))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Prompt Input Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1D1A2B))
                        .border(1.dp, Color(0xFF2E2840), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("What would you like to accomplish this week?", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${promptText.length}/1000",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2B263D))
                                        .clickable {
                                            promptText = "Dentist Tuesday at 3pm, finish my slides by Friday, gym after work 3 times a week, and I want to read every night"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice prompt",
                                        tint = SproutPrimaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Button(
                                    onClick = { generatePlan(promptText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("btn_ai_plan_it")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Plan it", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Generated Planned Items List
                if (plannedItems.isNotEmpty()) {
                    Text(
                        text = "READY TO ADD (${plannedItems.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    plannedItems.forEach { item ->
                        val isAdded = addedIds.contains(item.id)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E1B2C))
                                .border(1.dp, Color(0xFF2E2A3E), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (item.type == "HABIT") SproutAccentGreenGlow.copy(alpha = 0.2f)
                                                else SproutPrimary.copy(alpha = 0.25f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.type == "HABIT") SproutAccentGreenGlow else SproutPrimaryLight
                                        )
                                    }

                                    Text(
                                        text = item.scheduleSubtitle,
                                        fontSize = 11.sp,
                                        color = SproutAccentAmber
                                    )
                                }

                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                item.bullets.forEach { bullet ->
                                    Text(
                                        text = "• $bullet",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            if (!isAdded) {
                                                addedIds = addedIds + item.id
                                                onAddPlannedHabit(
                                                    item.title,
                                                    item.category,
                                                    item.scheduledTime,
                                                    item.targetValue,
                                                    item.unit
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAdded) Color(0xFF14532D) else SproutPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        if (isAdded) {
                                            Text("Added ✓", fontSize = 11.sp, color = SproutAccentGreenGlow)
                                        } else {
                                            Text("Add to Schedule", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
