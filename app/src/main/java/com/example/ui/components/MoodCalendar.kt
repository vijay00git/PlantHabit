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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.MoodLogEntity
import com.example.ui.theme.AppColors
import com.example.ui.theme.PlantTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val moodFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** Emoji and colour per mood score (5 = Great … 1 = Bad), matching the check-in card. */
internal fun moodEmoji(score: Int) = when (score) { 5 -> "😄"; 4 -> "🙂"; 3 -> "😐"; 2 -> "🙁"; else -> "😣" }
internal fun moodLabel(score: Int) = when (score) { 5 -> "Great"; 4 -> "Good"; 3 -> "Okay"; 2 -> "Meh"; else -> "Bad" }
internal fun AppColors.moodColor(score: Int): Color = when (score) {
    5 -> primary; 4 -> sky; 3 -> sun; 2 -> sunrise; else -> berry
}

/** Month calendar of daily mood check-ins: each day shows its mood emoji on a tinted cell. */
@Composable
fun MoodCalendar(moods: List<MoodLogEntity>, modifier: Modifier = Modifier) {
    val c = PlantTheme.colors
    var monthOffset by remember { mutableIntStateOf(0) }
    val byDate = remember(moods) { moods.associateBy { it.date } }
    val today = remember { moodFmt.format(Date()) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(background = c.sunSoft, size = 40.dp) {
                Text("😊", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Mood calendar", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                val month = remember(monthOffset) {
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, monthOffset) }.time
                    )
                }
                Text(month, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month", true) { monthOffset-- }
            Spacer(Modifier.width(6.dp))
            MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month", monthOffset < 0) { if (monthOffset < 0) monthOffset++ }
        }

        AnimatedContent(
            targetState = monthOffset,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(260)) { dir * it / 5 } + fadeIn(tween(200)))
                    .togetherWith(slideOutHorizontally(tween(260)) { -dir * it / 5 } + fadeOut(tween(150)))
            },
            label = "moodMonth"
        ) { offset ->
            val first = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, offset) }
            val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
            val lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-first
            val cells: List<Int?> = List(lead) { null } + (1..daysInMonth).toList()
            val monthMoods = (1..daysInMonth).mapNotNull { d ->
                byDate[moodFmt.format((first.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }.time)]
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = c.textMuted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                cells.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    val date = moodFmt.format((first.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }.time)
                                    val mood = byDate[date]
                                    val future = date > today
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    mood != null -> c.moodColor(mood.moodScore).copy(alpha = 0.22f)
                                                    future -> Color.Transparent
                                                    else -> c.cardMuted
                                                }
                                            )
                                            .then(if (date == today) Modifier.border(2.dp, c.primary, RoundedCornerShape(10.dp)) else Modifier)
                                            .testTag("mood_day_$date"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (mood != null) {
                                            Text(moodEmoji(mood.moodScore), style = MaterialTheme.typography.titleLarge)
                                            Text(
                                                "$day",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = c.textSecondary,
                                                modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 2.dp)
                                            )
                                        } else {
                                            Text("$day", style = MaterialTheme.typography.labelMedium, color = if (future) c.textMuted.copy(alpha = 0.5f) else c.textMuted)
                                        }
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                // Month summary: how many days of each mood
                if (monthMoods.isNotEmpty()) {
                    Spacer(Modifier.size(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (5 downTo 1).forEach { score ->
                            val count = monthMoods.count { it.moodScore == score }
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(c.moodColor(score).copy(alpha = if (count > 0) 0.18f else 0.06f))
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(moodEmoji(score), style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(4.dp))
                                Text("$count", style = MaterialTheme.typography.labelLarge, color = c.textPrimary)
                            }
                        }
                    }
                } else {
                    Text(
                        "No check-ins this month. Log your mood on Today to fill this in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
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
