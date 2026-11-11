package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Health", // Health, Mindfulness, Fitness, Productivity
    val timeOfDay: String = "MORNING", // MORNING, AFTERNOON, EVENING, ANYTIME
    val scheduledTime: String = "08:00", // "07:00", "08:00", etc.
    val targetValue: Int = 1, // e.g. 3000 ml, 8000 steps, 1 task
    val unit: String = "tasks", // ml, steps, tasks, mins, glasses
    val iconName: String = "water", // water, walk, sun, moon, book, meditate, sparkle, trophy
    val colorHex: String = "#7C3AED",
    val routineGroup: String? = null, // e.g., "Happy Mornings"
    val challengeId: String? = null,
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "08:00",
    val reminderDays: String = "1,2,3,4,5,6,7", // 1=Sun, 7=Sat
    val syncWithGoogleCalendar: Boolean = false,
    val calendarEventId: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Days of week this habit runs on (java.util.Calendar.DAY_OF_WEEK: 1 = Sunday … 7 = Saturday). */
fun HabitEntity.scheduledDays(): Set<Int> =
    reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..7 }.toSet()
        .ifEmpty { (1..7).toSet() }

fun HabitEntity.isScheduledOn(dayOfWeek: Int): Boolean = dayOfWeek in scheduledDays()

/** "Every day", "Weekdays", "Weekends" or e.g. "Mon, Wed, Fri". */
fun HabitEntity.scheduleLabel(): String = scheduleLabel(scheduledDays())

fun scheduleLabel(days: Set<Int>): String {
    val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    return when (days) {
        (1..7).toSet() -> "Every day"
        setOf(2, 3, 4, 5, 6) -> "Weekdays"
        setOf(1, 7) -> "Weekends"
        else -> (listOf(2, 3, 4, 5, 6, 7, 1).filter { it in days }).joinToString(", ") { names[it - 1] }
    }
}

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val progressValue: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null,
    /** Progress just before the habit was completed, restored if the user un-ticks it. */
    @ColumnInfo(defaultValue = "0")
    val previousProgress: Int = 0
)

data class ChallengeTaskItem(
    val title: String,
    val description: String = "",
    val timeOfDay: String = "MORNING",
    val scheduledTime: String = "08:00",
    val targetValue: Int = 1,
    val unit: String = "tasks",
    val iconName: String = "sparkle",
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "08:00",
    val syncWithGoogleCalendar: Boolean = true,
    val tip: String = ""
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String = "",
    val durationDays: Int = 7,
    val currentDay: Int = 1,
    val skipsLeft: Int = 1,
    val isJoined: Boolean = false,
    val category: String = "Mindfulness",
    val bannerResName: String? = null,
    val totalTasks: Int = 2,
    val completedTasksToday: Int = 0,
    val dailyTip: String = "",
    val tasksJson: String = "",
    val goalTarget: Float = 0f,
    val goalCurrent: Float = 0f,
    val goalUnit: String = "",
    /** When the user joined (epoch millis); 0 = not started. Drives the real "Day X of Y". */
    @ColumnInfo(defaultValue = "0")
    val startedAt: Long = 0L,
    /** AppIcon key for this challenge; blank = use the category icon. */
    @ColumnInfo(defaultValue = "")
    val icon: String = "",
    /** How many times the user has finished this challenge. */
    @ColumnInfo(defaultValue = "0")
    val timesCompleted: Int = 0
) {
    fun getParsedTasks(): List<ChallengeTaskItem> {
        val list = mutableListOf<ChallengeTaskItem>()
        if (tasksJson.isBlank()) return list
        try {
            val array = JSONArray(tasksJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChallengeTaskItem(
                        title = obj.optString("title", "Task ${i + 1}"),
                        description = obj.optString("description", ""),
                        timeOfDay = obj.optString("timeOfDay", "MORNING"),
                        scheduledTime = obj.optString("scheduledTime", "08:00"),
                        targetValue = obj.optInt("targetValue", 1),
                        unit = obj.optString("unit", "tasks"),
                        iconName = obj.optString("iconName", "sparkle"),
                        reminderEnabled = obj.optBoolean("reminderEnabled", true),
                        reminderTime = obj.optString("reminderTime", "08:00"),
                        syncWithGoogleCalendar = obj.optBoolean("syncWithGoogleCalendar", true),
                        tip = obj.optString("tip", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

@Entity(
    tableName = "mood_logs",
    indices = [Index(value = ["date"], unique = true)]
)
data class MoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val moodScore: Int, // 5 = Great, 4 = Good, 3 = Okay, 2 = Meh, 1 = Bad
    val moodLabel: String, // "Great", "Good", "Okay", "Meh", "Bad"
    val note: String? = null
)

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val description: String? = null,
    val location: String? = null,
    val color: Int? = null
)

// UI wrapper combining Habit and today's log
data class HabitWithStatus(
    val habit: HabitEntity,
    val log: HabitLogEntity?,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val currentStreak: Int = 0
)
