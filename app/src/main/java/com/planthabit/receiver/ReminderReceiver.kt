package com.planthabit.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planthabit.data.local.PlantHabitDatabase
import com.planthabit.data.model.HabitLogEntity
import com.planthabit.service.NotificationHelper
import com.planthabit.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_HABIT_REMINDER = "com.planthabit.ACTION_HABIT_REMINDER"
        const val ACTION_MARK_DONE = "com.planthabit.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.planthabit.ACTION_SNOOZE"

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
                        val db = PlantHabitDatabase.getDatabase(context, scope)
                        val habit = db.habitDao().getHabitById(habitId)
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        var doneToday = db.habitDao().getLogForHabitAndDate(habitId, today)?.isCompleted == true
                        // Weekly habits: no nagging once this week's goal is met
                        if (habit != null && habit.timesPerWeek > 0) {
                            val week = com.planthabit.data.model.weekDates(com.planthabit.data.model.weekStartOf(today)).toSet()
                            val done = db.habitDao().getLogsForHabit(habitId).count { it.isCompleted && it.date in week }
                            if (done >= habit.timesPerWeek) doneToday = true
                        }
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
                    val db = PlantHabitDatabase.getDatabase(context, scope)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val habit = db.habitDao().getHabitById(habitId) ?: return@launch
                    val existingLog = db.habitDao().getLogForHabitAndDate(habitId, today)
                    if (existingLog?.isCompleted == true) return@launch
                    // Same logic as the app (restores/links goal progress, refreshes widget)
                    com.planthabit.data.repository.HabitRepository(
                        context.applicationContext, db.habitDao(), com.planthabit.data.local.PlantHabitPreferences(context.applicationContext)
                    ).toggleHabitCompleted(habit, today)
                }
            }

            ACTION_SNOOZE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(habitId.toInt())
                val title = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Habit Reminder"
                ReminderScheduler.scheduleSnooze(context, habitId, title, 15)
            }
        }
    }
}
