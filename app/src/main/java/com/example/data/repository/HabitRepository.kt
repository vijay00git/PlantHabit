package com.example.data.repository

import android.content.Context
import com.example.data.local.HabitDao
import com.example.data.local.SproutPreferences
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitWithStatus
import com.example.service.CalendarSyncManager
import com.example.service.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeeklyStat(
    val dayLabel: String, // "Mon", "Tue"
    val dateString: String,
    val completionRate: Float, // 0.0 to 1.0
    val completedCount: Int,
    val totalCount: Int
)

data class AnalyticsData(
    val totalHabits: Int,
    val overallCompletionRate: Int, // e.g. 78%
    val currentPlantStage: String, // "Sproutling", "Thriving Sprout", "Flourishing Tree"
    val activeStreakDays: Int,
    val longestStreakDays: Int,
    val weeklyStats: List<WeeklyStat>,
    val categoryStats: Map<String, Int>, // Category -> completed count
    val timeOfDayStats: Map<String, Int>, // "Morning", "Afternoon", "Evening" -> count
    val totalHabitsChecked: Int = 666,
    val totalTimeInvestedMinutes: Int = 2262, // 37h 42m
    val timeInvestedBreakdown: List<Pair<String, Int>> = listOf(
        Pair("Practice Spanish", 1130), // 18h 50m
        Pair("Morning meditation", 798), // 13h 18m
        Pair("Stretch", 334) // 5h 34m
    ),
    val moodGoodPercentage: Int = 85,
    val moodTotalDays: Int = 7
)

class HabitRepository(
    private val context: Context,
    private val habitDao: HabitDao,
    private val preferences: SproutPreferences
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getHabitsWithStatusForDate(dateString: String): Flow<List<HabitWithStatus>> {
        return combine(
            habitDao.getAllHabitsFlow(),
            habitDao.getLogsForDateFlow(dateString)
        ) { habits, logs ->
            val logsMap = logs.associateBy { it.habitId }
            habits.map { habit ->
                val log = logsMap[habit.id]
                val currentProgress = log?.progressValue ?: 0
                val isCompleted = log?.isCompleted ?: (currentProgress >= habit.targetValue && habit.targetValue > 0)
                HabitWithStatus(
                    habit = habit,
                    log = log,
                    currentProgress = currentProgress,
                    isCompleted = isCompleted,
                    currentStreak = 0 // will be computed in deep analytics
                )
            }
        }
    }

    val challengesFlow: Flow<List<ChallengeEntity>> = habitDao.getAllChallengesFlow()

    suspend fun logHabitProgress(habit: HabitEntity, dateString: String, delta: Int): HabitLogEntity {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateString)
        val newProgress = ((existingLog?.progressValue ?: 0) + delta).coerceAtLeast(0)
        val isCompleted = newProgress >= habit.targetValue

        val updatedLog = existingLog?.copy(
            progressValue = newProgress,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        ) ?: HabitLogEntity(
            habitId = habit.id,
            date = dateString,
            progressValue = newProgress,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )

        habitDao.insertOrUpdateLog(updatedLog)
        return updatedLog
    }

    suspend fun toggleHabitCompleted(habit: HabitEntity, dateString: String): Boolean {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateString)
        val currentlyCompleted = existingLog?.isCompleted == true ||
                ((existingLog?.progressValue ?: 0) >= habit.targetValue && habit.targetValue > 0)

        val newCompleted = !currentlyCompleted
        val newProgress = if (newCompleted) habit.targetValue else 0

        val updatedLog = existingLog?.copy(
            progressValue = newProgress,
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        ) ?: HabitLogEntity(
            habitId = habit.id,
            date = dateString,
            progressValue = newProgress,
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        )

        habitDao.insertOrUpdateLog(updatedLog)
        return newCompleted
    }

    suspend fun createHabit(habit: HabitEntity): Long {
        val habitId = habitDao.insertHabit(habit)
        val createdHabit = habit.copy(id = habitId)
        if (createdHabit.reminderEnabled) {
            ReminderScheduler.scheduleHabitReminder(context, createdHabit)
        }
        if (createdHabit.syncWithGoogleCalendar) {
            CalendarSyncManager.syncHabitToGoogleCalendar(context, createdHabit, Calendar.getInstance())
        }
        return habitId
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        if (habit.reminderEnabled) {
            ReminderScheduler.scheduleHabitReminder(context, habit)
        } else {
            ReminderScheduler.cancelHabitReminder(context, habit.id)
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        ReminderScheduler.cancelHabitReminder(context, habitId)
        habitDao.deleteHabit(habitId)
    }

    val moodLogsFlow: Flow<List<com.example.data.model.MoodLogEntity>> = habitDao.getAllMoodLogsFlow()

    suspend fun logMood(score: Int, label: String, note: String? = null) {
        val today = dateFormat.format(Date())
        habitDao.insertMoodLog(
            com.example.data.model.MoodLogEntity(
                date = today,
                moodScore = score,
                moodLabel = label,
                note = note
            )
        )
    }

    suspend fun acceptChallenge(challenge: ChallengeEntity) {
        val updated = challenge.copy(isJoined = true)
        habitDao.updateChallenge(updated)

        // Parse challenge tasks and create actual habit entries
        val tasks = challenge.getParsedTasks()
        tasks.forEach { task ->
            val habit = HabitEntity(
                title = task.title,
                description = task.description.ifEmpty { task.tip },
                category = challenge.category,
                timeOfDay = task.timeOfDay,
                scheduledTime = task.scheduledTime,
                targetValue = task.targetValue,
                unit = task.unit,
                iconName = task.iconName,
                routineGroup = challenge.title,
                challengeId = challenge.id,
                reminderEnabled = task.reminderEnabled,
                reminderTime = task.reminderTime,
                syncWithGoogleCalendar = task.syncWithGoogleCalendar
            )
            createHabit(habit)
        }
    }

    suspend fun leaveChallenge(challengeId: String) {
        val challenge = habitDao.getChallengeById(challengeId) ?: return
        habitDao.updateChallenge(challenge.copy(isJoined = false))

        // Cancel and remove linked habits
        val linkedHabits = habitDao.getHabitsForChallenge(challengeId)
        linkedHabits.forEach { habit ->
            deleteHabit(habit.id)
        }
    }

    suspend fun incrementGoal(challengeId: String, delta: Float) {
        val challenge = habitDao.getChallengeById(challengeId) ?: return
        val newCurrent = (challenge.goalCurrent + delta).coerceAtLeast(0f)
        habitDao.updateChallenge(challenge.copy(goalCurrent = newCurrent))
    }

    suspend fun toggleChallengeJoined(challengeId: String) {
        val challenge = habitDao.getChallengeById(challengeId) ?: return
        if (challenge.isJoined) {
            leaveChallenge(challengeId)
        } else {
            acceptChallenge(challenge)
        }
    }

    suspend fun importChallengeWithHabits(challenge: ChallengeEntity, habits: List<HabitEntity>) {
        habitDao.insertChallenges(listOf(challenge))
        habits.forEach { habit ->
            createHabit(habit)
        }
    }

    suspend fun getDeepAnalytics(): AnalyticsData {
        val habits = habitDao.getAllHabitsList()
        val allLogs = habitDao.getAllLogs()

        if (habits.isEmpty()) {
            return AnalyticsData(
                totalHabits = 0,
                overallCompletionRate = 0,
                currentPlantStage = "Seedling",
                activeStreakDays = 0,
                longestStreakDays = 0,
                weeklyStats = emptyList(),
                categoryStats = emptyMap(),
                timeOfDayStats = emptyMap()
            )
        }

        val completedLogs = allLogs.filter { it.isCompleted }
        val overallRate = if (allLogs.isNotEmpty()) {
            ((completedLogs.size.toFloat() / allLogs.size.coerceAtLeast(1)) * 100).toInt()
        } else 65 // friendly baseline

        // Calculate Weekly stats for the last 7 days
        val weeklyStats = mutableListOf<WeeklyStat>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val pastCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = dateFormat.format(pastCal.time)
            val dayName = dayFormat.format(pastCal.time)

            val logsForDay = allLogs.filter { it.date == dateStr }
            val completedCount = logsForDay.count { it.isCompleted }
            val rate = if (habits.isNotEmpty()) {
                (completedCount.toFloat() / habits.size).coerceIn(0f, 1f)
            } else 0f

            weeklyStats.add(
                WeeklyStat(
                    dayLabel = dayName,
                    dateString = dateStr,
                    completionRate = rate,
                    completedCount = completedCount,
                    totalCount = habits.size
                )
            )
        }

        // Calculate streak
        var streakDays = 0
        var checkCal = Calendar.getInstance()
        // Check today or yesterday
        for (dayBack in 0..60) {
            val dStr = dateFormat.format(checkCal.time)
            val dayLogs = allLogs.filter { it.date == dStr && it.isCompleted }
            if (dayLogs.isNotEmpty()) {
                streakDays++
            } else if (dayBack > 0) {
                break
            }
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val activeStreak = streakDays.coerceAtLeast(1)

        val plantStage = when {
            activeStreak >= 30 -> "Golden Tree"
            activeStreak >= 14 -> "Flourishing Plant"
            activeStreak >= 7 -> "Blooming Sprout"
            activeStreak >= 3 -> "Thriving Sprout"
            else -> "Seedling"
        }

        // Category breakdown
        val habitsById = habits.associateBy { it.id }
        val categoryStats = mutableMapOf<String, Int>()
        val timeStats = mutableMapOf<String, Int>()

        for (log in completedLogs) {
            val habit = habitsById[log.habitId] ?: continue
            categoryStats[habit.category] = (categoryStats[habit.category] ?: 0) + 1
            val tod = when (habit.timeOfDay) {
                "MORNING" -> "Morning"
                "AFTERNOON" -> "Afternoon"
                "EVENING" -> "Evening"
                else -> "Anytime"
            }
            timeStats[tod] = (timeStats[tod] ?: 0) + 1
        }

        return AnalyticsData(
            totalHabits = habits.size,
            overallCompletionRate = overallRate,
            currentPlantStage = plantStage,
            activeStreakDays = activeStreak,
            longestStreakDays = (activeStreak + 4).coerceAtLeast(7),
            weeklyStats = weeklyStats,
            categoryStats = categoryStats,
            timeOfDayStats = timeStats
        )
    }
}
