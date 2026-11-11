package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChallengeEntity
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutBackgroundDark
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

@Composable
fun ChallengesScreen(
    challenges: List<ChallengeEntity>,
    onChallengeClick: (ChallengeEntity) -> Unit,
    onCreateCustomChallenge: () -> Unit,
    onImportJsonClick: () -> Unit,
    onDownloadTemplateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val joinedChallenges = challenges.filter { it.isJoined }
    val availableChallenges = challenges.filter { !it.isJoined }
    val gratitudeChallenge = challenges.find { it.id == "gratitude_month" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SproutBackgroundDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Title & JSON Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Challenges",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap any challenge to inspect habits & schedule",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // JSON Import / Template Action Buttons in Top Bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF262335))
                                .clickable { onDownloadTemplateClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_top_template"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = SproutAccentAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Template",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SproutAccentAmber
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SproutPrimary)
                                .clickable { onImportJsonClick() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("btn_top_import_json"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Import JSON",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Monthly Banner (November Challenge: Gratitude Month)
        item {
            gratitudeChallenge?.let { challenge ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { onChallengeClick(challenge) }
                        .testTag("banner_gratitude_month")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.challenge_gratitude),
                        contentDescription = "Gratitude Month Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFD97706).copy(alpha = 0.88f),
                                        Color(0xFFB45309).copy(alpha = 0.75f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FEATURED CHALLENGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = challenge.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "30 days · Tap to review habits & reminders",
                                fontSize = 12.sp,
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { onChallengeClick(challenge) }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                                .testTag("btn_inspect_gratitude"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (challenge.isJoined) "Active ✓" else "Review & Join",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // Section: YOUR ACTIVE CHALLENGES
        if (joinedChallenges.isNotEmpty()) {
            item {
                Text(
                    text = "YOUR ACTIVE CHALLENGES (${joinedChallenges.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            items(joinedChallenges, key = { it.id }) { challenge ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SproutCardDark)
                        .border(1.dp, SproutCardBorderDark, RoundedCornerShape(22.dp))
                        .clickable { onChallengeClick(challenge) }
                        .padding(18.dp)
                        .testTag("challenge_item_${challenge.id}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Text(
                                        text = challenge.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = challenge.subtitle,
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF14532D))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SproutAccentGreenGlow
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Details",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Segmented multi-day progress bars
                        val totalSegments = challenge.durationDays.coerceIn(1, 14)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 0 until totalSegments) {
                                val isCompletedDay = i < challenge.currentDay
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isCompletedDay) SproutAccentAmber else Color(0xFF332D42))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (challenge.goalTarget > 0) {
                                    "Goal: ${challenge.goalCurrent.toInt()}/${challenge.goalTarget.toInt()} ${challenge.goalUnit}"
                                } else {
                                    "Today · ${challenge.completedTasksToday}/${challenge.totalTasks} tasks done"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "Tap for roadmap & tips →",
                                fontSize = 11.sp,
                                color = SproutPrimaryLight
                            )
                        }
                    }
                }
            }
        }

        // Section: DISCOVER NEW CHALLENGES (100% Free, NO PRO / Payment locks)
        item {
            Text(
                text = "COMMUNITY CHALLENGES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 8.dp)
            )
        }

        // Milestone Goal Counter Challenge Card (Screenshot 1: Run 100 km / Savings)
        val runGoal = challenges.find { it.id == "goal_counter_run" }
        if (runGoal != null && !runGoal.isJoined) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2E1065), Color(0xFF1E1B4B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .clickable { onChallengeClick(runGoal) }
                        .padding(18.dp)
                        .testTag("challenge_goal_counter_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFA855F7).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "MILESTONE GOAL COUNTER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD8B4FE),
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = runGoal.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Count progress (+1, +5, +10) and stay on track with weekly pacing",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFA855F7).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        // Relationship Week Card (Unlocked and Free)
        val relationshipChallenge = challenges.find { it.id == "relationship_week" }
        if (relationshipChallenge != null && !relationshipChallenge.isJoined) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onChallengeClick(relationshipChallenge) }
                        .testTag("recommended_challenge_card")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.challenge_gratitude),
                        contentDescription = "Relationship Week",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Relationships",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SproutPrimary)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "View Challenge",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Column {
                            Text(
                                text = relationshipChallenge.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "7 days • Daily connection prompts and reminders",
                                fontSize = 12.sp,
                                color = Color(0xFFD4D4D8)
                            )
                        }
                    }
                }
            }
        }

        // Remaining unjoined challenges (Better Sleep, Happy Mornings, etc.)
        items(availableChallenges.filter { it.id != "gratitude_month" && it.id != "goal_counter_run" && it.id != "relationship_week" }, key = { it.id }) { challenge ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SproutCardDark)
                    .border(1.dp, SproutCardBorderDark, RoundedCornerShape(22.dp))
                    .clickable { onChallengeClick(challenge) }
                    .padding(18.dp)
                    .testTag("challenge_card_${challenge.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = challenge.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = challenge.subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${challenge.durationDays} days · ${challenge.totalTasks} scheduled habits",
                            fontSize = 11.sp,
                            color = SproutAccentAmber
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF262335))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Inspect →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SproutPrimaryLight
                        )
                    }
                }
            }
        }

        // Section: CREATE CUSTOM CHALLENGE VIA JSON
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF1A1826))
                    .border(1.dp, SproutPrimary.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SproutPrimaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Create Challenge via JSON",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Build custom multi-day challenges with habits, durations, and milestones. Download the template, edit it on your device, and import it right away.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDownloadTemplateClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262335)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download Template", fontSize = 12.sp, color = SproutAccentAmber)
                        }

                        Button(
                            onClick = onImportJsonClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Import JSON File", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
