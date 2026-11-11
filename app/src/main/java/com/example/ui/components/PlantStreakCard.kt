package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutCardDark

@Composable
fun PlantStreakCard(
    completedCount: Int,
    totalCount: Int,
    activeStreakDays: Int,
    modifier: Modifier = Modifier
) {
    val progressRatio = if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "plant_progress"
    )
    val percentage = (progressRatio * 100).toInt()
    val remaining = (totalCount - completedCount).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SproutCardDark)
            .border(1.dp, SproutCardBorderDark, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Progress on Left
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(76.dp),
                    color = Color(0xFF332D42),
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                // Progress Arc
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(76.dp),
                    color = if (percentage == 100) SproutAccentGreenGlow else SproutAccentAmber,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedCount/$totalCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "DONE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Streak Message in Middle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$percentage% there.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (remaining > 0) "$remaining more to keep the streak alive." else "Streak secured for today! 🔥",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = if (activeStreakDays > 0) "$activeStreakDays day streak • Sprout thriving 🌱" else "Plant a streak to grow.",
                    fontSize = 12.sp,
                    color = SproutAccentGreenGlow,
                    fontWeight = FontWeight.Medium
                )
            }

            // Plant in Pot Image on Right
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161421)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sprout_launcher_icon),
                    contentDescription = "Sprout streak plant",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
