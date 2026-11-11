package com.example.ui.screens

import com.example.ui.components.iconAccent
import com.example.ui.components.AppIcon
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.local.BannerStorage
import com.example.ui.components.ChallengeBannerImage
import com.example.ui.components.rememberBannerSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChallengeEntity
import com.example.data.model.ChallengeTaskItem
import com.example.ui.components.CelebrationBurst
import com.example.ui.components.IconBubble
import com.example.ui.components.bounceClick
import com.example.ui.components.ProgressRing
import com.example.ui.components.rememberHaptics
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors
import com.example.ui.theme.dreamGradient
import com.example.ui.theme.heroGradient
import com.example.ui.theme.sunriseGradient
import kotlinx.coroutines.delay

/** How long the accept celebration plays before the dialog closes. */
private const val ACCEPT_CELEBRATION_MS = 650L

@Composable
fun ChallengeDetailsDialog(
    challenge: ChallengeEntity,
    onAccept: () -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
    onPickBanner: (Uri) -> Unit = {},
    onRemoveBanner: () -> Unit = {},
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val accent = c.categoryColors(challenge.category)

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

    // Accept flow: fire the burst + haptic, let it play, then accept and close.
    var accepting by remember { mutableStateOf(false) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    val currentOnAccept by rememberUpdatedState(onAccept)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(accepting) {
        if (accepting) {
            delay(ACCEPT_CELEBRATION_MS)
            currentOnAccept()
            currentOnDismiss()
        }
    }

    Dialog(
        // Ignore outside-tap/back while the celebration plays so the accept isn't lost.
        onDismissRequest = { if (!accepting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(AppShapes.xl)
                .background(c.surface)
                .border(1.dp, c.border, AppShapes.xl)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DetailsHeader(
                    challenge = challenge,
                    accent = accent,
                    onPickBanner = onPickBanner,
                    onRemoveBanner = onRemoveBanner,
                    onClose = { if (!accepting) onDismiss() }
                )

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description
                    if (challenge.description.isNotBlank()) {
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary,
                            modifier = Modifier.staggeredEntrance(0)
                        )
                    }

                    // Science & Daily Philosophy Tip
                    if (challenge.dailyTip.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .staggeredEntrance(1)
                                .fillMaxWidth()
                                .clip(AppShapes.medium)
                                .background(c.sunSoft)
                                .border(1.dp, c.sun.copy(alpha = 0.3f), AppShapes.medium)
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconBubble(background = c.sun.copy(alpha = 0.2f), size = 34.dp) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = c.sun,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "DAILY PHILOSOPHY & TIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.sun
                                )
                                Text(
                                    text = challenge.dailyTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textPrimary
                                )
                            }
                        }
                    }

                    // Goal Counter Overview (if applicable)
                    if (isGoalCounter) {
                        val progressRatio = (challenge.goalCurrent / challenge.goalTarget.coerceAtLeast(1f)).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier
                                .staggeredEntrance(2)
                                .fillMaxWidth()
                                .clip(AppShapes.medium)
                                .background(c.cardMuted)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "MILESTONE TRACKER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.primary
                                )
                                Text(
                                    text = "%.0f %s of %.0f %s".format(
                                        challenge.goalCurrent,
                                        challenge.goalUnit,
                                        challenge.goalTarget,
                                        challenge.goalUnit
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.textPrimary
                                )
                                Text(
                                    text = "${(progressRatio * 100).toInt()}% completed • On track",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            ProgressRing(
                                progress = progressRatio,
                                size = 58.dp,
                                strokeWidth = 6.dp,
                                trackColor = c.border
                            ) {
                                Text(
                                    text = "${(progressRatio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.textPrimary
                                )
                            }
                        }
                    }

                    // Section: TASKS, HABITS & REMINDERS INCLUDED
                    Text(
                        text = "HABITS & REMINDERS INCLUDED (${tasks.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textSecondary,
                        modifier = Modifier.staggeredEntrance(3)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        tasks.forEachIndexed { index, task ->
                            TaskRow(
                                task = task,
                                fallbackAccent = accent,
                                modifier = Modifier.staggeredEntrance(4 + index)
                            )
                        }
                    }

                    // Action Decision Buttons
                    Column(
                        modifier = Modifier
                            .staggeredEntrance(4 + tasks.size)
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!challenge.isJoined) {
                            ChallengeGradientButton(
                                text = if (accepting) "Challenge accepted! 🎉" else "Accept Challenge & Add Habits 🌱",
                                icon = Icons.Default.Check,
                                haptic = false,
                                onClick = {
                                    if (!accepting) {
                                        haptics.success()
                                        burstTrigger++
                                        accepting = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_accept_challenge_popup")
                            )

                            ChallengeOutlinedButton(
                                text = "Decline / Maybe Later",
                                onClick = { if (!accepting) onDismiss() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_decline_challenge_popup"),
                                height = 46.dp
                            )
                        } else {
                            ChallengeGradientButton(
                                text = "Leave This Challenge",
                                icon = Icons.Default.Logout,
                                brush = SolidColor(c.berry),
                                contentColor = c.onAccent,
                                haptic = false,
                                onClick = {
                                    haptics.reject()
                                    onLeave()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_leave_challenge_popup"),
                                height = 48.dp
                            )

                            ChallengeOutlinedButton(
                                text = "Close Details",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth(),
                                height = 44.dp
                            )
                        }
                    }
                }
            }

            // Celebration overlay (doesn't intercept touches)
            CelebrationBurst(
                trigger = burstTrigger,
                modifier = Modifier.matchParentSize(),
                particleCount = 36
            )
        }
    }
}

@Composable
private fun DetailsHeader(
    challenge: ChallengeEntity,
    accent: AccentPair,
    onPickBanner: (Uri) -> Unit,
    onRemoveBanner: () -> Unit,
    onClose: () -> Unit,
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val photo = rememberBannerSource(challenge)
    val isCustomPhoto = BannerStorage.isCustom(challenge.bannerResName) && photo != null
    // System photo picker: no storage permission needed.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            haptics.success()
            onPickBanner(uri)
        }
    }
    val fallbackBrush: Brush = when (challenge.category.trim().lowercase()) {
        "fitness", "exercise", "sport", "movement", "routine" -> c.sunriseGradient
        "mindfulness", "mind", "meditation", "mental" -> c.dreamGradient
        else -> c.heroGradient
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        if (photo != null) {
            ChallengeBannerImage(
                source = photo,
                contentDescription = "${challenge.title} banner",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fallbackBrush)
            )
            // Large faded category glyph for texture
            Icon(
                imageVector = challengeIcon(challenge),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 24.dp, y = (-12).dp)
                    .size(150.dp)
            )
        }

        // Scrim so the white title stays legible on any photo/gradient.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        // Banner photo actions
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BannerActionPill(
                icon = Icons.Default.AddPhotoAlternate,
                text = if (photo == null) "Add banner photo" else "Change photo",
                onClick = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.testTag("btn_pick_banner")
            )
            if (isCustomPhoto) {
                BannerActionPill(
                    icon = Icons.Default.Delete,
                    text = "Remove",
                    onClick = {
                        haptics.reject()
                        onRemoveBanner()
                    },
                    modifier = Modifier.testTag("btn_remove_banner")
                )
            }
        }

        ChallengeCloseButton(
            onClick = onClose,
            onPhoto = true,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderChip(text = challenge.category.uppercase(), accent = accent, icon = challengeCategoryIcon(challenge.category))
                HeaderChip(text = "${challenge.durationDays} Days", accent = AccentPair(c.sun, c.sunSoft), icon = null)
                if (challenge.isJoined) {
                    HeaderChip(text = "Active", accent = AccentPair(c.primary, c.primarySoft), icon = Icons.Default.Check)
                }
            }
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (challenge.subtitle.isNotBlank()) {
                Text(
                    text = challenge.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Frosted pill button that reads well over any photo or gradient. */
@Composable
private fun BannerActionPill(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .bounceClick(onClick = onClick)
            .clip(AppShapes.pill)
            .background(Color.Black.copy(alpha = 0.38f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), AppShapes.pill)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun HeaderChip(text: String, accent: AccentPair, icon: ImageVector?) {
    Row(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(accent.soft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = accent.color, modifier = Modifier.size(12.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = accent.color)
    }
}

@Composable
private fun TaskRow(
    task: ChallengeTaskItem,
    fallbackAccent: AccentPair,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val taskIcon = AppIcon.of(task.iconName)
    val iconVector = taskIcon.vector
    val taskAccent = if (task.iconName.isBlank()) fallbackAccent else c.iconAccent(taskIcon)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.medium)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(background = taskAccent.soft, size = 42.dp) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = taskAccent.color,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.sunSoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${task.targetValue} ${task.unit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.sun
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
            }

            // Reminder & Calendar Badges
            if (task.reminderEnabled || task.syncWithGoogleCalendar) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.reminderEnabled) {
                        MetaBadge(icon = Icons.Default.Alarm, text = "Alarm: ${task.reminderTime}", color = c.sunrise)
                    }
                    if (task.syncWithGoogleCalendar) {
                        MetaBadge(icon = Icons.Default.CalendarMonth, text = "Google Calendar sync", color = c.sky)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaBadge(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
