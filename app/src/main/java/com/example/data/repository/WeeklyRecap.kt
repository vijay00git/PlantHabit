package com.example.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.HabitDao
import com.example.data.local.SproutDatabase
import com.example.data.model.isScheduledOn
import com.example.data.model.weekDates
import com.example.data.model.weekStartOf
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Summary of one week (Mon–Sun). */
data class WeeklyRecap(
    val weekStart: String,
    val checkIns: Int,
    val scheduled: Int,
    val completionPercent: Int,
    val bestHabit: String?,
    val bestHabitCount: Int,
    val averageMood: Double?,
    val lastWeekPercent: Int,
) {
    val deltaVsLastWeek: Int get() = completionPercent - lastWeekPercent

    companion object {
        private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        suspend fun compute(dao: HabitDao, anyDateInWeek: String = fmt.format(Date())): WeeklyRecap {
            val start = weekStartOf(anyDateInWeek)
            val lastStart = fmt.format(Calendar.getInstance().apply { time = fmt.parse(start)!!; add(Calendar.DAY_OF_YEAR, -7) }.time)
            val habits = dao.getAllHabitsList()
            val logs = dao.getAllLogs().filter { it.isCompleted }
            val moods = dao.getAllMoodLogs()
            val today = fmt.format(Date())

            fun percent(weekStart: String): Pair<Int, Int> {
                val dates = weekDates(weekStart).filter { it <= today }
                var scheduled = 0
                for (d in dates) {
                    val dow = Calendar.getInstance().apply { time = fmt.parse(d)!! }.get(Calendar.DAY_OF_WEEK)
                    scheduled += habits.count { h ->
                        h.timesPerWeek == 0 && h.isScheduledOn(dow) && fmt.format(Date(h.createdAt)) <= d
                    }
                }
                // Weekly-goal habits count their goal once per week
                scheduled += habits.filter { it.timesPerWeek > 0 }.sumOf { it.timesPerWeek }
                val done = logs.count { it.date in dates.toSet() }
                return done to scheduled
            }

            val week = weekDates(start).toSet()
            val (done, scheduled) = percent(start)
            val (lastDone, lastScheduled) = percent(lastStart)
            val byHabit = logs.filter { it.date in week }.groupingBy { it.habitId }.eachCount()
            val best = byHabit.maxByOrNull { it.value }
            val weekMoods = moods.filter { it.date in week }
            return WeeklyRecap(
                weekStart = start,
                checkIns = done,
                scheduled = scheduled,
                completionPercent = if (scheduled == 0) 0 else (done * 100 / scheduled).coerceAtMost(100),
                bestHabit = best?.let { b -> habits.firstOrNull { it.id == b.key }?.title },
                bestHabitCount = best?.value ?: 0,
                averageMood = if (weekMoods.isEmpty()) null else weekMoods.map { it.moodScore }.average(),
                lastWeekPercent = if (lastScheduled == 0) 0 else (lastDone * 100 / lastScheduled).coerceAtMost(100),
            )
        }
    }
}

/** Schedules and posts the Sunday-evening recap notification. */
object RecapScheduler {
    private const val ACTION = "com.planthabit.ACTION_WEEKLY_RECAP"
    private const val REQUEST = 777_001
    private const val NOTIFICATION_ID = 777_001

    /** Next Sunday at 19:00 (today if it's Sunday before 19:00). */
    fun schedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST,
            Intent(context, RecapReceiver::class.java).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Inexact is fine for a weekly summary and needs no exact-alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
        } else {
            am.set(AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
        }
    }

    fun post(context: Context, recap: WeeklyRecap) {
        val mood = recap.averageMood?.let { avg ->
            " · mood " + when { avg >= 4.5 -> "😄"; avg >= 3.5 -> "🙂"; avg >= 2.5 -> "😐"; avg >= 1.5 -> "🙁"; else -> "😣" }
        }.orEmpty()
        val trend = when {
            recap.deltaVsLastWeek > 0 -> "▲ ${recap.deltaVsLastWeek}% vs last week"
            recap.deltaVsLastWeek < 0 -> "▼ ${-recap.deltaVsLastWeek}% vs last week"
            else -> "same as last week"
        }
        val best = recap.bestHabit?.let { "\nStar habit: $it (${recap.bestHabitCount}×)" }.orEmpty()
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_stat_plant)
            .setContentTitle("Your week: ${recap.completionPercent}% done 🌱")
            .setContentText("${recap.checkIns} check-ins · $trend$mood")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${recap.checkIns} check-ins · $trend$mood$best"))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notifications not allowed — nothing to do
        }
    }
}

class RecapReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val dao = SproutDatabase.getDatabase(context, scope).habitDao()
                val recap = WeeklyRecap.compute(dao)
                if (recap.scheduled > 0) RecapScheduler.post(context, recap)
                RecapScheduler.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
