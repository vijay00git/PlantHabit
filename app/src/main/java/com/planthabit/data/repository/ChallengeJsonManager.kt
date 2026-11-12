package com.planthabit.data.repository

import com.planthabit.data.model.ChallengeEntity
import com.planthabit.data.model.HabitEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ChallengeJsonManager {

    fun getExampleTemplateJson(): String {
        return """{
  "id": "mindfulness_deep_work",
  "title": "Mindful Productivity Sprint",
  "subtitle": "14 days to sharpen mental clarity and focus",
  "description": "Establish a bulletproof daily focus routine by eliminating digital distraction, staying hydrated, and doing a daily deep work block.",
  "durationDays": 14,
  "skipsLeft": 2,
  "category": "Productivity",
  "habits": [
    {
      "title": "Morning Meditation & Intention",
      "description": "5 minutes of breathwork before looking at screens",
      "category": "Mindfulness",
      "timeOfDay": "MORNING",
      "scheduledTime": "07:15",
      "targetValue": 5,
      "unit": "mins",
      "iconName": "sparkle",
      "colorHex": "#8B5CF6",
      "reminderEnabled": true,
      "reminderTime": "07:15",
      "syncWithGoogleCalendar": true
    },
    {
      "title": "Drink 2000 ml Hydration",
      "description": "Keep cognitive stamina high throughout the morning",
      "category": "Health",
      "timeOfDay": "MORNING",
      "scheduledTime": "09:00",
      "targetValue": 2000,
      "unit": "ml",
      "iconName": "water",
      "colorHex": "#3B82F6",
      "reminderEnabled": true,
      "reminderTime": "09:00",
      "syncWithGoogleCalendar": false
    },
    {
      "title": "90-Minute Unbroken Deep Work",
      "category": "Productivity",
      "timeOfDay": "AFTERNOON",
      "scheduledTime": "14:00",
      "targetValue": 90,
      "unit": "mins",
      "iconName": "sun",
      "colorHex": "#F59E0B",
      "reminderEnabled": true,
      "reminderTime": "14:00",
      "syncWithGoogleCalendar": true
    }
  ]
}"""
    }

    fun parseChallengeJson(jsonString: String): Result<Pair<ChallengeEntity, List<HabitEntity>>> {
        return try {
            val trimmed = jsonString.trim()
            val root = JSONObject(trimmed)

            val rawId = root.optString("id").ifEmpty { "custom_${UUID.randomUUID().toString().take(8)}" }
            val title = root.getString("title").trim()
            if (title.isEmpty()) {
                return Result.failure(IllegalArgumentException("Challenge title cannot be empty"))
            }

            val subtitle = root.optString("subtitle", "${root.optInt("durationDays", 7)} days challenge")
            val description = root.optString("description", "Custom imported challenge")
            val durationDays = root.optInt("durationDays", 7).coerceAtLeast(1)
            val skipsLeft = root.optInt("skipsLeft", 2).coerceAtLeast(0)
            val category = root.optString("category", "Productivity")

            val habitsList = mutableListOf<HabitEntity>()
            val habitsArray = root.optJSONArray("habits")
            if (habitsArray != null) {
                for (i in 0 until habitsArray.length()) {
                    val habitObj = habitsArray.getJSONObject(i)
                    val habitTitle = habitObj.optString("title", "Challenge Habit ${i + 1}")
                    val habitDesc = habitObj.optString("description", "")
                    val habitCat = habitObj.optString("category", category)
                    val timeOfDay = habitObj.optString("timeOfDay", "MORNING")
                    val scheduledTime = habitObj.optString("scheduledTime", "08:00")
                    val targetValue = habitObj.optInt("targetValue", 1).coerceAtLeast(1)
                    val unit = habitObj.optString("unit", "tasks")
                    val iconName = habitObj.optString("iconName", "sparkle")
                    val colorHex = habitObj.optString("colorHex", "#7C3AED")
                    val reminderEnabled = habitObj.optBoolean("reminderEnabled", true)
                    val reminderTime = habitObj.optString("reminderTime", scheduledTime)
                    val syncCal = habitObj.optBoolean("syncWithGoogleCalendar", true)

                    habitsList.add(
                        HabitEntity(
                            title = habitTitle,
                            description = habitDesc,
                            category = habitCat,
                            timeOfDay = timeOfDay,
                            scheduledTime = scheduledTime,
                            targetValue = targetValue,
                            unit = unit,
                            iconName = iconName,
                            colorHex = colorHex,
                            routineGroup = title,
                            challengeId = rawId,
                            reminderEnabled = reminderEnabled,
                            reminderTime = reminderTime,
                            syncWithGoogleCalendar = syncCal
                        )
                    )
                }
            }

            val totalTasks = if (habitsList.isNotEmpty()) habitsList.size else root.optInt("totalTasks", 2)

            val challenge = ChallengeEntity(
                id = rawId,
                title = title,
                subtitle = subtitle,
                description = description,
                durationDays = durationDays,
                currentDay = 1,
                skipsLeft = skipsLeft,
                isJoined = true, // Auto-joined upon import so user can use immediately!
                category = category,
                bannerResName = null,
                totalTasks = totalTasks,
                completedTasksToday = 0
            )

            Result.success(Pair(challenge, habitsList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportChallengeJson(challenge: ChallengeEntity, habits: List<HabitEntity>): String {
        val root = JSONObject()
        root.put("id", challenge.id)
        root.put("title", challenge.title)
        root.put("subtitle", challenge.subtitle)
        root.put("description", challenge.description)
        root.put("durationDays", challenge.durationDays)
        root.put("skipsLeft", challenge.skipsLeft)
        root.put("category", challenge.category)

        val habitsArray = JSONArray()
        habits.forEach { habit ->
            val h = JSONObject()
            h.put("title", habit.title)
            h.put("description", habit.description)
            h.put("category", habit.category)
            h.put("timeOfDay", habit.timeOfDay)
            h.put("scheduledTime", habit.scheduledTime)
            h.put("targetValue", habit.targetValue)
            h.put("unit", habit.unit)
            h.put("iconName", habit.iconName)
            h.put("colorHex", habit.colorHex)
            h.put("reminderEnabled", habit.reminderEnabled)
            h.put("reminderTime", habit.reminderTime)
            h.put("syncWithGoogleCalendar", habit.syncWithGoogleCalendar)
            habitsArray.put(h)
        }
        root.put("habits", habitsArray)
        return root.toString(2)
    }
}
