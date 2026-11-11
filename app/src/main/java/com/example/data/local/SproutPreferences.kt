package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sprout_preferences")

class SproutPreferences(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "DARK", "LIGHT", "SYSTEM"
        val KEY_CALENDAR_SYNC = booleanPreferencesKey("calendar_sync_enabled")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_DAILY_SUMMARY_TIME = stringPreferencesKey("daily_summary_time")
        val KEY_PLANT_STAGE = intPreferencesKey("plant_stage") // 0 to 4
        val KEY_USER_NAME = stringPreferencesKey("user_name")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "DARK" // Dark mode by default
    }

    val isCalendarSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_CALENDAR_SYNC] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val dailySummaryTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAILY_SUMMARY_TIME] ?: "21:00"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CALENDAR_SYNC] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
