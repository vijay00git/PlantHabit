package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.model.AchievementEntity
import com.example.data.model.isScheduledOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** A badge the user can earn. Definitions live here; unlocks are stored in the database. */
data class Achievement(val id: String, val emoji: String, val title: String, val description: String)

object Achievements {

    val all = listOf(
        Achievement("first_habit", "🌱", "First seed", "Plant your first habit"),
        Achievement("first_check", "✅", "First check-in", "Complete a habit for the first time"),
        Achievement("streak_3", "🔥", "Spark", "Reach a 3-day streak"),
        Achievement("streak_7", "🌿", "Sprouting", "Reach a 7-day streak"),
        Achievement("streak_14", "🌸", "In bloom", "Reach a 14-day streak"),
        Achievement("streak_30", "🌳", "Deep roots", "Reach a 30-day streak"),
        Achievement("checkins_10", "🎯", "Getting going", "10 habit check-ins"),
        Achievement("checkins_50", "💪", "Habit builder", "50 habit check-ins"),
        Achievement("checkins_100", "🏅", "Centurion", "100 habit check-ins"),
        Achievement("checkins_500", "👑", "Unstoppable", "500 habit check-ins"),
        Achievement("perfect_day", "⭐", "Perfect day", "Finish every habit on a day with 3 or more"),
        Achievement("challenge_joined", "🚀", "Challenger", "Join a challenge"),
        Achievement("challenge_done", "🏆", "Finisher", "Complete a challenge"),
        Achievement("mood_7", "😊", "Self-aware", "Log your mood on 7 days"),
        Achievement("early_bird", "🌅", "Early bird", "Complete a habit before 7 am"),
    )

    fun byId(id: String) = all.firstOrNull { it.id == id }

    /** Checks every badge against current data, stores new unlocks and returns them. */
    suspend fun evaluate(dao: HabitDao): List<Achievement> {
        val unlocked = dao.getAchievements().map { it.id }.toSet()
        val habits = dao.getAllHabitsIncludingArchived()
        val logs = dao.getAllLogs()
        val completed = logs.filter { it.isCompleted }
        val challenges = dao.getAllChallengesList()
        val moods = dao.getAllMoodLogs()

        // Longest run of consecutive days with at least one completion
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dates = completed.map { it.date }.toSortedSet()
        var longest = 0
        var run = 0
        var prev: String? = null
        for (d in dates) {
            val expected = prev?.let { p ->
                Calendar.getInstance().apply { time = fmt.parse(p)!!; add(Calendar.DAY_OF_YEAR, 1) }.let { fmt.format(it.time) }
            }
            run = if (expected == d) run + 1 else 1
            longest = maxOf(longest, run)
            prev = d
        }

        // A day where every scheduled habit (3+) was completed
        val activeHabits = habits.filter { !it.isArchived }
        val perfectDay = completed.groupBy { it.date }.any { (date, dayLogs) ->
            val dow = Calendar.getInstance().apply { time = fmt.parse(date)!! }.get(Calendar.DAY_OF_WEEK)
            val scheduled = activeHabits.filter { it.isScheduledOn(dow) && it.timesPerWeek == 0 && fmt.format(java.util.Date(it.createdAt)) <= date }
            scheduled.size >= 3 && scheduled.all { h -> dayLogs.any { it.habitId == h.id } }
        }

        val earlyBird = completed.any { log ->
            log.completedAt?.let { Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.HOUR_OF_DAY) < 7 } == true
        }

        val earned = buildSet {
            if (habits.isNotEmpty()) add("first_habit")
            if (completed.isNotEmpty()) add("first_check")
            if (longest >= 3) add("streak_3")
            if (longest >= 7) add("streak_7")
            if (longest >= 14) add("streak_14")
            if (longest >= 30) add("streak_30")
            if (completed.size >= 10) add("checkins_10")
            if (completed.size >= 50) add("checkins_50")
            if (completed.size >= 100) add("checkins_100")
            if (completed.size >= 500) add("checkins_500")
            if (perfectDay) add("perfect_day")
            if (challenges.any { it.isJoined || it.timesCompleted > 0 }) add("challenge_joined")
            if (challenges.any { it.timesCompleted > 0 }) add("challenge_done")
            if (moods.size >= 7) add("mood_7")
            if (earlyBird) add("early_bird")
        }

        val now = System.currentTimeMillis()
        return (earned - unlocked).mapNotNull { id ->
            dao.insertAchievement(AchievementEntity(id, now))
            byId(id)
        }
    }
}
