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
    val createdAt: Long = System.currentTimeMillis(),
    /** Manual order on Today (lower first); ties fall back to scheduled time. */
    @ColumnInfo(defaultValue = "0")
    val sortOrder: Int = 0,
    /** >0 = flexible "N times per week" goal on any days (reminderDays then lists allowed days). */
    @ColumnInfo(defaultValue = "0")
    val timesPerWeek: Int = 0,
    /** Additional daily reminder times, comma-separated "HH:mm". */
    @ColumnInfo(defaultValue = "")
    val extraReminderTimes: String = ""
)

/** Days of week this habit runs on (java.util.Calendar.DAY_OF_WEEK: 1 = Sunday … 7 = Saturday). */
fun HabitEntity.scheduledDays(): Set<Int> =
    reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..7 }.toSet()
        .ifEmpty { (1..7).toSet() }

fun HabitEntity.isScheduledOn(dayOfWeek: Int): Boolean = dayOfWeek in scheduledDays()

/** "Every day", "Weekdays", "Weekends" or e.g. "Mon, Wed, Fri". */
fun HabitEntity.scheduleLabel(): String =
    if (timesPerWeek > 0) "${timesPerWeek}× a week" else scheduleLabel(scheduledDays())

/** Monday (yyyy-MM-dd) of the week containing [date]. */
fun weekStartOf(date: String): String {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val cal = java.util.Calendar.getInstance().apply { time = fmt.parse(date) ?: java.util.Date() }
    val back = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0 … Sun=6
    cal.add(java.util.Calendar.DAY_OF_YEAR, -back)
    return fmt.format(cal.time)
}

/** The 7 dates (Mon–Sun) of the week starting [weekStart]. */
fun weekDates(weekStart: String): List<String> {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val cal = java.util.Calendar.getInstance().apply { time = fmt.parse(weekStart) ?: java.util.Date() }
    return List(7) { i -> if (i > 0) cal.add(java.util.Calendar.DAY_OF_YEAR, 1); fmt.format(cal.time) }
}

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
    val previousProgress: Int = 0,
    /** A "streak freeze": the day doesn't count as done, but doesn't break the streak. */
    @ColumnInfo(defaultValue = "0")
    val isSkipped: Boolean = false
)

/** An unlocked achievement (definitions live in code; see Achievements). */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long
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
    val timesCompleted: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean = false,
    /** User's own group ("My groups"); blank = ungrouped. */
    @ColumnInfo(defaultValue = "")
    val groupName: String = "",
    /** Built-in challenges can't be deleted (they'd come back on sync), so they're hidden. */
    @ColumnInfo(defaultValue = "0")
    val isHidden: Boolean = false
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

/** Whole calendar days since the user joined (0 on the start day). */
fun ChallengeEntity.daysSinceStart(): Int {
    if (startedAt <= 0) return 0
    fun startOfDay(millis: Long) = java.util.Calendar.getInstance().apply {
        timeInMillis = millis
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    return ((startOfDay(System.currentTimeMillis()) - startOfDay(startedAt)) / (24L * 60 * 60 * 1000)).toInt()
}

/** Joined and either every day has passed or the goal target is reached. */
fun ChallengeEntity.isFinished(): Boolean =
    isJoined && ((startedAt > 0 && daysSinceStart() >= durationDays) || (goalTarget > 0f && goalCurrent >= goalTarget))

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
    val currentStreak: Int = 0,
    /** For "N times a week" habits: completions in the selected date's week (Mon–Sun). */
    val weekDone: Int = 0
) {
    val isWeekly: Boolean get() = habit.timesPerWeek > 0
    /** Weekly goal already reached and today isn't ticked: nothing more needed this week. */
    val weeklyGoalMet: Boolean get() = isWeekly && weekDone >= habit.timesPerWeek && !isCompleted
}
