package com.planthabit.service

import com.planthabit.data.model.isScheduledOn
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.planthabit.data.model.HabitEntity
import com.planthabit.receiver.ReminderReceiver
import java.util.Calendar

object ReminderScheduler {

    /** All reminder times for a habit: the main one plus any extra "HH:mm" times. */
    fun reminderTimes(habit: HabitEntity): List<String> =
        (listOf(habit.reminderTime) + habit.extraReminderTimes.split(","))
            .map { it.trim() }
            .filter { Regex("^\\d{1,2}:\\d{2}$").matches(it) }
            .distinct()
            .take(MAX_REMINDERS)

    private const val MAX_REMINDERS = 8

    /** Request code per reminder slot; slot 0 keeps the original id-based code. */
    private fun requestCode(habitId: Long, slot: Int): Int =
        if (slot == 0) habitId.toInt() else (1_000_000 + habitId * MAX_REMINDERS + slot).toInt()

    fun scheduleHabitReminder(context: Context, habit: HabitEntity) {
        cancelHabitReminder(context, habit.id)
        if (!habit.reminderEnabled) return
        reminderTimes(habit).forEachIndexed { slot, time ->
            scheduleAt(context, habit, time, requestCode(habit.id, slot))
        }
    }

    private fun scheduleAt(context: Context, habit: HabitEntity, time: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            // Skip forward to the next day this habit is scheduled on
            var guard = 0
            while (!habit.isScheduledOn(get(Calendar.DAY_OF_WEEK)) && guard++ < 7) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_HABIT_REMINDER
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderReceiver.EXTRA_HABIT_TITLE, habit.title)
            putExtra(ReminderReceiver.EXTRA_HABIT_DESC, habit.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for permission restrictions
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleSnooze(context: Context, habitId: Long, title: String, minutes: Int = 15) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_HABIT_REMINDER
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(ReminderReceiver.EXTRA_HABIT_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_HABIT_DESC, "Snoozed reminder 🌱")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId + 99999).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelHabitReminder(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_HABIT_REMINDER
        }
        for (slot in 0 until MAX_REMINDERS) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(habitId, slot),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
