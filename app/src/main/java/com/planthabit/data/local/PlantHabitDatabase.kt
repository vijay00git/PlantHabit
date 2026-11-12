package com.planthabit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.planthabit.data.model.ChallengeEntity
import com.planthabit.data.model.HabitEntity
import com.planthabit.data.model.HabitLogEntity
import com.planthabit.data.model.MoodLogEntity
import com.planthabit.data.model.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        ChallengeEntity::class,
        MoodLogEntity::class,
        AchievementEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class PlantHabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: PlantHabitDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PlantHabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantHabitDatabase::class.java,
                    "planthabit.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    // Only the very first prototype schema (v1) may be rebuilt; every later
                    // version must ship a Migration so user data is never silently wiped.
                    .fallbackToDestructiveMigrationFrom(true, 1)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** v3: challenges get a real start date and their own icon. Keeps all user data. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE challenges ADD COLUMN startedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE challenges ADD COLUMN icon TEXT NOT NULL DEFAULT ''")
            }
        }

        /** v4: undo restores progress; challenges count completions. Keeps all user data. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habit_logs ADD COLUMN previousProgress INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE challenges ADD COLUMN timesCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v5: favourite, custom group and hidden flags for challenges. Keeps all user data. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE challenges ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE challenges ADD COLUMN groupName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE challenges ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v6: habit order, weekly goals, extra reminders, skip days, achievements. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN timesPerWeek INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN extraReminderTimes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE habit_logs ADD COLUMN isSkipped INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`id` TEXT NOT NULL, `unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
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

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Add new built-in challenges / refresh their content on every launch.
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) { BuiltInChallenges.sync(database.habitDao()) }
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

                // Built-in challenges are added by BuiltInChallenges.sync() in onOpen.

                // Sample mood check-ins for the past 7 days (relative to today)
                val moodScores = listOf(5 to "Great", 4 to "Good", 5 to "Great", 4 to "Good", 4 to "Good", 3 to "Okay", 4 to "Good")
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val sampleMoods = moodScores.mapIndexed { i, (score, label) ->
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -(7 - i)) }
                    MoodLogEntity(date = fmt.format(cal.time), moodScore = score, moodLabel = label)
                }
                for (m in sampleMoods) {
                    dao.insertMoodLog(m)
                }
            }
        }
    }
}
