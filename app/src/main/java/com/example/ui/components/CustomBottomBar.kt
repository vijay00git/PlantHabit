package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight
import com.example.ui.viewmodel.BottomTab

@Composable
fun CustomBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onAddHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color(0xFF13121A)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.WbSunny,
                label = "Today",
                isSelected = currentTab == BottomTab.TODAY,
                onClick = { onTabSelected(BottomTab.TODAY) },
                testTag = "nav_today"
            )

            BottomNavItem(
                icon = Icons.Default.CalendarMonth,
                label = "Plan",
                isSelected = currentTab == BottomTab.PLAN,
                onClick = { onTabSelected(BottomTab.PLAN) },
                testTag = "nav_plan"
            )

            // Placeholder spacer for the center FAB
            Box(modifier = Modifier.size(54.dp))

            BottomNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "AI",
                isSelected = currentTab == BottomTab.AI,
                onClick = { onTabSelected(BottomTab.AI) },
                testTag = "nav_ai"
            )

            BottomNavItem(
                icon = Icons.Default.EmojiEvents,
                label = "Challenges",
                isSelected = currentTab == BottomTab.CHALLENGES,
                onClick = { onTabSelected(BottomTab.CHALLENGES) },
                testTag = "nav_challenges"
            )
        }

        // Center Elevated Glowing (+) FAB
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(58.dp)
                .shadow(12.dp, CircleShape, spotColor = SproutPrimaryLight)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF6D28D9)
                        )
                    )
                )
                .clickable { onAddHabitClick() }
                .testTag("fab_add_habit"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Habit",
                tint = Color.White,
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
    testTag: String
) {
    val tint = if (isSelected) Color(0xFFC084FC) else Color(0xFF71717A)

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint
        )
    }
}
