package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.leafGradient
import com.example.ui.viewmodel.BottomTab
import kotlin.math.abs

private val BarHeight = 68.dp
private val BarSidePadding = 8.dp
private val IndicatorWidth = 56.dp
private val IndicatorHeight = 32.dp
private val IndicatorTop = 8.dp
private const val SlotCount = 5 // 4 tabs + centre add button

private fun BottomTab.slotIndex(): Int = when (this) {
    BottomTab.TODAY -> 0
    BottomTab.PLAN -> 1
    BottomTab.CHALLENGES -> 3
}

@Composable
fun CustomBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onAddHabitClick: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val hairline = c.border

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface)
            .drawBehind {
                drawLine(
                    color = hairline,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .padding(horizontal = BarSidePadding)
        ) {
            val slotWidth = maxWidth / SlotCount

            // Gliding pill indicator behind the selected tab's icon. It stretches slightly
            // while travelling and settles back to its resting width.
            val targetX = slotWidth * currentTab.slotIndex() + (slotWidth - IndicatorWidth) / 2
            val indicatorX by animateDpAsState(
                targetValue = targetX,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                label = "indicator_x"
            )
            val stretch = minOf(abs((targetX - indicatorX).value), 40f).dp * 0.5f
            Box(
                modifier = Modifier
                    .offset(x = indicatorX - stretch / 2, y = IndicatorTop)
                    .size(width = IndicatorWidth + stretch, height = IndicatorHeight)
                    .clip(AppShapes.pill)
                    .background(c.primarySoft)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                BottomNavItem(
                    icon = Icons.Default.WbSunny,
                    label = "Today",
                    isSelected = currentTab == BottomTab.TODAY,
                    onClick = { onTabSelected(BottomTab.TODAY) },
                    testTag = "nav_today",
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Plan",
                    isSelected = currentTab == BottomTab.PLAN,
                    onClick = { onTabSelected(BottomTab.PLAN) },
                    testTag = "nav_plan",
                    modifier = Modifier.weight(1f)
                )

                // Placeholder slot for the centre add button
                Box(modifier = Modifier.weight(1f))

                BottomNavItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Challenges",
                    isSelected = currentTab == BottomTab.CHALLENGES,
                    onClick = { onTabSelected(BottomTab.CHALLENGES) },
                    testTag = "nav_challenges",
                    modifier = Modifier.weight(1f)
                )
                // Stats opens the analytics overlay (not a tab, so no indicator)
                BottomNavItem(
                    icon = Icons.Default.Insights,
                    label = "Stats",
                    isSelected = false,
                    onClick = onOpenStats,
                    testTag = "nav_stats",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Centre add button on a leaf gradient
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
                .bounceClick(pressedScale = 0.86f, onClick = onAddHabitClick)
                .size(56.dp)
                .shadow(12.dp, CircleShape, ambientColor = c.primary, spotColor = c.primary)
                .clip(CircleShape)
                .background(c.leafGradient)
                .testTag("fab_add_habit"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Habit",
                tint = c.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    val tint by animateColorAsState(
        targetValue = if (isSelected) c.primary else c.textMuted,
        animationSpec = tween(300),
        label = "nav_tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "nav_scale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .bounceClick(pressedScale = 0.9f, onClick = onClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = IndicatorTop)
                .height(IndicatorHeight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
