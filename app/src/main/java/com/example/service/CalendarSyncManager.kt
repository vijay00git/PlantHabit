package com.example.service

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEventItem
import com.example.data.model.HabitEntity
import com.example.data.model.scheduledDays
import java.util.Calendar
import java.util.TimeZone

/**
 * Google Calendar integration through the device's calendar provider.
 *
 * - Reading: Plan shows events only from the calendars the user picked, or (by default) the
 *   calendars switched on in their Calendar app.
 * - Writing: each synced habit owns ONE recurring event (on the habit's weekdays) in the
 *   user's chosen calendar. It's updated in place on edit and deleted with the habit, so
 *   syncing never creates duplicates.
 */
object CalendarSyncManager {

    data class CalendarInfo(
        val id: Long,
        val accountName: String,
        val displayName: String,
        val color: Int,
        val isPrimary: Boolean,
        /** Switched on in the Calendar app. */
        val visible: Boolean,
        /** We may add events to it (owner/editor access). */
        val writable: Boolean,
    )

    private const val PREFS = "calendar_settings"
    private const val KEY_SHOWN = "shown_calendar_ids"
    private const val KEY_TARGET = "target_calendar_id"
    private const val TITLE_PREFIX = "[PlantHabit] "
    private const val LEGACY_PREFIX = "[Sprout] "

    fun hasCalendarPermission(context: Context): Boolean {
        fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        return granted(android.Manifest.permission.READ_CALENDAR) && granted(android.Manifest.permission.WRITE_CALENDAR)
    }

    /* ---------------------------------------------------------------------------------- */
    /* Calendar selection                                                                   */
    /* ---------------------------------------------------------------------------------- */

    /** All calendars on the device, grouped by account (primary calendar first). */
    fun listCalendars(context: Context): List<CalendarInfo> {
        if (!hasCalendarPermission(context)) return emptyList()
        val out = mutableListOf<CalendarInfo>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        )
        try {
            context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use {
                while (it.moveToNext()) {
                    out += CalendarInfo(
                        id = it.getLong(0),
                        accountName = it.getString(1) ?: "Device",
                        displayName = it.getString(2) ?: "Calendar",
                        color = it.getInt(3),
                        isPrimary = it.getInt(4) == 1,
                        visible = it.getInt(5) == 1,
                        writable = it.getInt(6) >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return out.sortedWith(
            compareBy<CalendarInfo> { it.accountName.lowercase() }.thenByDescending { it.isPrimary }.thenBy { it.displayName }
        )
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Calendars whose events appear in Plan; null = follow the Calendar app's visible calendars. */
    fun getShownCalendarIds(context: Context): Set<Long>? =
        prefs(context).getStringSet(KEY_SHOWN, null)?.mapNotNull { it.toLongOrNull() }?.toSet()

    fun setShownCalendarIds(context: Context, ids: Set<Long>?) {
        prefs(context).edit().apply {
            if (ids == null) remove(KEY_SHOWN) else putStringSet(KEY_SHOWN, ids.map { it.toString() }.toSet())
        }.apply()
    }

    /** Calendar the user chose for habit events (null = automatic). */
    fun getTargetCalendarId(context: Context): Long? =
        prefs(context).getLong(KEY_TARGET, -1L).takeIf { it >= 0 }

    fun setTargetCalendarId(context: Context, id: Long?) {
        prefs(context).edit().apply { if (id == null) remove(KEY_TARGET) else putLong(KEY_TARGET, id) }.apply()
    }

    /** The chosen calendar if still writable, else the first visible primary calendar. */
    fun resolveTargetCalendarId(context: Context, calendars: List<CalendarInfo> = listCalendars(context)): Long? {
        val writable = calendars.filter { it.writable }
        val chosen = getTargetCalendarId(context)
        writable.firstOrNull { it.id == chosen }?.let { return it.id }
        return (writable.firstOrNull { it.isPrimary && it.visible }
            ?: writable.firstOrNull { it.visible }
            ?: writable.firstOrNull())?.id
    }

    /* ---------------------------------------------------------------------------------- */
    /* Reading                                                                              */
    /* ---------------------------------------------------------------------------------- */

    fun getEventsForDay(context: Context, calendarDate: Calendar): List<CalendarEventItem> {
        val events = mutableListOf<CalendarEventItem>()
        if (!hasCalendarPermission(context)) return events

        val startOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = (calendarDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
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
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_COLOR,
        )

        // Only the calendars the user wants to see
        val shown = getShownCalendarIds(context)
        val selection: String
        val args: Array<String>?
        if (shown != null) {
            if (shown.isEmpty()) return events
            selection = "${CalendarContract.Instances.CALENDAR_ID} IN (${shown.joinToString(",") { "?" }})"
            args = shown.map { it.toString() }.toTypedArray()
        } else {
            selection = "${CalendarContract.Instances.VISIBLE} = 1"
            args = null
        }

        try {
            context.contentResolver.query(
                builder.build(), projection, selection, args, CalendarContract.Instances.BEGIN + " ASC"
            )?.use {
                while (it.moveToNext()) {
                    val title = it.getString(1) ?: "Event"
                    // Our own habit blocks are already shown as habits
                    if (title.startsWith(TITLE_PREFIX) || title.startsWith(LEGACY_PREFIX)) continue
                    events.add(
                        CalendarEventItem(
                            id = it.getLong(0),
                            title = title,
                            startTime = it.getLong(2),
                            endTime = it.getLong(3),
                            isAllDay = it.getInt(4) == 1,
                            description = it.getString(5),
                            location = it.getString(6),
                            color = it.getInt(7)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return events
    }

    /* ---------------------------------------------------------------------------------- */
    /* Writing                                                                              */
    /* ---------------------------------------------------------------------------------- */

    private val byDay = mapOf(1 to "SU", 2 to "MO", 3 to "TU", 4 to "WE", 5 to "TH", 6 to "FR", 7 to "SA")

    /**
     * Creates or updates the habit's recurring event in the target calendar.
     * Returns the event id to store on the habit (unchanged if the calendar isn't available).
     */
    fun upsertHabitEvent(context: Context, habit: HabitEntity): Long? {
        if (!hasCalendarPermission(context)) return habit.calendarEventId
        val calendarId = resolveTargetCalendarId(context) ?: return habit.calendarEventId

        val parts = habit.scheduledTime.split(":")
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 8)
            set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val days = habit.scheduledDays().sorted().mapNotNull { byDay[it] }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, start.timeInMillis)
            putNull(CalendarContract.Events.DTEND)
            put(CalendarContract.Events.DURATION, "PT30M")
            put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=${days.joinToString(",")}")
            put(CalendarContract.Events.TITLE, TITLE_PREFIX + habit.title)
            put(
                CalendarContract.Events.DESCRIPTION,
                "Habit tracked with PlantHabit: ${habit.targetValue} ${habit.unit}. ${habit.description}".trim()
            )
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }

        return try {
            val existingId = habit.calendarEventId
            val existingCalendar = existingId?.let { eventCalendarId(context, it) }
            if (existingId != null && existingCalendar == calendarId) {
                // Same calendar: update in place
                context.contentResolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingId), values, null, null
                )
                existingId
            } else {
                // New, deleted by the user, or moved to another calendar: (re)create
                if (existingId != null && existingCalendar != null) deleteHabitEvent(context, existingId)
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                val eventId = uri?.lastPathSegment?.toLongOrNull()
                if (eventId != null) {
                    context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, ContentValues().apply {
                        put(CalendarContract.Reminders.MINUTES, 10)
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    })
                }
                eventId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            habit.calendarEventId
        }
    }

    /** Calendar id of an existing (not deleted) event, or null if it's gone. */
    private fun eventCalendarId(context: Context, eventId: Long): Long? = try {
        context.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
            arrayOf(CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.DELETED),
            null, null, null
        )?.use { if (it.moveToFirst() && it.getInt(1) == 0) it.getLong(0) else null }
    } catch (e: Exception) {
        null
    }

    fun deleteHabitEvent(context: Context, eventId: Long?) {
        if (eventId == null || !hasCalendarPermission(context)) return
        try {
            context.contentResolver.delete(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes calendar events this app created (title "[PlantHabit] …" or the old "[Sprout] …",
     * with our "Habit tracked with" description). Returns how many were removed.
     */
    fun deleteAppEvents(context: Context): Int {
        if (!hasCalendarPermission(context)) return 0
        return try {
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "(${CalendarContract.Events.TITLE} LIKE ? OR ${CalendarContract.Events.TITLE} LIKE ?) AND " +
                    "${CalendarContract.Events.DESCRIPTION} LIKE ?",
                arrayOf("$TITLE_PREFIX%", "$LEGACY_PREFIX%", "Habit tracked with %")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
