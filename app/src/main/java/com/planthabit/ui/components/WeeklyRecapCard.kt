package com.planthabit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.planthabit.data.repository.WeeklyRecap
import com.planthabit.ui.theme.AppShapes
import com.planthabit.ui.theme.PlantTheme
import com.planthabit.ui.theme.dreamGradient

/** "This week" summary: completion, check-ins, star habit, mood and trend vs last week. */
@Composable
fun WeeklyRecapCard(recap: WeeklyRecap, modifier: Modifier = Modifier) {
    val c = PlantTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(c.dreamGradient)
            .padding(18.dp)
            .testTag("weekly_recap"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("THIS WEEK", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedCounter(
                value = recap.completionPercent,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                suffix = "%"
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("of planned habits done", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                val d = recap.deltaVsLastWeek
                Text(
                    when {
                        d > 0 -> "▲ $d% vs last week"
                        d < 0 -> "▼ ${-d}% vs last week"
                        else -> "Same as last week"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecapPill("✅ ${recap.checkIns} check-ins", Modifier.weight(1f))
            recap.averageMood?.let { avg ->
                val emoji = when { avg >= 4.5 -> "😄"; avg >= 3.5 -> "🙂"; avg >= 2.5 -> "😐"; avg >= 1.5 -> "🙁"; else -> "😣" }
                RecapPill("$emoji avg mood", Modifier.weight(1f))
            }
        }
        recap.bestHabit?.let {
            RecapPill("⭐ Star habit: $it (${recap.bestHabitCount}×)", Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RecapPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        maxLines = 1,
        modifier = modifier
            .clip(AppShapes.pill)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}
