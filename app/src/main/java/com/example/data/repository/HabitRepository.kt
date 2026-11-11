package com.example.data.repository

import com.example.data.model.isScheduledOn
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
    val totalHabitsChecked: Int = 0,
    /** Minutes logged on minute-based habits over the last 12 weeks. */
    val totalTimeInvestedMinutes: Int = 0,
    val timeInvestedBreakdown: List<Pair<String, Int>> = emptyList(),
    /** Share of mood check-ins in the last 7 days that were Good or Great (0 when none). */
    val moodGoodPercentage: Int = 0,
    val moodTotalDays: Int = 0,
    /** Category -> completion % over the last 7 days. */
    val categoryCompletion: List<Pair<String, Int>> = emptyList()
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
            habitDao.getAllLogsFlow()
        ) { habits, allLogs ->
            val logs = allLogs.filter { it.date == dateString }
            val logsMap = logs.associateBy { it.habitId }
            val completedByHabit = allLogs.filter { it.isCompleted }.groupBy({ it.habitId }, { it.date })
                .mapValues { it.value.toSet() }
            val dayOfWeek = dayOfWeekOf(dateString)
            habits.filter { it.isScheduledOn(dayOfWeek) }.map { habit ->
                val log = logsMap[habit.id]
                val currentProgress = log?.progressValue ?: 0
                val isCompleted = log?.isCompleted ?: (currentProgress >= habit.targetValue && habit.targetValue > 0)
                HabitWithStatus(
                    habit = habit,
                    log = log,
                    currentProgress = currentProgress,
                    isCompleted = isCompleted,
                    currentStreak = currentStreak(habit, completedByHabit[habit.id].orEmpty(), dateString)
                )
            }
        }
    }

    /**
     * Challenges with live progress: "Day X of Y" comes from the join date and today's task
     * count from the challenge's own habits and today's logs.
     */
    val challengesFlow: Flow<List<ChallengeEntity>> = combine(
        habitDao.getAllChallengesFlow(),
        habitDao.getAllHabitsFlow(),
        habitDao.getLogsForDateFlow(dateFormat.format(Date()))
    ) { challenges, habits, todayLogs ->
        val completedToday = todayLogs.filter { it.isCompleted }.map { it.habitId }.toSet()
        val habitsByChallenge = habits.filter { it.challengeId != null }.groupBy { it.challengeId }
        challenges
            .sortedWith(compareByDescending<ChallengeEntity> { it.isJoined }.thenBy { it.title })
            .map { ch ->
                var out = ch
                if (ch.isJoined && ch.startedAt > 0) {
                    out = out.copy(currentDay = dayNumber(ch.startedAt).coerceIn(1, ch.durationDays.coerceAtLeast(1)))
                }
                val linked = habitsByChallenge[ch.id].orEmpty()
                if (ch.isJoined && linked.isNotEmpty()) {
                    out = out.copy(totalTasks = linked.size, completedTasksToday = linked.count { it.id in completedToday })
                }
                out
            }
    }

    /**
     * Consecutive scheduled days completed, counting back from [asOf]. Unscheduled days are
     * skipped, and an unfinished [asOf] itself doesn't break the streak (the day isn't over).
     */
    fun currentStreak(habit: HabitEntity, completedDates: Set<String>, asOf: String): Int {
        val cal = Calendar.getInstance().apply { time = dateFormat.parse(asOf) ?: Date() }
        val createdDay = dateFormat.format(Date(habit.createdAt))
        var streak = 0
        for (i in 0 until 730) {
            val d = dateFormat.format(cal.time)
            if (d < createdDay && d !in completedDates) break
            if (habit.isScheduledOn(cal.get(Calendar.DAY_OF_WEEK))) {
                when {
                    d in completedDates -> streak++
                    i == 0 -> Unit // today/selected day still in progress
                    else -> return streak
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    /** Longest run of consecutive scheduled days completed, ever. */
    fun bestStreak(habit: HabitEntity, completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0
        val cal = Calendar.getInstance().apply { time = dateFormat.parse(completedDates.min()) ?: Date() }
        val end = dateFormat.format(Date())
        var best = 0
        var run = 0
        while (dateFormat.format(cal.time) <= end) {
            val d = dateFormat.format(cal.time)
            if (habit.isScheduledOn(cal.get(Calendar.DAY_OF_WEEK))) {
                if (d in completedDates) { run++; best = maxOf(best, run) } else if (d != end) run = 0
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return best
    }

    private fun dayOfWeekOf(dateString: String): Int = Calendar.getInstance().apply {
        time = dateFormat.parse(dateString) ?: Date()
    }.get(Calendar.DAY_OF_WEEK)

    /** 1 on the start day, 2 the next calendar day, and so on. */
    private fun dayNumber(startedAt: Long): Int {
        fun startOfDay(millis: Long) = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val days = (startOfDay(System.currentTimeMillis()) - startOfDay(startedAt)) / (24L * 60 * 60 * 1000)
        return days.toInt() + 1
    }

    /**
     * Clears everything the user logged — habits (and their reminders), check-ins, moods and
     * challenge progress — so they can start fresh. Challenges themselves, banner photos and
     * settings are kept.
     */
    suspend fun resetAllData() {
        habitDao.getAllHabitsList().forEach { ReminderScheduler.cancelHabitReminder(context, it.id) }
        habitDao.deleteAllLogs()
        habitDao.deleteAllHabits()
        habitDao.deleteAllMoodLogs()
        habitDao.resetAllChallengeProgress()
    }

    suspend fun logHabitProgress(habit: HabitEntity, dateString: String, delta: Int): HabitLogEntity {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateString)
        val oldProgress = existingLog?.progressValue ?: 0
        val newProgress = (oldProgress + delta).coerceAtLeast(0)
        val isCompleted = newProgress >= habit.targetValue
        // Remember where we were when this step completed the habit
        val previous = if (isCompleted && existingLog?.isCompleted != true) oldProgress
            else existingLog?.previousProgress ?: 0

        val updatedLog = existingLog?.copy(
            progressValue = newProgress,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            previousProgress = previous
        ) ?: HabitLogEntity(
            habitId = habit.id,
            date = dateString,
            progressValue = newProgress,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            previousProgress = previous
        )

        habitDao.insertOrUpdateLog(updatedLog)
        return updatedLog
    }

    suspend fun toggleHabitCompleted(habit: HabitEntity, dateString: String): Boolean {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateString)
        val currentlyCompleted = existingLog?.isCompleted == true ||
                ((existingLog?.progressValue ?: 0) >= habit.targetValue && habit.targetValue > 0)

        val newCompleted = !currentlyCompleted
        val oldProgress = existingLog?.progressValue ?: 0
        // Completing jumps to the target; un-ticking restores what was there before
        val newProgress = if (newCompleted) maxOf(habit.targetValue, oldProgress)
            else (existingLog?.previousProgress ?: 0).coerceIn(0, (habit.targetValue - 1).coerceAtLeast(0))
        val previous = if (newCompleted) oldProgress else 0

        val updatedLog = existingLog?.copy(
            progressValue = newProgress,
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null,
            previousProgress = previous
        ) ?: HabitLogEntity(
            habitId = habit.id,
            date = dateString,
            progressValue = newProgress,
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null,
            previousProgress = previous
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
        syncCalendarEvent(createdHabit)
        return habitId
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
        if (habit.reminderEnabled) {
            ReminderScheduler.scheduleHabitReminder(context, habit)
        } else {
            ReminderScheduler.cancelHabitReminder(context, habit.id)
        }
        syncCalendarEvent(habit)
    }

    /** Keeps the habit's single recurring calendar event in line with its settings. */
    private suspend fun syncCalendarEvent(habit: HabitEntity) {
        val newId = if (habit.syncWithGoogleCalendar) {
            CalendarSyncManager.upsertHabitEvent(context, habit)
        } else {
            CalendarSyncManager.deleteHabitEvent(context, habit.calendarEventId)
            null
        }
        if (newId != habit.calendarEventId) habitDao.updateHabit(habit.copy(calendarEventId = newId))
    }

    suspend fun getAllHabitsOnce(): List<HabitEntity> = habitDao.getAllHabitsList()

    /** Creates/updates events for every habit with calendar sync on. Returns how many. */
    suspend fun syncAllHabitsToCalendar(): Int {
        val habits = habitDao.getAllHabitsList().filter { it.syncWithGoogleCalendar }
        habits.forEach { syncCalendarEvent(it) }
        return habits.size
    }

    suspend fun deleteHabit(habitId: Long) {
        ReminderScheduler.cancelHabitReminder(context, habitId)
        CalendarSyncManager.deleteHabitEvent(context, habitDao.getHabitById(habitId)?.calendarEventId)
        habitDao.deleteLogsForHabit(habitId)
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
        // Joining starts the clock fresh: day 1 today, goal counter from zero.
        val updated = challenge.copy(
            isJoined = true,
            startedAt = System.currentTimeMillis(),
            currentDay = 1,
            goalCurrent = 0f,
            completedTasksToday = 0
        )
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
        habitDao.updateChallenge(challenge.copy(isJoined = false, startedAt = 0L, currentDay = 1, completedTasksToday = 0))

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
        // Re-importing a challenge with the same id keeps any banner photo already attached.
        val existingBanner = habitDao.getChallengeById(challenge.id)?.bannerResName
        habitDao.insertChallenges(listOf(challenge.copy(bannerResName = challenge.bannerResName ?: existingBanner)))
        habits.forEach { habit ->
            createHabit(habit)
        }
    }

    /** Sets (or clears, with null) a challenge's banner reference. Returns the previous one. */
    suspend fun setChallengeBanner(challengeId: String, bannerRef: String?): Pair<String?, ChallengeEntity?> {
        val existing = habitDao.getChallengeById(challengeId) ?: return null to null
        val updated = existing.copy(bannerResName = bannerRef)
        habitDao.updateChallenge(updated)
        return existing.bannerResName to updated
    }

    suspend fun getDeepAnalytics(): AnalyticsData {
        val habits = habitDao.getAllHabitsList()
        val allLogs = habitDao.getAllLogs()
        val moods = habitDao.getAllMoodLogs()

        // Mood: share of Good/Great check-ins in the last 7 days
        val weekAgo = dateFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time)
        val recentMoods = moods.filter { it.date >= weekAgo }
        val moodGood = if (recentMoods.isEmpty()) 0
            else recentMoods.count { it.moodScore >= 4 } * 100 / recentMoods.size

        if (habits.isEmpty()) {
            return AnalyticsData(
                totalHabits = 0,
                overallCompletionRate = 0,
                currentPlantStage = "Seedling",
                activeStreakDays = 0,
                longestStreakDays = 0,
                weeklyStats = emptyList(),
                categoryStats = emptyMap(),
                timeOfDayStats = emptyMap(),
                moodGoodPercentage = moodGood,
                moodTotalDays = recentMoods.size
            )
        }

        val habitsById = habits.associateBy { it.id }
        // Only count logs for habits that still exist
        val liveLogs = allLogs.filter { it.habitId in habitsById }
        val completedLogs = liveLogs.filter { it.isCompleted }
        val overallRate = if (liveLogs.isNotEmpty()) completedLogs.size * 100 / liveLogs.size else 0

        // Weekly stats for the last 7 days
        val weeklyStats = mutableListOf<WeeklyStat>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        for (i in 6 downTo 0) {
            val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = dateFormat.format(day.time)
            val completedCount = completedLogs.count { it.date == dateStr }
            val scheduledCount = habits.count { it.isScheduledOn(day.get(Calendar.DAY_OF_WEEK)) }
            weeklyStats.add(
                WeeklyStat(
                    dayLabel = dayFormat.format(day.time),
                    dateString = dateStr,
                    completionRate = if (scheduledCount == 0) 0f else (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f),
                    completedCount = completedCount,
                    totalCount = scheduledCount
                )
            )
        }

        // Streaks: consecutive days with at least one completed habit
        val completedDates = completedLogs.map { it.date }.toSet()
        var activeStreak = 0
        val cursor = Calendar.getInstance()
        // Today doesn't break the streak until it's over
        if (dateFormat.format(cursor.time) !in completedDates) cursor.add(Calendar.DAY_OF_YEAR, -1)
        while (dateFormat.format(cursor.time) in completedDates) {
            activeStreak++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        var longest = 0
        var run = 0
        var prev: Calendar? = null
        for (d in completedDates.sorted()) {
            val cal = Calendar.getInstance().apply { time = dateFormat.parse(d) ?: return@apply }
            run = if (prev != null && (prev.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                    .let { dateFormat.format(it.time) == d }) run + 1 else 1
            longest = maxOf(longest, run)
            prev = cal
        }

        val plantStage = when {
            activeStreak >= 30 -> "Golden Tree"
            activeStreak >= 14 -> "Flourishing Plant"
            activeStreak >= 7 -> "Blooming Sprout"
            activeStreak >= 3 -> "Thriving Sprout"
            else -> "Seedling"
        }

        // Category / time-of-day counts
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

        // Category completion % over the last 7 days
        val weekDates = weeklyStats.map { it.dateString }.toSet()
        val categoryCompletion = habits.groupBy { it.category.ifBlank { "Other" } }.map { (category, list) ->
            val ids = list.map { it.id }.toSet()
            val done = completedLogs.count { it.habitId in ids && it.date in weekDates }
            // Only count days each habit was actually scheduled
            val scheduled = weeklyStats.sumOf { stat ->
                val dow = dayOfWeekOf(stat.dateString)
                list.count { it.isScheduledOn(dow) }
            }
            category to (if (scheduled == 0) 0 else done * 100 / scheduled).coerceIn(0, 100)
        }.sortedByDescending { it.second }

        // Time invested: minutes logged on minute-based habits over the last 12 weeks
        val twelveWeeksAgo = dateFormat.format(Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -12) }.time)
        val minuteHabits = habits.filter { it.unit.equals("mins", true) || it.unit.equals("minutes", true) }
            .associateBy { it.id }
        val minutesByHabit = liveLogs
            .filter { it.habitId in minuteHabits && it.date >= twelveWeeksAgo }
            .groupBy { it.habitId }
            .map { (id, logs) -> minuteHabits.getValue(id).title to logs.sumOf { it.progressValue } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        return AnalyticsData(
            totalHabits = habits.size,
            overallCompletionRate = overallRate,
            currentPlantStage = plantStage,
            activeStreakDays = activeStreak,
            longestStreakDays = maxOf(longest, activeStreak),
            weeklyStats = weeklyStats,
            categoryStats = categoryStats,
            timeOfDayStats = timeStats,
            totalHabitsChecked = completedLogs.size,
            totalTimeInvestedMinutes = minutesByHabit.sumOf { it.second },
            timeInvestedBreakdown = minutesByHabit.take(5),
            moodGoodPercentage = moodGood,
            moodTotalDays = recentMoods.size,
            categoryCompletion = categoryCompletion
        )
    }
}
