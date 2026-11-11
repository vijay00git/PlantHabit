package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.SproutDatabase
import com.example.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = SproutDatabase.getDatabase(context, scope)
                val habits = db.habitDao().getAllHabitsList()
                habits.filter { it.reminderEnabled }.forEach { habit ->
                    ReminderScheduler.scheduleHabitReminder(context, habit)
                }
            }
        }
    }
}
