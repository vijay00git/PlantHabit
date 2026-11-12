package com.planthabit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planthabit.data.local.PlantHabitDatabase
import com.planthabit.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = PlantHabitDatabase.getDatabase(context, scope)
                val habits = db.habitDao().getAllHabitsList()
                habits.filter { it.reminderEnabled }.forEach { habit ->
                    ReminderScheduler.scheduleHabitReminder(context, habit)
                }
                com.planthabit.data.repository.RecapScheduler.schedule(context)
            }
        }
    }
}
