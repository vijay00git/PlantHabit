package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.isScheduledOn
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val heatFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/**
 * "Monthly heat maps": one compact month grid per habit, shaded by how much of the target
 * was done each day. Shared month selector; tap a habit to open its details.
 */
@Composable
fun HabitHeatmaps(
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onOpenHabit: (HabitEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    var monthOffset by remember { mutableIntStateOf(0) }
    val logsByHabit = remember(logs) { logs.groupBy { it.habitId }.mapValues { e -> e.value.associateBy { it.date } } }
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 3 else 2

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Month selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(background = c.lavenderSoft, size = 40.dp) {
                Text("🗓️", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Monthly heat maps", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                val monthName = remember(monthOffset) {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, monthOffset) }.time
                    )
                }
                Text(monthName, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            NavButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", enabled = true) { monthOffset-- }
            Spacer(Modifier.width(6.dp))
            NavButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month", enabled = monthOffset < 0) {
                if (monthOffset < 0) monthOffset++
            }
        }

        if (habits.isEmpty()) {
            Text(
                "Add a habit to see its month at a glance.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted
            )
        }

        AnimatedContent(
            targetState = monthOffset,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(260)) { dir * it / 5 } + fadeIn(tween(200)))
                    .togetherWith(slideOutHorizontally(tween(260)) { -dir * it / 5 } + fadeOut(tween(150)))
            },
            label = "heatmapMonth"
        ) { offset ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habits.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { habit ->
                            HabitMonthCard(
                                habit = habit,
                                logsByDate = logsByHabit[habit.id].orEmpty(),
                                monthOffset = offset,
                                onClick = { onOpenHabit(habit) },
                                modifier = Modifier.weight(1f).testTag("heatmap_${habit.id}")
                            )
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Legend
        val accent = AccentPair(c.primary, c.primarySoft)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Less", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { level ->
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(heatColor(level, accent, c.cardMuted)))
            }
            Text("More", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
    }
}

/** 0 = nothing logged → track colour; 1 = target reached → full accent. */
private fun heatColor(level: Float, accent: AccentPair, empty: Color): Color = when {
    level <= 0f -> empty
    level >= 1f -> accent.color
    else -> accent.color.copy(alpha = 0.25f + 0.55f * level)
}

@Composable
private fun HabitMonthCard(
    habit: HabitEntity,
    logsByDate: Map<String, HabitLogEntity>,
    monthOffset: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val icon = AppIcon.of(habit.iconName)
    val accent = c.iconAccent(icon)
    val today = remember { heatFmt.format(Date()) }
    val first = remember(monthOffset) {
        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, monthOffset) }
    }
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-first

    // Per-day level for this month
    val levels = remember(logsByDate, monthOffset, habit) {
        (1..daysInMonth).map { day ->
            val cal = (first.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            val date = heatFmt.format(cal.time)
            val log = logsByDate[date]
            val level = when {
                log == null -> 0f
                log.isCompleted -> 1f
                else -> (log.progressValue.toFloat() / habit.targetValue.coerceAtLeast(1)).coerceIn(0f, 0.95f)
            }
            Triple(date, level, habit.isScheduledOn(cal.get(Calendar.DAY_OF_WEEK)))
        }
    }
    val doneDays = levels.count { it.second >= 1f }
    val scheduledSoFar = levels.count { it.third && it.first <= today }

    Column(
        modifier = modifier
            .bounceClick(pressedScale = 0.97f, onClick = onClick)
            .clip(AppShapes.medium)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(background = accent.soft, size = 28.dp) {
                Icon(icon.vector, contentDescription = null, tint = accent.color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                habit.title,
                style = MaterialTheme.typography.titleSmall,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$doneDays/${scheduledSoFar.coerceAtLeast(doneDays)}",
                style = MaterialTheme.typography.labelMedium,
                color = accent.color
            )
        }
        // Month grid, Monday-first
        val cells: List<Triple<String, Float, Boolean>?> = List(lead) { null } + levels
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            cells.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        cell == null -> Color.Transparent
                                        cell.first > today -> c.cardMuted.copy(alpha = 0.4f)
                                        !cell.third && cell.second == 0f -> c.cardMuted.copy(alpha = 0.5f)
                                        else -> heatColor(cell.second, accent, c.cardMuted)
                                    }
                                )
                                .then(
                                    if (cell?.first == today) Modifier.border(1.5.dp, c.textPrimary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    else Modifier
                                )
                        )
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val c = PlantTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (enabled) c.cardMuted else Color.Transparent)
            .bounceClick(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) c.textPrimary else c.textMuted, modifier = Modifier.size(20.dp))
    }
}
