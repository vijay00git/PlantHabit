package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChallengeEntity
import com.example.data.model.ChallengeTaskItem
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

@Composable
fun ChallengeDetailsDialog(
    challenge: ChallengeEntity,
    onAccept: () -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    val tasks = challenge.getParsedTasks().ifEmpty {
        listOf(
            ChallengeTaskItem(
                title = "Daily ${challenge.title} Action",
                description = challenge.description,
                timeOfDay = "MORNING",
                scheduledTime = "08:00",
                targetValue = 1,
                unit = "tasks",
                iconName = "sparkle",
                reminderEnabled = true,
                reminderTime = "08:00",
                syncWithGoogleCalendar = true,
                tip = challenge.dailyTip.ifEmpty { "Consistency beats intensity every single time." }
            )
        )
    }

    val isGoalCounter = challenge.goalTarget > 0f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF151320))
                .border(1.dp, SproutCardBorderDark, RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row with Category & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SproutPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = challenge.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SproutPrimaryLight,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF262335))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${challenge.durationDays} Days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SproutAccentAmber
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
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

                // Title and Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = challenge.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = challenge.subtitle,
                        fontSize = 13.sp,
                        color = SproutAccentAmber,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = challenge.description,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Science & Daily Philosophy Tip
                if (challenge.dailyTip.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF261938),
                                        Color(0xFF1E1630)
                                    )
                                )
                            )
                            .border(1.dp, SproutPrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = SproutAccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "DAILY PHILOSOPHY & TIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SproutAccentAmber,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = challenge.dailyTip,
                                    fontSize = 12.sp,
                                    color = Color(0xFFF1F5F9),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Goal Counter Overview (if applicable)
                if (isGoalCounter) {
                    val progressRatio = (challenge.goalCurrent / challenge.goalTarget.coerceAtLeast(1f)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF201B2E))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "MILESTONE TRACKER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SproutAccentGreenGlow
                                )
                                Text(
                                    text = "%.0f %s of %.0f %s".format(
                                        challenge.goalCurrent,
                                        challenge.goalUnit,
                                        challenge.goalTarget,
                                        challenge.goalUnit
                                    ),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${((progressRatio) * 100).toInt()}% completed • On track",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Box(
                                modifier = Modifier.size(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.size(54.dp),
                                    color = Color(0xFF332D42),
                                    strokeWidth = 5.dp
                                )
                                CircularProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier.size(54.dp),
                                    color = SproutAccentGreenGlow,
                                    strokeWidth = 5.dp
                                )
                                Text(
                                    text = "${(progressRatio * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Section: TASKS, HABITS & REMINDERS INCLUDED
                Text(
                    text = "HABITS & REMINDERS INCLUDED (${tasks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.forEachIndexed { index, task ->
                        val iconVector = when (task.iconName) {
                            "sun" -> Icons.Default.WbSunny
                            "moon" -> Icons.Default.NightsStay
                            "walk" -> Icons.Default.DirectionsWalk
                            "water" -> Icons.Default.WaterDrop
                            "alarm" -> Icons.Default.Alarm
                            else -> Icons.Default.AutoAwesome
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1F1C2B))
                                .border(1.dp, Color(0xFF2E2A3E), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SproutPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = null,
                                        tint = SproutPrimaryLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2A263D))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${task.targetValue} ${task.unit}",
                                                fontSize = 11.sp,
                                                color = SproutAccentAmber,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }

                                    // Reminder & Calendar Badges
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (task.reminderEnabled) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Alarm,
                                                    contentDescription = null,
                                                    tint = SproutAccentAmber,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Alarm: ${task.reminderTime}",
                                                    fontSize = 11.sp,
                                                    color = SproutAccentAmber
                                                )
                                            }
                                        }

                                        if (task.syncWithGoogleCalendar) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = SproutPrimaryLight,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Google Calendar sync",
                                                    fontSize = 11.sp,
                                                    color = SproutPrimaryLight
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Decision Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!challenge.isJoined) {
                        Button(
                            onClick = {
                                onAccept()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_accept_challenge_popup"),
                            colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Accept Challenge & Add Habits 🌱",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_decline_challenge_popup"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D42))
                        ) {
                            Text(
                                text = "Decline / Maybe Later",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                onLeave()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_leave_challenge_popup"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Leave This Challenge",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D42))
                        ) {
                            Text(
                                text = "Close Details",
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }
    }
}
