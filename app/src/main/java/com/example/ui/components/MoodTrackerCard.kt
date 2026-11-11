package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodLogEntity
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark

data class MoodOption(
    val score: Int,
    val emoji: String,
    val label: String
)

@Composable
fun MoodTrackerCard(
    currentMood: MoodLogEntity?,
    onSelectMood: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        MoodOption(5, "😄", "Great"),
        MoodOption(4, "🙂", "Good"),
        MoodOption(3, "😐", "Okay"),
        MoodOption(2, "🙁", "Meh"),
        MoodOption(1, "😣", "Bad")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SproutCardDark)
            .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("card_mood_tracker")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Mood Check-in",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (currentMood != null) "Logged: ${currentMood.moodLabel} today" else "How are you feeling today?",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Balance badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF221E33))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "85% Good or great",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SproutAccentGreenGlow
                    )
                }
            }

            // Emoji Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                moods.forEach { option ->
                    val isSelected = currentMood?.moodScore == option.score

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SproutAccentAmber.copy(alpha = 0.25f) else Color(0xFF232032))
                            .border(
                                1.5.dp,
                                if (isSelected) SproutAccentAmber else Color.Transparent,
                                CircleShape
                            )
                            .clickable { onSelectMood(option.score, option.label) }
                            .testTag("mood_option_${option.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = option.emoji,
                                fontSize = 22.sp
                            )
                            Text(
                                text = option.label,
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SproutAccentAmber else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
