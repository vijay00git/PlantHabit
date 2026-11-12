package com.planthabit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.planthabit.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "sprout_habit_reminders"
    const val CHANNEL_NAME = "Habit Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and streak notifications for PlantHabit habits"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        habitId: Long,
        title: String,
        description: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_HABIT_ID", habitId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done Action
        val doneIntent = Intent(context, com.planthabit.receiver.ReminderReceiver::class.java).apply {
            action = com.planthabit.receiver.ReminderReceiver.ACTION_MARK_DONE
            putExtra(com.planthabit.receiver.ReminderReceiver.EXTRA_HABIT_ID, habitId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 15m Action
        val snoozeIntent = Intent(context, com.planthabit.receiver.ReminderReceiver::class.java).apply {
            action = com.planthabit.receiver.ReminderReceiver.ACTION_SNOOZE
            putExtra(com.planthabit.receiver.ReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(com.planthabit.receiver.ReminderReceiver.EXTRA_HABIT_TITLE, title)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.planthabit.R.drawable.ic_stat_plant)
            .setContentTitle(title)
            .setContentText(if (description.isNotBlank()) description else "Time to keep your streak alive! 🌱")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 15m", snoozePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(habitId.toInt(), notification)
    }
}
