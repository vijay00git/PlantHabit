package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DateItem(
    val dayName: String, // "Sun", "Mon"
    val dayNumber: String, // "8", "9"
    val dateString: String, // "2026-11-08"
    val isToday: Boolean
)

private val DayPillMinWidth = 46.dp
private val DayPillSpacing = 6.dp
private val StripSidePadding = 16.dp

@Composable
fun HorizontalDateStrip(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = remember {
        val list = mutableListOf<DateItem>()
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumberFormat = SimpleDateFormat("d", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        // Align to current week (Sunday start)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0 until 7) {
            val dateStr = dateFormat.format(cal.time)
            list.add(
                DateItem(
                    dayName = dayNameFormat.format(cal.time),
                    dayNumber = dayNumberFormat.format(cal.time),
                    dateString = dateStr,
                    isToday = dateStr == todayStr
                )
            )
            cal.add(Calendar.DAY_OF_WEEK, 1)
        }
        list
    }

    val listState = rememberLazyListState()
    val selectedIndex = weekDays.indexOfFirst { it.dateString == selectedDate }

    // Keep the selected day in view (no-op when the whole week already fits).
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            val visible = listState.layoutInfo.visibleItemsInfo
            val fullyVisible = visible.any { info ->
                info.index == selectedIndex &&
                    info.offset >= 0 &&
                    info.offset + info.size <= listState.layoutInfo.viewportEndOffset
            }
            if (!fullyVisible) {
                listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Fill the width when there's room, otherwise fall back to a scrollable strip.
        val available = maxWidth - StripSidePadding * 2 - DayPillSpacing * (weekDays.size - 1)
        val pillWidth: Dp = maxOf(DayPillMinWidth, available / weekDays.size)

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = StripSidePadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(DayPillSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(weekDays, key = { _, item -> item.dateString }) { _, item ->
                DayPill(
                    item = item,
                    isSelected = item.dateString == selectedDate,
                    width = pillWidth,
                    onClick = { onDateSelected(item.dateString) }
                )
            }
        }
    }
}

@Composable
private fun DayPill(
    item: DateItem,
    isSelected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val c = PlantTheme.colors

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) c.primary else c.card,
        animationSpec = tween(300),
        label = "day_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) c.primary else c.border,
        animationSpec = tween(300),
        label = "day_border"
    )
    val dayTextColor by animateColorAsState(
        targetValue = if (isSelected) c.onPrimary else c.textMuted,
        animationSpec = tween(300),
        label = "day_text"
    )
    val numberCircleColor by animateColorAsState(
        targetValue = when {
            isSelected -> c.onPrimary
            item.isToday -> c.sunriseSoft
            else -> c.cardMuted
        },
        animationSpec = tween(300),
        label = "day_circle"
    )
    val numberTextColor by animateColorAsState(
        targetValue = when {
            isSelected -> c.primaryDeep
            item.isToday -> c.sunrise
            else -> c.textPrimary
        },
        animationSpec = tween(300),
        label = "day_number"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "day_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .bounceClick(pressedScale = 0.92f, onClick = onClick)
            .width(width)
            .clip(AppShapes.large)
            .background(containerColor)
            .border(1.dp, borderColor, AppShapes.large)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("date_strip_${item.dayName}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.dayName,
                style = MaterialTheme.typography.labelMedium,
                color = dayTextColor
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(numberCircleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.dayNumber,
                    style = MaterialTheme.typography.titleSmall,
                    color = numberTextColor
                )
            }

            // Little "today" dot
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !item.isToday -> Color.Transparent
                            isSelected -> c.onPrimary
                            else -> c.sunrise
                        }
                    )
            )
        }
    }
}
