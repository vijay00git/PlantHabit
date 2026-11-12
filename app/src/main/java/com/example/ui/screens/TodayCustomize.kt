package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.IconBubble
import com.example.ui.components.bounceClick
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme

/** Cards that can appear below the habits on Today, in the user's order. */
enum class TodayCard(val id: String, val label: String, val emoji: String) {
    STREAK("streak", "Plant streak", "🌱"),
    GOALS("goals", "Goal counters", "🎯"),
    ROUTINES("routines", "Routines & challenges", "🗓️"),
    MOOD("mood", "Mood check-in", "😊"),
    FOCUS("focus", "Focus mode", "⏱️"),
}

data class TodayLayout(
    val order: List<TodayCard> = TodayCard.entries,
    val hidden: Set<TodayCard> = emptySet(),
) {
    val visible: List<TodayCard> get() = order.filter { it !in hidden }

    companion object {
        private const val PREFS = "today_layout"

        fun load(context: Context): TodayLayout {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val ids = p.getString("order", null)?.split(",").orEmpty()
            val known = ids.mapNotNull { id -> TodayCard.entries.firstOrNull { it.id == id } }
            // Keep any cards added in newer versions at the end
            val order = known + TodayCard.entries.filter { it !in known }
            val hidden = p.getStringSet("hidden", emptySet()).orEmpty()
                .mapNotNull { id -> TodayCard.entries.firstOrNull { it.id == id } }.toSet()
            return TodayLayout(order, hidden)
        }

        fun save(context: Context, layout: TodayLayout) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("order", layout.order.joinToString(",") { it.id })
                .putStringSet("hidden", layout.hidden.map { it.id }.toSet())
                .apply()
        }
    }
}

/** Reorder and show/hide the Today cards. Changes apply immediately. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeTodaySheet(
    layout: TodayLayout,
    onChange: (TodayLayout) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        scrimColor = c.scrim,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Customize Today", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Text(
                "Your habits always come first. Choose which cards follow them, and in what order.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
            Spacer(Modifier.size(6.dp))
            layout.order.forEachIndexed { index, card ->
                val shown = card !in layout.hidden
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.medium)
                        .background(c.card)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("today_card_${card.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(card.emoji, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        card.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (shown) c.textPrimary else c.textMuted,
                        modifier = Modifier.weight(1f)
                    )
                    MoveButton(Icons.Default.KeyboardArrowUp, "Move up", enabled = index > 0) {
                        onChange(layout.copy(order = layout.order.swap(index, index - 1)))
                    }
                    MoveButton(Icons.Default.KeyboardArrowDown, "Move down", enabled = index < layout.order.lastIndex) {
                        onChange(layout.copy(order = layout.order.swap(index, index + 1)))
                    }
                    Spacer(Modifier.width(6.dp))
                    Switch(
                        checked = shown,
                        onCheckedChange = { on ->
                            onChange(layout.copy(hidden = if (on) layout.hidden - card else layout.hidden + card))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = c.primary, checkedThumbColor = c.onPrimary)
                    )
                }
            }
        }
    }
}

internal fun <T> List<T>.swap(i: Int, j: Int): List<T> =
    if (i !in indices || j !in indices) this else toMutableList().also { val t = it[i]; it[i] = it[j]; it[j] = t }

@Composable
internal fun MoveButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    IconBubble(
        background = if (enabled) c.cardMuted else c.card,
        size = 34.dp,
        modifier = Modifier.padding(horizontal = 2.dp).clip(CircleShape).bounceClick(enabled = enabled, onClick = onClick)
    ) {
        Icon(icon, contentDescription = description, tint = if (enabled) c.textPrimary else c.textMuted, modifier = Modifier.size(20.dp))
    }
}
