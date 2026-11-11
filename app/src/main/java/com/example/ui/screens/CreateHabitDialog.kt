package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        timeOfDay: String,
        scheduledTime: String,
        targetValue: Int,
        unit: String,
        reminderEnabled: Boolean,
        syncGoogleCalendar: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Health") }
    var timeOfDay by remember { mutableStateOf("MORNING") }
    var scheduledTime by remember { mutableStateOf("08:00") }
    var targetValueString by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("tasks") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var syncGoogleCalendar by remember { mutableStateOf(true) }

    val categories = listOf("Health", "Mindfulness", "Fitness", "Productivity")
    val timesOfDay = listOf("MORNING", "AFTERNOON", "EVENING")
    val units = listOf("tasks", "ml", "steps", "mins", "glasses")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF181622))
                .border(1.dp, SproutCardBorderDark, RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "New Habit",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262335))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("e.g., Drink 3000 Glasses of Water", color = Color(0xFF64748B)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_habit_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SproutPrimaryLight,
                        unfocusedBorderColor = Color(0xFF332D42)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Category Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val isSelected = category == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) SproutPrimary else Color(0xFF262335))
                                    .clickable { category = cat }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("chip_cat_$cat")
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Time of Day
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TIME OF DAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        timesOfDay.forEach { tod ->
                            val isSelected = timeOfDay == tod
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) SproutAccentAmber else Color(0xFF262335))
                                    .clickable { timeOfDay = tod }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tod,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.Black else Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Schedule Time & Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = scheduledTime,
                        onValueChange = { scheduledTime = it },
                        label = { Text("Time (HH:mm)", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SproutPrimaryLight,
                            unfocusedBorderColor = Color(0xFF332D42)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = targetValueString,
                        onValueChange = { targetValueString = it },
                        label = { Text("Target", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SproutPrimaryLight,
                            unfocusedBorderColor = Color(0xFF332D42)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Unit selection
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    units.forEach { u ->
                        val isSelected = unit == u
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF332D42) else Color(0xFF1E1C28))
                                .border(1.dp, if (isSelected) SproutPrimaryLight else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { unit = u }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(text = u, fontSize = 11.sp, color = if (isSelected) Color.White else Color(0xFF94A3B8))
                        }
                    }
                }

                // Notification Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = SproutAccentAmber)
                        Column {
                            Text("Daily Reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Flexible exact push alerts", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SproutAccentAmber, checkedTrackColor = SproutAccentAmber.copy(alpha = 0.5f))
                    )
                }

                // Google Calendar Sync Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = SproutPrimaryLight)
                        Column {
                            Text("Sync with Google Calendar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Across all devices", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Switch(
                        checked = syncGoogleCalendar,
                        onCheckedChange = { syncGoogleCalendar = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SproutPrimaryLight, checkedTrackColor = SproutPrimary.copy(alpha = 0.5f))
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        val target = targetValueString.toIntOrNull() ?: 1
                        if (title.isNotBlank()) {
                            onSave(
                                title.trim(),
                                category,
                                timeOfDay,
                                scheduledTime.ifEmpty { "08:00" },
                                target,
                                unit,
                                reminderEnabled,
                                syncGoogleCalendar
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_habit"),
                    colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Plant This Habit 🌱", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
