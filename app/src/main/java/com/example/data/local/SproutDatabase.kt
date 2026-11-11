package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.MoodLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        ChallengeEntity::class,
        MoodLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SproutDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: SproutDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SproutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SproutDatabase::class.java,
                    "sprout_habit_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.habitDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: HabitDao) {
                // Initial Habits matching screenshots
                val initialHabits = listOf(
                    HabitEntity(
                        title = "Rise and shine",
                        description = "Start your day with energy",
                        category = "Mindfulness",
                        timeOfDay = "MORNING",
                        scheduledTime = "07:00",
                        targetValue = 1,
                        unit = "tasks",
                        iconName = "sun",
                        colorHex = "#F59E0B",
                        routineGroup = "Happy Mornings",
                        reminderEnabled = true,
                        reminderTime = "07:00"
                    ),
                    HabitEntity(
                        title = "Get up at 7:00 am, no snooze",
                        description = "Jump out of bed promptly",
                        category = "Productivity",
                        timeOfDay = "MORNING",
                        scheduledTime = "07:00",
                        targetValue = 1,
                        unit = "tasks",
                        iconName = "alarm",
                        colorHex = "#8B5CF6",
                        routineGroup = "Happy Mornings",
                        reminderEnabled = true,
                        reminderTime = "07:00"
                    ),
                    HabitEntity(
                        title = "Drink a glass of water when you wake",
                        description = "Hydrate your body first thing",
                        category = "Health",
                        timeOfDay = "MORNING",
                        scheduledTime = "07:15",
                        targetValue = 1,
                        unit = "tasks",
                        iconName = "sparkle",
                        colorHex = "#F59E0B",
                        routineGroup = "Happy Mornings",
                        reminderEnabled = true,
                        reminderTime = "07:15"
                    ),
                    HabitEntity(
                        title = "Walk 8,000 Steps",
                        description = "Maintain daily physical mobility",
                        category = "Fitness",
                        timeOfDay = "MORNING",
                        scheduledTime = "08:00",
                        targetValue = 8000,
                        unit = "steps",
                        iconName = "walk",
                        colorHex = "#3B82F6",
                        reminderEnabled = true,
                        reminderTime = "08:00",
                        syncWithGoogleCalendar = true
                    ),
                    HabitEntity(
                        title = "Drink 3000 Glasses of Water",
                        description = "Reach optimal daily hydration",
                        category = "Health",
                        timeOfDay = "MORNING",
                        scheduledTime = "09:00",
                        targetValue = 3000,
                        unit = "ml",
                        iconName = "water",
                        colorHex = "#F59E0B",
                        reminderEnabled = true,
                        reminderTime = "09:00"
                    ),
                    HabitEntity(
                        title = "Wind down",
                        description = "Check in and plan tomorrow",
                        category = "Mindfulness",
                        timeOfDay = "EVENING",
                        scheduledTime = "23:00",
                        targetValue = 1,
                        unit = "tasks",
                        iconName = "moon",
                        colorHex = "#6366F1",
                        reminderEnabled = true,
                        reminderTime = "23:00",
                        syncWithGoogleCalendar = true
                    )
                )

                for (habit in initialHabits) {
                    dao.insertHabit(habit)
                }

                // Initial Challenges with rich tasks JSON
                val initialChallenges = listOf(
                    ChallengeEntity(
                        id = "gratitude_month",
                        title = "Gratitude Month",
                        subtitle = "23 days left in November",
                        description = "Cultivate daily thankfulness and positive perspective with intentional morning and evening reflections.",
                        durationDays = 30,
                        currentDay = 7,
                        skipsLeft = 3,
                        isJoined = true,
                        category = "Mindfulness",
                        bannerResName = "challenge_gratitude",
                        totalTasks = 3,
                        completedTasksToday = 1,
                        dailyTip = "Reflecting on gratitude triggers dopamine and serotonin, increasing mental resilience and mood stability by 23%.",
                        tasksJson = """[
                            {"title":"Morning Gratitude Reflection","description":"Write 3 specific things you appreciate today","timeOfDay":"MORNING","scheduledTime":"07:30","targetValue":1,"unit":"tasks","iconName":"sparkle","reminderEnabled":true,"reminderTime":"07:30","syncWithGoogleCalendar":true,"tip":"Write detailed reasons why you appreciate each thing."},
                            {"title":"Send Appreciation Note","description":"Reach out to someone with genuine thanks","timeOfDay":"AFTERNOON","scheduledTime":"14:00","targetValue":1,"unit":"tasks","iconName":"sun","reminderEnabled":true,"reminderTime":"14:00","syncWithGoogleCalendar":false,"tip":"Small unexpected compliments brighten someone's entire day."},
                            {"title":"Evening Win Journal","description":"Celebrate 1 personal win before sleeping","timeOfDay":"EVENING","scheduledTime":"21:45","targetValue":1,"unit":"tasks","iconName":"moon","reminderEnabled":true,"reminderTime":"21:45","syncWithGoogleCalendar":true,"tip":"Focus on personal effort rather than outcomes."}
                        ]"""
                    ),
                    ChallengeEntity(
                        id = "better_sleep",
                        title = "Better Sleep Reset",
                        subtitle = "Day 6 of 14 · 2 skips left",
                        description = "Reset your circadian rhythm and build deep restorative sleep habits with structured wind-down protocols.",
                        durationDays = 14,
                        currentDay = 6,
                        skipsLeft = 2,
                        isJoined = true,
                        category = "Health",
                        bannerResName = "routine_morning",
                        totalTasks = 3,
                        completedTasksToday = 1,
                        dailyTip = "A regular bedtime can support deeper REM sleep phases and sharper next-day mental acuity.",
                        tasksJson = """[
                            {"title":"Get into bed at your set time","description":"A regular bedtime can support better sleep","timeOfDay":"EVENING","scheduledTime":"22:30","targetValue":1,"unit":"tasks","iconName":"moon","reminderEnabled":true,"reminderTime":"22:30","syncWithGoogleCalendar":true,"tip":"Maintain within 15 minutes of your standard bedtime."},
                            {"title":"Start winding down at your alarm","description":"Thirty calm minutes before lights out","timeOfDay":"EVENING","scheduledTime":"22:00","targetValue":1,"unit":"tasks","iconName":"alarm","reminderEnabled":true,"reminderTime":"22:00","syncWithGoogleCalendar":true,"tip":"Dim overhead lights and switch to warm lamps."},
                            {"title":"Read paper instead of a screen","description":"A book or magazine. Ten pages is plenty.","timeOfDay":"EVENING","scheduledTime":"22:15","targetValue":10,"unit":"pages","iconName":"sparkle","reminderEnabled":true,"reminderTime":"22:15","syncWithGoogleCalendar":false,"tip":"Avoid blue light to let natural melatonin flow."}
                        ]"""
                    ),
                    ChallengeEntity(
                        id = "happy_mornings",
                        title = "Happy Mornings",
                        subtitle = "Day 1 of 7 · 1 skip left",
                        description = "Wake up energized with a consistent morning routine designed to set up your day for success.",
                        durationDays = 7,
                        currentDay = 1,
                        skipsLeft = 1,
                        isJoined = true,
                        category = "Routine",
                        bannerResName = "routine_morning",
                        totalTasks = 2,
                        completedTasksToday = 1,
                        dailyTip = "Early sunlight exposure synchronizes your master circadian clock.",
                        tasksJson = """[
                            {"title":"Get up at 7:00 am, no snooze","description":"Jump out of bed promptly","timeOfDay":"MORNING","scheduledTime":"07:00","targetValue":1,"unit":"tasks","iconName":"alarm","reminderEnabled":true,"reminderTime":"07:00","syncWithGoogleCalendar":true,"tip":"Place your alarm across the room so you have to stand up."},
                            {"title":"Drink a glass of water when you wake","description":"Hydrate your body first thing","timeOfDay":"MORNING","scheduledTime":"07:15","targetValue":1,"unit":"tasks","iconName":"water","reminderEnabled":true,"reminderTime":"07:15","syncWithGoogleCalendar":false,"tip":"Drinking 500ml water boosts metabolic rate by up to 30%."}
                        ]"""
                    ),
                    ChallengeEntity(
                        id = "goal_counter_run",
                        title = "Goal Counter: 100 km",
                        subtitle = "Day 21 of 70 · 49 days left",
                        description = "Reach 100 km step-by-step. Log your runs and walks to stay on track for your seasonal fitness target.",
                        durationDays = 70,
                        currentDay = 21,
                        skipsLeft = 5,
                        isJoined = true,
                        category = "Fitness",
                        bannerResName = "challenge_gratitude",
                        totalTasks = 1,
                        completedTasksToday = 0,
                        goalTarget = 100f,
                        goalCurrent = 38f,
                        goalUnit = "km",
                        dailyTip = "About 8.7 km a week to reach it right on schedule by Dec 8, 2026.",
                        tasksJson = """[
                            {"title":"Run or Walk Cardio Block","description":"Log distance toward your 100 km seasonal goal","timeOfDay":"MORNING","scheduledTime":"07:30","targetValue":5,"unit":"km","iconName":"walk","reminderEnabled":true,"reminderTime":"07:30","syncWithGoogleCalendar":true,"tip":"Consistent easy-pace cardio builds durable aerobic endurance."}
                        ]"""
                    ),
                    ChallengeEntity(
                        id = "relationship_week",
                        title = "Relationship Week",
                        subtitle = "7 days · Daily connection prompts",
                        description = "Strengthen bonds with loved ones through daily connection prompts and shared activities.",
                        durationDays = 7,
                        currentDay = 1,
                        skipsLeft = 2,
                        isJoined = false,
                        category = "People",
                        totalTasks = 2,
                        completedTasksToday = 0,
                        dailyTip = "Deep relational check-ins reduce cortisol and foster lasting emotional security.",
                        tasksJson = """[
                            {"title":"15-Min Focused Conversation","description":"Uninterrupted talk with a partner or close friend","timeOfDay":"EVENING","scheduledTime":"19:30","targetValue":15,"unit":"mins","iconName":"sun","reminderEnabled":true,"reminderTime":"19:30","syncWithGoogleCalendar":true,"tip":"Put phones in another room to give your full presence."},
                            {"title":"Express Unprompted Gratitude","description":"Send a heartfelt text acknowledging someone","timeOfDay":"AFTERNOON","scheduledTime":"13:00","targetValue":1,"unit":"tasks","iconName":"sparkle","reminderEnabled":true,"reminderTime":"13:00","syncWithGoogleCalendar":false,"tip":"Mention a specific action they took that helped you."}
                        ]"""
                    ),
                    ChallengeEntity(
                        id = "savings_jar",
                        title = "Savings Jar",
                        subtitle = "Day 25 of 60 · Target $1,850",
                        description = "Track small daily savings to build your emergency fund or reach your reward goal.",
                        durationDays = 60,
                        currentDay = 25,
                        skipsLeft = 5,
                        isJoined = true,
                        category = "Finance",
                        totalTasks = 1,
                        completedTasksToday = 0,
                        goalTarget = 1850f,
                        goalCurrent = 740f,
                        goalUnit = "$",
                        dailyTip = "Automating even $5 a day yields massive compound interest and financial peace of mind.",
                        tasksJson = """[
                            {"title":"Deposit Daily Micro-Savings","description":"Put aside daily savings into your high-yield jar","timeOfDay":"EVENING","scheduledTime":"20:00","targetValue":1,"unit":"tasks","iconName":"sparkle","reminderEnabled":true,"reminderTime":"20:00","syncWithGoogleCalendar":false,"tip":"Skipping one impulse buy pays for a week of savings."}
                        ]"""
                    )
                )
                dao.insertChallenges(initialChallenges)

                // Initial sample mood logs for the past 7 days (matching Screenshot 3: 57% Good or great, 6 of 7 days)
                val sampleMoods = listOf(
                    MoodLogEntity(date = "2026-11-04", moodScore = 5, moodLabel = "Great"),
                    MoodLogEntity(date = "2026-11-05", moodScore = 4, moodLabel = "Good"),
                    MoodLogEntity(date = "2026-11-06", moodScore = 5, moodLabel = "Great"),
                    MoodLogEntity(date = "2026-11-07", moodScore = 4, moodLabel = "Good"),
                    MoodLogEntity(date = "2026-11-08", moodScore = 4, moodLabel = "Good"),
                    MoodLogEntity(date = "2026-11-09", moodScore = 3, moodLabel = "Okay"),
                    MoodLogEntity(date = "2026-11-10", moodScore = 4, moodLabel = "Good")
                )
                for (m in sampleMoods) {
                    dao.insertMoodLog(m)
                }
            }
        }
    }
}
