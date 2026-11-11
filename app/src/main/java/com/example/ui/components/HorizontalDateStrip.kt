package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SproutAccentGreen
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutPrimary
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDays.forEach { item ->
            val isSelected = item.dateString == selectedDate

            val containerColor = if (isSelected) {
                Color(0xFFF1F5F9) // White-ish pill from screenshot
            } else {
                Color(0xFF1E1C28) // Deep dark card
            }

            val dayTextColor = if (isSelected) {
                Color(0xFF334155)
            } else {
                Color(0xFF94A3B8)
            }

            val numberCircleColor = if (isSelected) {
                Color(0xFFFFFFFF)
            } else {
                Color(0xFF282536)
            }

            val numberTextColor = if (isSelected) {
                Color(0xFF0F172A)
            } else {
                Color(0xFFCBD5E1)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(containerColor)
                    .clickable { onDateSelected(item.dateString) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .width(44.dp)
                    .testTag("date_strip_${item.dayName}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.dayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = dayTextColor
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(numberCircleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.dayNumber,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = numberTextColor
                        )
                    }
                }
            }
        }
    }
}
