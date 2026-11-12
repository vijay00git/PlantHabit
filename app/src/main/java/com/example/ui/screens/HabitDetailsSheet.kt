package com.example.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Archive
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.isScheduledOn
import com.example.data.model.scheduleLabel
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.AppIcon
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.bounceClick
import com.example.ui.components.iconAccent
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/**
 * A habit's details: streaks, totals and a month calendar of its history. Tapping a past
 * (or today's) scheduled day toggles that day's check-in, so missed days can be filled in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsSheet(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    currentStreak: Int,
    bestStreak: Int,
    onToggleDate: (date: String) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    onArchive: () -> Unit = {},
    onSaveNote: (date: String, note: String) -> Unit = { _, _ -> },
    onToggleSkip: (date: String) -> Unit = {},
    freezesLeft: Int = 2,
) {
    var noteDate by remember { mutableStateOf<String?>(null) }
    noteDate?.let { d ->
        NoteDialog(
            date = d,
            initial = logs.firstOrNull { it.date == d }?.notes.orEmpty(),
            onSave = { text -> onSaveNote(d, text); noteDate = null },
            onDismiss = { noteDate = null }
        )
    }
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val icon = AppIcon.of(habit.iconName)
    val accent = c.iconAccent(icon)
    val logsByDate = remember(logs) { logs.associateBy { it.date } }
    val today = remember { dayFmt.format(Date()) }

    // Completion rate over the last 30 scheduled days (since the habit was created)
    val rate = remember(logs, habit) {
        // Count from creation, or from the earliest day the user filled in
        val created = (listOf(dayFmt.format(Date(habit.createdAt))) + logs.map { it.date }).min()
        val cal = Calendar.getInstance()
        var scheduled = 0
        var done = 0
        repeat(30) {
            val d = dayFmt.format(cal.time)
            if (d >= created && habit.isScheduledOn(cal.get(Calendar.DAY_OF_WEEK))) {
                scheduled++
                if (logsByDate[d]?.isCompleted == true) done++
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        if (scheduled == 0) 0 else done * 100 / scheduled
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        contentColor = c.textPrimary,
        scrimColor = c.scrim,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(AppShapes.pill)
                    .background(c.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(background = accent.soft, size = 52.dp) {
                    Icon(icon.vector, contentDescription = null, tint = accent.color, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.title, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                    Text(
                        "${habit.scheduleLabel()} · ${habit.scheduledTime}" +
                            if (habit.targetValue > 1) " · %,d %s".format(habit.targetValue, habit.unit) else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary
                    )
                }
                HeaderButton(Icons.Default.Archive, "Archive habit", c.sunSoft, c.sun, Modifier.testTag("btn_archive_habit")) {
                    onArchive()
                }
                Spacer(Modifier.width(8.dp))
                HeaderButton(Icons.Default.Edit, "Edit habit", c.primarySoft, c.primary, Modifier.testTag("btn_edit_habit")) {
                    onEdit()
                }
                Spacer(Modifier.width(8.dp))
                HeaderButton(Icons.Default.Close, "Close", c.cardMuted, c.textPrimary) { onDismiss() }
            }

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("🔥", currentStreak, "", "Current streak", AccentPair(c.sunrise, c.sunriseSoft), Modifier.weight(1f))
                StatTile("🏆", bestStreak, "", "Best streak", AccentPair(c.sun, c.sunSoft), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("✅", logs.count { it.isCompleted }, "", "Times done", AccentPair(c.primary, c.primarySoft), Modifier.weight(1f))
                StatTile("📈", rate, "%", "Last 30 days", AccentPair(c.sky, c.skySoft), Modifier.weight(1f))
            }

            // Today: note + streak freeze
            val todayLog = logs.firstOrNull { it.date == today }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPill(
                    text = if (todayLog?.notes.isNullOrBlank()) "📝 Add note" else "📝 Edit today's note",
                    bg = c.lavenderSoft, fg = c.lavender,
                    modifier = Modifier.weight(1f).testTag("btn_add_note")
                ) { noteDate = today }
                val skipped = todayLog?.isSkipped == true
                val canSkip = todayLog?.isCompleted != true && (skipped || freezesLeft > 0)
                ActionPill(
                    text = if (skipped) "❄️ Undo skip" else "❄️ Skip today",
                    bg = if (canSkip) c.skySoft else c.cardMuted, fg = if (canSkip) c.sky else c.textMuted,
                    modifier = Modifier.weight(1f).testTag("btn_skip_today"),
                    enabled = canSkip
                ) { onToggleSkip(today) }
            }
            Text(
                "❄️ $freezesLeft of 2 streak freezes left this month · a skipped day keeps your streak safe",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted
            )

            // History calendar
            PlantCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                var monthOffset by remember { mutableIntStateOf(0) }
                val monthCal = remember(monthOffset) {
                    Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        add(Calendar.MONTH, monthOffset)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", c.cardMuted, c.textPrimary) {
                        monthOffset--
                    }
                    Text(
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    HeaderButton(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month",
                        if (monthOffset < 0) c.cardMuted else Color.Transparent,
                        if (monthOffset < 0) c.textPrimary else c.textMuted
                    ) { if (monthOffset < 0) monthOffset++ }
                }
                Spacer(Modifier.height(12.dp))

                AnimatedContent(
                    targetState = monthOffset,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(tween(250)) { dir * it / 4 } + fadeIn(tween(200)))
                            .togetherWith(slideOutHorizontally(tween(250)) { -dir * it / 4 } + fadeOut(tween(150)))
                    },
                    label = "month"
                ) { offset ->
                    MonthGrid(
                        offset = offset,
                        habit = habit,
                        logsByDate = logsByDate,
                        today = today,
                        accent = accent,
                        onLongPress = { date -> noteDate = date },
                        onToggle = { date ->
                            haptics.tick()
                            onToggleDate(date)
                        }
                    )
                }

                Spacer(Modifier.height(10.dp))
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Legend(accent.color, "Done")
                    Legend(accent.soft, "Partly")
                    Legend(c.berrySoft, "Missed")
                    Legend(c.skySoft, "❄️ Skipped")
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap a day to fill in or undo a check-in · long-press to add a note.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted
                )
            }

            // Recent notes
            val notes = logs.filter { !it.notes.isNullOrBlank() }.sortedByDescending { it.date }.take(6)
            if (notes.isNotEmpty()) {
                Text("NOTES", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                notes.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.medium)
                            .background(c.card)
                            .bounceClick(pressedScale = 0.98f) { noteDate = log.date }
                            .padding(12.dp)
                    ) {
                        Text(
                            prettyDate(log.date),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.lavender,
                            modifier = Modifier.width(72.dp)
                        )
                        Text(log.notes.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    offset: Int,
    habit: HabitEntity,
    logsByDate: Map<String, HabitLogEntity>,
    today: String,
    accent: AccentPair,
    onToggle: (String) -> Unit,
    onLongPress: (String) -> Unit = {},
) {
    val c = PlantTheme.colors
    val created = (listOf(dayFmt.format(Date(habit.createdAt))) + logsByDate.filterValues { it.isCompleted }.keys).min()
    val first = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, offset)
    }
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Monday-first grid: blank cells before the 1st
    val lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val cells: List<Int?> = List(lead) { null } + (1..daysInMonth).toList()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (day != null) {
                            val cal = (first.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                            val date = dayFmt.format(cal.time)
                            val scheduled = habit.isScheduledOn(cal.get(Calendar.DAY_OF_WEEK))
                            val log = logsByDate[date]
                            val done = log?.isCompleted == true
                            val partial = !done && (log?.progressValue ?: 0) > 0
                            val future = date > today
                            val beforeHabit = date < created
                            val skipped = !done && log?.isSkipped == true
                            val missed = scheduled && !done && !partial && !skipped && !future && !beforeHabit && date != today
                            val bg by animateColorAsState(
                                when {
                                    done -> accent.color
                                    skipped -> c.skySoft
                                    partial -> accent.soft
                                    missed -> c.berrySoft
                                    scheduled && !future -> c.cardMuted
                                    else -> Color.Transparent
                                },
                                tween(250),
                                label = "dayCell"
                            )
                            val fg = when {
                                done -> c.onAccent
                                future || beforeHabit || !scheduled -> c.textMuted
                                missed -> c.berry
                                else -> c.textPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSizeCompat()
                                    .clip(CircleShape)
                                    .background(bg)
                                    .then(if (date == today) Modifier.border(2.dp, c.primary, CircleShape) else Modifier)
                                    .bounceClick(
                                        enabled = !future, pressedScale = 0.85f, haptic = false,
                                        onLongClick = { onLongPress(date) }
                                    ) { onToggle(date) }
                                    .testTag("history_day_$date"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (skipped) "❄️" else "$day", style = MaterialTheme.typography.labelMedium, color = fg)
                                if (!log?.notes.isNullOrBlank()) {
                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 4.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (done) c.onAccent else c.lavender)
                                    )
                                }
                            }
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun Modifier.matchParentSizeCompat(): Modifier = this.then(Modifier.fillMaxWidth().aspectRatio(1f))

@Composable
private fun StatTile(emoji: String, value: Int, suffix: String, label: String, accent: AccentPair, modifier: Modifier) {
    val c = PlantTheme.colors
    Row(
        modifier = modifier
            .clip(AppShapes.medium)
            .background(accent.soft)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(10.dp))
        Column {
            AnimatedCounter(value = value, style = MaterialTheme.typography.titleLarge, color = accent.color, suffix = suffix)
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = PlantTheme.colors.textSecondary)
    }
}

@Composable
private fun HeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    bg: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(bg)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}


private fun prettyDate(date: String): String = try {
    SimpleDateFormat("d MMM", Locale.getDefault()).format(dayFmt.parse(date)!!)
} catch (e: Exception) { date }

@Composable
private fun ActionPill(
    text: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(bg)
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
private fun NoteDialog(date: String, initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val c = PlantTheme.colors
    var text by remember { mutableStateOf(initial) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = AppShapes.xl,
        title = { Text("Note · ${prettyDate(date)}", style = MaterialTheme.typography.titleLarge, color = c.textPrimary) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(300) },
                placeholder = { Text("How did it go?") },
                minLines = 3,
                shape = AppShapes.medium,
                modifier = Modifier.fillMaxWidth().testTag("input_note")
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onSave(text) }, modifier = Modifier.testTag("btn_save_note")) {
                Text("Save", style = MaterialTheme.typography.labelLarge, color = c.primary)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
    )
}
