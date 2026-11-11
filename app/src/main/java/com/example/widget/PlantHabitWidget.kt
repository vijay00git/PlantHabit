package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.data.local.SproutDatabase
import com.example.data.local.SproutPreferences
import com.example.data.model.HabitEntity
import com.example.data.model.isScheduledOn
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Home-screen widget: today's habits, tap one to tick it off. */
class PlantHabitWidget : GlanceAppWidget() {

    private data class WidgetHabit(val id: Long, val title: String, val done: Boolean, val progress: String?)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rows = loadToday(context)
        provideContent { WidgetContent(rows) }
    }

    private suspend fun loadToday(context: Context): List<WidgetHabit> {
        val dao = database(context).habitDao()
        val today = today()
        val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val logs = dao.getLogsForDate(today).associateBy { it.habitId }
        return dao.getAllHabitsList()
            .filter { it.isScheduledOn(dow) }
            .map { h ->
                val log = logs[h.id]
                val progress = log?.progressValue ?: 0
                val done = log?.isCompleted == true || (h.targetValue > 0 && progress >= h.targetValue)
                WidgetHabit(
                    id = h.id,
                    title = h.title,
                    done = done,
                    progress = if (h.targetValue > 1 && !done) "%,d/%,d %s".format(progress, h.targetValue, h.unit) else null
                )
            }
            .sortedBy { it.done }
    }

    @Composable
    private fun WidgetContent(rows: List<WidgetHabit>) {
        val bg = ColorProvider(day = Color(0xFFF4FAF2), night = Color(0xFF111C17))
        val card = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF16231D))
        val text = ColorProvider(day = Color(0xFF13261A), night = Color(0xFFEDF7EF))
        val muted = ColorProvider(day = Color(0xFF889A8D), night = Color(0xFF6C8476))
        val green = ColorProvider(day = Color(0xFF1FA65A), night = Color(0xFF4ADE80))
        val onGreen = ColorProvider(day = Color.White, night = Color(0xFF04240F))
        val done = rows.count { it.done }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bg)
                .cornerRadius(24.dp)
                .padding(12.dp)
        ) {
            // Header opens the app
            Row(
                modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌱 Today", style = TextStyle(color = text, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    if (rows.isEmpty()) "" else "$done / ${rows.size}",
                    style = TextStyle(color = green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(GlanceModifier.height(8.dp))

            if (rows.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No habits today — tap to add one", style = TextStyle(color = muted, fontSize = 13.sp))
                }
            } else {
                LazyColumn {
                    items(rows, itemId = { it.id }) { row ->
                        Column {
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .background(card)
                                    .cornerRadius(14.dp)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .clickable(
                                        actionRunCallback<ToggleHabitAction>(actionParametersOf(HabitIdKey to row.id))
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(24.dp)
                                        .cornerRadius(12.dp)
                                        .background(if (row.done) green else ColorProvider(day = Color(0xFFECF5E9), night = Color(0xFF1D2D25))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (row.done) Text("✓", style = TextStyle(color = onGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold))
                                }
                                Spacer(GlanceModifier.width(10.dp))
                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(
                                        row.title,
                                        maxLines = 1,
                                        style = TextStyle(color = if (row.done) muted else text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    )
                                    row.progress?.let {
                                        Text(it, style = TextStyle(color = muted, fontSize = 11.sp))
                                    }
                                }
                            }
                            Spacer(GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    companion object {
        val HabitIdKey = ActionParameters.Key<Long>("habitId")

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        fun database(context: Context) = SproutDatabase.getDatabase(context.applicationContext, scope)
        fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        /** Redraws every PlantHabit widget; call after habits or check-ins change. */
        suspend fun refresh(context: Context) {
            try {
                PlantHabitWidget().updateAll(context.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/** Tapping a habit in the widget toggles it for today (same logic as the app). */
class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[PlantHabitWidget.HabitIdKey] ?: return
        val db = PlantHabitWidget.database(context)
        val habit: HabitEntity = db.habitDao().getHabitById(id) ?: return
        val repo = HabitRepository(context.applicationContext, db.habitDao(), SproutPreferences(context.applicationContext))
        repo.toggleHabitCompleted(habit, PlantHabitWidget.today())
        // toggleHabitCompleted refreshes widgets itself
    }
}

class PlantHabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PlantHabitWidget()
}
