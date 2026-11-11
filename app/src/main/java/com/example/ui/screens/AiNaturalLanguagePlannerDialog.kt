package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.IconBubble
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors
import com.example.ui.theme.dreamGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

private const val SAMPLE_PROMPT =
    "Dentist Tuesday at 3pm, finish my slides by Friday, gym after work 3 times a week, and I want to read every night"

/** A single chat entry: either something the user typed or the assistant's reply (with an optional plan). */
private data class PlannerMessage(
    val id: Int,
    val fromUser: Boolean,
    val text: String,
    val plan: List<PlannedItem> = emptyList()
)

private fun buildPlan(prompt: String): List<PlannedItem> {
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
    return list
}

@Composable
fun AiNaturalLanguagePlannerDialog(
    onDismiss: () -> Unit,
    onAddPlannedHabit: (title: String, category: String, scheduledTime: String, target: Int, unit: String) -> Unit
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var promptText by remember { mutableStateOf(SAMPLE_PROMPT) }
    var messages by remember {
        mutableStateOf(
            listOf(
                PlannerMessage(
                    id = 0,
                    fromUser = false,
                    text = "Hi! Tell me what your week looks like — appointments, goals, routines — and I'll turn it into habits & tasks. 🌱"
                )
            )
        )
    }
    var nextId by remember { mutableIntStateOf(1) }
    var isThinking by remember { mutableStateOf(false) }
    var addedIds by remember { mutableStateOf(setOf<String>()) }

    val canSend = promptText.isNotBlank() && !isThinking

    fun send() {
        val prompt = promptText.trim()
        if (prompt.isEmpty() || isThinking) return
        haptics.tick()
        messages = messages + PlannerMessage(id = nextId++, fromUser = true, text = prompt)
        promptText = ""
        isThinking = true
        scope.launch {
            delay(1100)
            val plan = buildPlan(prompt)
            messages = messages + PlannerMessage(
                id = nextId++,
                fromUser = false,
                text = "Here's what I planned — ${plan.size} ${if (plan.size == 1) "item" else "items"} ready to add:",
                plan = plan
            )
            isThinking = false
        }
    }

    // Keep the newest message in view.
    LaunchedEffect(messages.size, isThinking) {
        delay(80)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(AppShapes.xl)
                .background(c.surface)
                .border(1.dp, c.border, AppShapes.xl)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssistantAvatar(size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Natural Language Planner",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary
                    )
                    AnimatedContent(
                        targetState = isThinking,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                        label = "plannerStatus"
                    ) { thinking ->
                        Text(
                            text = if (thinking) "Planning…" else "Just type or speak. AI extracts tasks & habits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (thinking) c.lavender else c.textSecondary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.cardMuted)
                        .bounceClick(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = c.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

            // Conversation
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(c.background.copy(alpha = 0.5f))
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                messages.forEach { message ->
                    key(message.id) {
                        AnimatedEntrance {
                            if (message.fromUser) {
                                UserBubble(text = message.text)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AssistantBubble {
                                        Text(
                                            text = message.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textPrimary
                                        )
                                    }
                                    message.plan.forEachIndexed { index, item ->
                                        AnimatedEntrance(delayMillis = 120 + index * 110) {
                                            PlanCard(
                                                item = item,
                                                isAdded = addedIds.contains(item.id),
                                                onAdd = {
                                                    if (!addedIds.contains(item.id)) {
                                                        haptics.success()
                                                        addedIds = addedIds + item.id
                                                        onAddPlannedHabit(
                                                            item.title,
                                                            item.category,
                                                            item.scheduledTime,
                                                            item.targetValue,
                                                            item.unit
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isThinking) {
                    key("thinking") {
                        AnimatedEntrance {
                            AssistantBubble {
                                AiThinkingDots(
                                    color = c.lavender,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

            // Input bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp, max = 140.dp),
                        placeholder = { Text("What would you like to accomplish this week?") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = AppShapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textPrimary,
                            unfocusedTextColor = c.textPrimary,
                            focusedContainerColor = c.cardMuted,
                            unfocusedContainerColor = c.cardMuted,
                            focusedBorderColor = c.primary,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = c.primary,
                            focusedPlaceholderColor = c.textMuted,
                            unfocusedPlaceholderColor = c.textMuted,
                        )
                    )

                    // Voice prompt (fills in a sample request)
                    IconBubble(
                        background = c.lavenderSoft,
                        size = 48.dp,
                        modifier = Modifier.bounceClick {
                            promptText = SAMPLE_PROMPT
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice prompt",
                            tint = c.lavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Send / Plan it
                    val sendAlpha = if (canSend) 1f else 0.4f
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bounceClick(enabled = canSend, haptic = false) { send() }
                            .clip(CircleShape)
                            .background(c.dreamGradient, alpha = sendAlpha)
                            .testTag("btn_ai_plan_it"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Plan it",
                            tint = c.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "${promptText.length}/1000",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMuted,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/** Fades + slides its content up the first time it enters composition. */
@Composable
private fun AnimatedEntrance(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(320, delayMillis = delayMillis)) +
            slideInVertically(tween(360, delayMillis = delayMillis)) { it / 3 } +
            expandVertically(tween(300, delayMillis = delayMillis), expandFrom = Alignment.Top)
    ) {
        content()
    }
}

@Composable
private fun AssistantAvatar(size: androidx.compose.ui.unit.Dp) {
    val c = PlantTheme.colors
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(c.dreamGradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = c.onAccent,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun UserBubble(text: String) {
    val c = PlantTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                .background(c.primary)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = c.onPrimary)
        }
    }
}

@Composable
private fun AssistantBubble(content: @Composable () -> Unit) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistantAvatar(size = 28.dp)
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                .background(c.lavenderSoft)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun PlanCard(
    item: PlannedItem,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(item.category)
    val isHabit = item.type == "HABIT"
    val badgeBg = if (isHabit) c.primarySoft else c.lavenderSoft
    val badgeFg = if (isHabit) c.primary else c.lavender

    Column(
        modifier = Modifier
            .padding(start = 36.dp)
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.medium)
            .animateContentSize(tween(300))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isHabit) Icons.Default.Spa else Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = badgeFg,
                    modifier = Modifier.size(12.dp)
                )
                Text(text = item.type, style = MaterialTheme.typography.labelSmall, color = badgeFg)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Schedule, contentDescription = null, tint = c.sun, modifier = Modifier.size(13.dp))
            Text(text = item.scheduleSubtitle, style = MaterialTheme.typography.labelSmall, color = c.sun)
        }

        Text(text = item.title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)

        item.bullets.forEach { bullet ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(accent.color))
                Text(text = bullet, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isAdded,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = "Planted in your routine 🌱",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.primary
                )
            }
            Spacer(Modifier.weight(1f))

            val bg by animateColorAsState(
                targetValue = if (isAdded) c.primarySoft else c.primary,
                animationSpec = tween(300),
                label = "planAddBg"
            )
            val fg = if (isAdded) c.primary else c.onPrimary
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .bounceClick(enabled = !isAdded, haptic = false, onClick = onAdd)
                    .clip(AppShapes.pill)
                    .background(bg)
                    .animateContentSize(tween(250))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isAdded,
                    transitionSpec = {
                        (scaleIn(initialScale = 0.7f, animationSpec = tween(250)) + fadeIn(tween(250))) togetherWith
                            (scaleOut(targetScale = 0.7f, animationSpec = tween(180)) + fadeOut(tween(180)))
                    },
                    label = "planAddContent"
                ) { added ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (added) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (added) "Added" else "Add to Schedule",
                            style = MaterialTheme.typography.labelMedium,
                            color = fg
                        )
                    }
                }
            }
        }
    }
}
