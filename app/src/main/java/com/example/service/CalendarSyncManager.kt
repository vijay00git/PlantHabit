package com.example.service

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEventItem
import com.example.data.model.HabitEntity
import java.util.Calendar
import java.util.TimeZone

object CalendarSyncManager {

    fun hasCalendarPermission(context: Context): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted && writeGranted
    }

    fun getPrimaryCalendarId(context: Context): Long? {
        if (!hasCalendarPermission(context)) return null
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE
        )
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        var primaryId: Long? = null
        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val isPrimary = it.getInt(1) == 1
                    val isVisible = it.getInt(2) == 1
                    if (isPrimary || primaryId == null) {
                        primaryId = id
                        if (isPrimary) break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return primaryId
    }

    fun getEventsForDay(context: Context, calendarDate: Calendar): List<CalendarEventItem> {
        val events = mutableListOf<CalendarEventItem>()
        if (!hasCalendarPermission(context)) return events

        val startOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfDay)
        ContentUris.appendId(builder, endOfDay)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION
        )

        try {
            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                CalendarContract.Instances.BEGIN + " ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val title = it.getString(1) ?: "Event"
                    val begin = it.getLong(2)
                    val end = it.getLong(3)
                    val allDay = it.getInt(4) == 1
                    val desc = it.getString(5)
                    val loc = it.getString(6)
                    events.add(
                        CalendarEventItem(
                            id = id,
                            title = title,
                            startTime = begin,
                            endTime = end,
                            isAllDay = allDay,
                            description = desc,
                            location = loc
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return events
    }

    fun syncHabitToGoogleCalendar(
        context: Context,
        habit: HabitEntity,
        dayCalendar: Calendar
    ): Long? {
        if (!hasCalendarPermission(context)) return null
        val calendarId = getPrimaryCalendarId(context) ?: return null

        val parts = habit.scheduledTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val startCal = (dayCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, 30) // 30 min duration
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startCal.timeInMillis)
            put(CalendarContract.Events.DTEND, endCal.timeInMillis)
            put(CalendarContract.Events.TITLE, "[PlantHabit] ${habit.title}")
            put(CalendarContract.Events.DESCRIPTION, "Habit tracked with PlantHabit: ${habit.targetValue} ${habit.unit}. ${habit.description}")
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            // Add reminder 10 minutes before
            if (eventId != null) {
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.MINUTES, 10)
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            }
            eventId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
