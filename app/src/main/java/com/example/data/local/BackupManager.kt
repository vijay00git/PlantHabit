package com.example.data.local

import android.content.Context
import android.util.Base64
import androidx.room.withTransaction
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.MoodLogEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Full backup of everything the user made: habits, check-ins, moods, challenges (with
 * progress), banner photos and the theme setting — as one JSON file they can keep anywhere.
 */
object BackupManager {
    private const val FORMAT = "planthabit-backup"
    private const val VERSION = 1
    private const val BANNER_DIR = "challenge_banners"

    data class Summary(
        val exportedAt: Long,
        val habits: Int,
        val checkIns: Int,
        val moods: Int,
        val challenges: Int,
        val photos: Int,
    )

    /* ------------------------------ export ------------------------------ */

    suspend fun export(context: Context, db: SproutDatabase, themeMode: String, out: OutputStream): Summary {
        val dao = db.habitDao()
        val habits = dao.getAllHabitsIncludingArchived()
        val logs = dao.getAllLogs()
        val moods = dao.getAllMoodLogs()
        val challenges = dao.getAllChallengesList()

        val banners = JSONObject()
        File(context.filesDir, BANNER_DIR).listFiles()?.forEach { f ->
            banners.put(f.name, Base64.encodeToString(f.readBytes(), Base64.NO_WRAP))
        }

        val root = JSONObject().apply {
            put("format", FORMAT)
            put("version", VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("settings", JSONObject().put("themeMode", themeMode))
            put("habits", JSONArray(habits.map { it.toJson() }))
            put("logs", JSONArray(logs.map { it.toJson() }))
            put("moods", JSONArray(moods.map { it.toJson() }))
            put("challenges", JSONArray(challenges.map { it.toJson() }))
            put("banners", banners)
        }
        out.bufferedWriter().use { it.write(root.toString()) }
        return Summary(root.getLong("exportedAt"), habits.size, logs.count { it.isCompleted }, moods.size, challenges.size, banners.length())
    }

    /* ------------------------------ import ------------------------------ */

    class Parsed internal constructor(internal val root: JSONObject, val summary: Summary, val themeMode: String?)

    /** Reads and validates a backup without changing anything. */
    fun parse(input: InputStream): Parsed {
        val root = JSONObject(input.bufferedReader().use { it.readText() })
        require(root.optString("format") == FORMAT) { "This isn't a PlantHabit backup file" }
        require(root.optInt("version") in 1..VERSION) { "This backup was made by a newer version of PlantHabit" }
        val logs = root.optJSONArray("logs") ?: JSONArray()
        val completed = (0 until logs.length()).count { logs.getJSONObject(it).optBoolean("isCompleted") }
        val summary = Summary(
            exportedAt = root.optLong("exportedAt"),
            habits = root.optJSONArray("habits")?.length() ?: 0,
            checkIns = completed,
            moods = root.optJSONArray("moods")?.length() ?: 0,
            challenges = root.optJSONArray("challenges")?.length() ?: 0,
            photos = root.optJSONObject("banners")?.length() ?: 0,
        )
        return Parsed(root, summary, root.optJSONObject("settings")?.optString("themeMode")?.ifBlank { null })
    }

    /**
     * Replaces all current data with the backup. Calendar event ids are dropped (they belong to
     * the device the backup came from); the caller re-syncs calendar events afterwards.
     */
    suspend fun restore(context: Context, db: SproutDatabase, parsed: Parsed) {
        val root = parsed.root
        val dao = db.habitDao()

        // Banner photos first, so challenge rows can point at the restored files
        val bannerDir = File(context.filesDir, BANNER_DIR).apply { mkdirs() }
        val restoredBanners = mutableMapOf<String, String>()
        root.optJSONObject("banners")?.let { banners ->
            banners.keys().forEach { name ->
                val safe = File(name).name // never allow paths outside our folder
                val file = File(bannerDir, safe)
                file.writeBytes(Base64.decode(banners.getString(name), Base64.NO_WRAP))
                restoredBanners[safe] = file.absolutePath
            }
        }

        val habits = root.optJSONArray("habits").objects().map { habitFromJson(it) }
        val logs = root.optJSONArray("logs").objects().map { logFromJson(it) }
        val moods = root.optJSONArray("moods").objects().map { moodFromJson(it) }
        val challenges = root.optJSONArray("challenges").objects().map { challengeFromJson(it) }.map { ch ->
            val ref = ch.bannerResName
            if (BannerStorage.isCustom(ref)) ch.copy(bannerResName = restoredBanners[File(ref!!).name]) else ch
        }

        db.withTransaction {
            dao.deleteAllLogs()
            dao.deleteAllHabits()
            dao.deleteAllMoodLogs()
            dao.deleteAllChallenges()
            habits.forEach { dao.insertHabit(it) }
            logs.forEach { dao.insertOrUpdateLog(it) }
            moods.forEach { dao.insertMoodLog(it) }
            dao.insertChallenges(challenges)
        }
        // Built-in challenges missing from an older backup come back automatically
        BuiltInChallenges.sync(dao)
    }

    /* ------------------------------ JSON mapping ------------------------------ */

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.optStringOrNull(key: String): String? = if (isNull(key) || !has(key)) null else getString(key)
    private fun JSONObject.optLongOrNull(key: String): Long? = if (isNull(key) || !has(key)) null else getLong(key)

    private fun HabitEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("description", description); put("category", category)
        put("timeOfDay", timeOfDay); put("scheduledTime", scheduledTime); put("targetValue", targetValue)
        put("unit", unit); put("iconName", iconName); put("colorHex", colorHex)
        put("routineGroup", routineGroup ?: JSONObject.NULL); put("challengeId", challengeId ?: JSONObject.NULL)
        put("reminderEnabled", reminderEnabled); put("reminderTime", reminderTime); put("reminderDays", reminderDays)
        put("syncWithGoogleCalendar", syncWithGoogleCalendar); put("isArchived", isArchived); put("createdAt", createdAt)
    }

    private fun habitFromJson(o: JSONObject) = HabitEntity(
        id = o.getLong("id"),
        title = o.getString("title"),
        description = o.optString("description"),
        category = o.optString("category", "Health"),
        timeOfDay = o.optString("timeOfDay", "MORNING"),
        scheduledTime = o.optString("scheduledTime", "08:00"),
        targetValue = o.optInt("targetValue", 1),
        unit = o.optString("unit", "tasks"),
        iconName = o.optString("iconName", "sparkle"),
        colorHex = o.optString("colorHex", "#1FA65A"),
        routineGroup = o.optStringOrNull("routineGroup"),
        challengeId = o.optStringOrNull("challengeId"),
        reminderEnabled = o.optBoolean("reminderEnabled", true),
        reminderTime = o.optString("reminderTime", "08:00"),
        reminderDays = o.optString("reminderDays", "1,2,3,4,5,6,7"),
        syncWithGoogleCalendar = o.optBoolean("syncWithGoogleCalendar", false),
        calendarEventId = null,
        isArchived = o.optBoolean("isArchived", false),
        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
    )

    private fun HabitLogEntity.toJson() = JSONObject().apply {
        put("habitId", habitId); put("date", date); put("progressValue", progressValue)
        put("isCompleted", isCompleted); put("completedAt", completedAt ?: JSONObject.NULL)
        put("notes", notes ?: JSONObject.NULL); put("previousProgress", previousProgress)
    }

    private fun logFromJson(o: JSONObject) = HabitLogEntity(
        habitId = o.getLong("habitId"),
        date = o.getString("date"),
        progressValue = o.optInt("progressValue"),
        isCompleted = o.optBoolean("isCompleted"),
        completedAt = o.optLongOrNull("completedAt"),
        notes = o.optStringOrNull("notes"),
        previousProgress = o.optInt("previousProgress"),
    )

    private fun MoodLogEntity.toJson() = JSONObject().apply {
        put("date", date); put("moodScore", moodScore); put("moodLabel", moodLabel); put("note", note ?: JSONObject.NULL)
    }

    private fun moodFromJson(o: JSONObject) = MoodLogEntity(
        date = o.getString("date"),
        moodScore = o.optInt("moodScore", 3),
        moodLabel = o.optString("moodLabel", "Okay"),
        note = o.optStringOrNull("note"),
    )

    private fun ChallengeEntity.toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("subtitle", subtitle); put("description", description)
        put("durationDays", durationDays); put("currentDay", currentDay); put("skipsLeft", skipsLeft)
        put("isJoined", isJoined); put("category", category); put("bannerResName", bannerResName ?: JSONObject.NULL)
        put("totalTasks", totalTasks); put("completedTasksToday", completedTasksToday); put("dailyTip", dailyTip)
        put("tasksJson", tasksJson); put("goalTarget", goalTarget.toDouble()); put("goalCurrent", goalCurrent.toDouble())
        put("goalUnit", goalUnit); put("startedAt", startedAt); put("icon", icon); put("timesCompleted", timesCompleted)
    }

    private fun challengeFromJson(o: JSONObject) = ChallengeEntity(
        id = o.getString("id"),
        title = o.getString("title"),
        subtitle = o.optString("subtitle"),
        description = o.optString("description"),
        durationDays = o.optInt("durationDays", 7),
        currentDay = o.optInt("currentDay", 1),
        skipsLeft = o.optInt("skipsLeft", 1),
        isJoined = o.optBoolean("isJoined"),
        category = o.optString("category", "Mindfulness"),
        bannerResName = o.optStringOrNull("bannerResName"),
        totalTasks = o.optInt("totalTasks", 1),
        completedTasksToday = o.optInt("completedTasksToday"),
        dailyTip = o.optString("dailyTip"),
        tasksJson = o.optString("tasksJson"),
        goalTarget = o.optDouble("goalTarget", 0.0).toFloat(),
        goalCurrent = o.optDouble("goalCurrent", 0.0).toFloat(),
        goalUnit = o.optString("goalUnit"),
        startedAt = o.optLong("startedAt"),
        icon = o.optString("icon"),
        timesCompleted = o.optInt("timesCompleted"),
    )
}
