package com.planthabit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planthabit.data.model.ChallengeEntity
import com.planthabit.data.model.HabitEntity
import com.planthabit.data.model.HabitLogEntity
import com.planthabit.data.model.MoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // Habits
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, scheduledTime ASC")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, scheduledTime ASC")
    suspend fun getAllHabitsList(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY title")
    fun getArchivedHabitsFlow(): Flow<List<HabitEntity>>

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAchievementsFlow(): Flow<List<com.planthabit.data.model.AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAchievements(): List<com.planthabit.data.model.AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(a: com.planthabit.data.model.AchievementEntity)

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE challengeId = :challengeId")
    suspend fun getHabitsForChallenge(challengeId: String): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habits WHERE challengeId = :challengeId")
    suspend fun deleteHabitsForChallenge(challengeId: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)

    // Logs
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDateFlow(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForHabitAndDate(habitId: Long, date: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getLogsForHabit(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabitFlow(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    suspend fun getAllLogs(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity): Long

    // Challenges
    @Query("SELECT * FROM challenges")
    fun getAllChallengesFlow(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallenge(id: String)

    // Reset ("clear my data") — keeps challenge definitions, wipes everything the user logged
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM mood_logs")
    suspend fun deleteAllMoodLogs()

    @Query("UPDATE challenges SET isJoined = 0, currentDay = 1, completedTasksToday = 0, goalCurrent = 0, startedAt = 0, timesCompleted = 0")
    suspend fun resetAllChallengeProgress()

    @Query("SELECT * FROM challenges")
    suspend fun getAllChallengesList(): List<ChallengeEntity>

    @Query("DELETE FROM challenges")
    suspend fun deleteAllChallenges()

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsIncludingArchived(): List<HabitEntity>

    @Query("SELECT * FROM mood_logs ORDER BY date DESC")
    suspend fun getAllMoodLogs(): List<MoodLogEntity>

    // Mood Tracking
    @Query("SELECT * FROM mood_logs ORDER BY date DESC")
    fun getAllMoodLogsFlow(): Flow<List<MoodLogEntity>>

    @Query("SELECT * FROM mood_logs WHERE date = :date LIMIT 1")
    suspend fun getMoodLogForDate(date: String): MoodLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(moodLog: MoodLogEntity)
}
