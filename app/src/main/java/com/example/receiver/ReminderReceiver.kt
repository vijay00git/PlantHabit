package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.SproutDatabase
import com.example.data.model.HabitLogEntity
import com.example.service.NotificationHelper
import com.example.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_HABIT_REMINDER = "com.aistudio.sprout.ACTION_HABIT_REMINDER"
        const val ACTION_MARK_DONE = "com.aistudio.sprout.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.aistudio.sprout.ACTION_SNOOZE"

        const val EXTRA_HABIT_ID = "EXTRA_HABIT_ID"
        const val EXTRA_HABIT_TITLE = "EXTRA_HABIT_TITLE"
        const val EXTRA_HABIT_DESC = "EXTRA_HABIT_DESC"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId == -1L) return

        when (intent.action) {
            ACTION_HABIT_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Habit Reminder"
                val desc = intent.getStringExtra(EXTRA_HABIT_DESC) ?: "Time for your habit!"
                val pending = goAsync()
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    try {
                        val db = SproutDatabase.getDatabase(context, scope)
                        val habit = db.habitDao().getHabitById(habitId)
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val doneToday = db.habitDao().getLogForHabitAndDate(habitId, today)?.isCompleted == true
                        // Only remind if the habit still exists and isn't already done today
                        if (habit == null || !doneToday) {
                            NotificationHelper.showReminderNotification(context, habitId, title, desc)
                        }
                        // Reschedule for the next scheduled day
                        if (habit != null && habit.reminderEnabled) {
                            ReminderScheduler.scheduleHabitReminder(context, habit)
                        }
                    } finally {
                        pending.finish()
                    }
                }
            }

            ACTION_MARK_DONE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(habitId.toInt())

                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val db = SproutDatabase.getDatabase(context, scope)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val habit = db.habitDao().getHabitById(habitId) ?: return@launch
                    val existingLog = db.habitDao().getLogForHabitAndDate(habitId, today)
                    if (existingLog?.isCompleted == true) return@launch
                    val updatedLog = existingLog?.copy(
                        progressValue = maxOf(habit.targetValue, existingLog.progressValue),
                        isCompleted = true,
                        completedAt = System.currentTimeMillis(),
                        previousProgress = existingLog.progressValue
                    ) ?: HabitLogEntity(
                        habitId = habitId,
                        date = today,
                        progressValue = habit.targetValue,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis()
                    )
                    db.habitDao().insertOrUpdateLog(updatedLog)
                }
            }

            ACTION_SNOOZE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(habitId.toInt())
                val title = intent.getStringExtra("EXTRA_TITLE") ?: "Habit Reminder"
                ReminderScheduler.scheduleSnooze(context, habitId, title, 15)
            }
        }
    }
}
