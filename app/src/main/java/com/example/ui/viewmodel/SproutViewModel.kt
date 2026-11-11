package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.data.local.BannerStorage
import com.example.data.local.SproutDatabase
import com.example.data.local.SproutPreferences
import com.example.data.model.CalendarEventItem
import com.example.data.model.ChallengeEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitWithStatus
import com.example.data.repository.AnalyticsData
import com.example.data.repository.HabitRepository
import com.example.service.CalendarSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BottomTab {
    TODAY,
    PLAN,
    CHALLENGES
}

class SproutViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SproutDatabase.getDatabase(application, viewModelScope)
    private val preferences = SproutPreferences(application)
    val repository = HabitRepository(application, db.habitDao(), preferences)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentFilter = MutableStateFlow("ALL") // ALL, MORNING, AFTERNOON, EVENING
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val _currentTab = MutableStateFlow(BottomTab.TODAY)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    private val _planViewMode = MutableStateFlow("Day") // "Day", "Week"
    val planViewMode: StateFlow<String> = _planViewMode.asStateFlow()

    val habitsWithStatus: StateFlow<List<HabitWithStatus>> = _selectedDate.flatMapLatest { date ->
        repository.getHabitsWithStatusForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges: StateFlow<List<ChallengeEntity>> = repository.challengesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<String> = preferences.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    private val _calendarEvents = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEventItem>> = _calendarEvents.asStateFlow()

    private val _isCalendarPermissionGranted = MutableStateFlow(
        CalendarSyncManager.hasCalendarPermission(application)
    )
    val isCalendarPermissionGranted: StateFlow<Boolean> = _isCalendarPermissionGranted.asStateFlow()

    private val _analyticsData = MutableStateFlow<AnalyticsData?>(null)
    val analyticsData: StateFlow<AnalyticsData?> = _analyticsData.asStateFlow()

    // Dialog & overlay states
    val isCreateHabitOpen = MutableStateFlow(false)
    val isAnalyticsOpen = MutableStateFlow(false)
    val isSettingsOpen = MutableStateFlow(false)
    val isFocusModeOpen = MutableStateFlow(false)
    val isImportChallengeOpen = MutableStateFlow(false)
    val isTemplateDownloadOpen = MutableStateFlow(false)
    val selectedChallengeForDetails = MutableStateFlow<com.example.data.model.ChallengeEntity?>(null)

    val moodLogs: StateFlow<List<com.example.data.model.MoodLogEntity>> = repository.moodLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCalendarEvents()
        refreshAnalytics()
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadCalendarEvents()
    }

    fun setFilter(filter: String) {
        _currentFilter.value = filter
    }

    fun setCurrentTab(tab: BottomTab) {
        _currentTab.value = tab
    }

    fun setPlanViewMode(mode: String) {
        _planViewMode.value = mode
    }

    fun toggleHabitCompleted(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleHabitCompleted(habit, _selectedDate.value)
            refreshAnalytics()
        }
    }

    fun incrementHabitProgress(habit: HabitEntity, amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logHabitProgress(habit, _selectedDate.value, amount)
            refreshAnalytics()
        }
    }

    fun createHabit(
        title: String,
        category: String,
        timeOfDay: String,
        scheduledTime: String,
        targetValue: Int,
        unit: String,
        reminderEnabled: Boolean,
        syncGoogleCalendar: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Pick a fitting icon from the title ("Read 20 pages" → book), else by category.
            val iconName = com.example.ui.components.AppIcon.guessFromTitle(title, category).key
            val habit = HabitEntity(
                title = title,
                category = category,
                timeOfDay = timeOfDay,
                scheduledTime = scheduledTime,
                targetValue = targetValue,
                unit = unit,
                iconName = iconName,
                reminderEnabled = reminderEnabled,
                reminderTime = scheduledTime,
                syncWithGoogleCalendar = syncGoogleCalendar
            )
            repository.createHabit(habit)
            refreshAnalytics()
            if (syncGoogleCalendar) {
                loadCalendarEvents()
            }
        }
    }

    /** Habit currently open in the editor sheet (null = closed). */
    val editingHabit = MutableStateFlow<HabitEntity?>(null)

    /** Creates a new habit, or updates [existing] with the draft's values. */
    fun saveHabit(draft: com.example.ui.screens.HabitDraft, existing: HabitEntity?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (existing == null) {
                repository.createHabit(
                    HabitEntity(
                        title = draft.title,
                        category = draft.category,
                        timeOfDay = draft.timeOfDay,
                        scheduledTime = draft.scheduledTime,
                        targetValue = draft.targetValue,
                        unit = draft.unit,
                        iconName = draft.iconName,
                        reminderEnabled = draft.reminderEnabled,
                        reminderTime = draft.scheduledTime,
                        reminderDays = draft.days.sorted().joinToString(","),
                        syncWithGoogleCalendar = draft.syncGoogleCalendar
                    )
                )
            } else {
                repository.updateHabit(
                    existing.copy(
                        title = draft.title,
                        category = draft.category,
                        timeOfDay = draft.timeOfDay,
                        scheduledTime = draft.scheduledTime,
                        targetValue = draft.targetValue,
                        unit = draft.unit,
                        iconName = draft.iconName,
                        reminderEnabled = draft.reminderEnabled,
                        reminderTime = draft.scheduledTime,
                        reminderDays = draft.days.sorted().joinToString(","),
                        syncWithGoogleCalendar = draft.syncGoogleCalendar
                    )
                )
            }
            refreshAnalytics()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabit(habitId)
            refreshAnalytics()
        }
    }

    fun openChallengeDetails(challenge: ChallengeEntity) {
        selectedChallengeForDetails.value = challenge
    }

    fun closeChallengeDetails() {
        selectedChallengeForDetails.value = null
    }

    fun acceptChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.acceptChallenge(challenge)
            closeChallengeDetails()
            refreshAnalytics()
        }
    }

    fun leaveChallenge(challengeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.leaveChallenge(challengeId)
            closeChallengeDetails()
            refreshAnalytics()
        }
    }

    fun logMood(score: Int, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logMood(score, label)
            refreshAnalytics()
        }
    }

    fun incrementGoal(challengeId: String, delta: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementGoal(challengeId, delta)
            refreshAnalytics()
        }
    }

    fun toggleChallenge(challengeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleChallengeJoined(challengeId)
        }
    }

    /**
     * Attaches a photo banner to a challenge (or removes it when [photo] is null).
     * The photo is copied into app storage; the previous custom banner file is deleted.
     */
    fun setChallengeBanner(challengeId: String, photo: Uri?, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val newRef = photo?.let { BannerStorage.save(context, challengeId, it) }
                val (oldRef, updated) = repository.setChallengeBanner(challengeId, newRef)
                if (updated == null) {
                    BannerStorage.delete(context, newRef)
                    onResult(false, "Challenge not found")
                    return@launch
                }
                if (oldRef != newRef) BannerStorage.delete(context, oldRef)
                // Keep an open details dialog in sync with the new banner.
                if (selectedChallengeForDetails.value?.id == challengeId) {
                    selectedChallengeForDetails.value = updated
                }
                onResult(true, if (photo != null) "Banner photo updated 📸" else "Banner photo removed")
            } catch (e: Exception) {
                onResult(false, "Couldn't use that photo: ${e.localizedMessage ?: "unknown error"}")
            }
        }
    }

    fun importChallengeFromJson(jsonString: String, bannerPhoto: Uri? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val parseResult = com.example.data.repository.ChallengeJsonManager.parseChallengeJson(jsonString)
            parseResult.fold(
                onSuccess = { (challenge, habits) ->
                    repository.importChallengeWithHabits(challenge, habits)
                    if (bannerPhoto != null) setChallengeBanner(challenge.id, bannerPhoto)
                    refreshAnalytics()
                    onResult(true, "Successfully imported '${challenge.title}' with ${habits.size} habits! 🏆")
                },
                onFailure = { error ->
                    onResult(false, "Invalid JSON: ${error.localizedMessage ?: "Please check format"}")
                }
            )
        }
    }

    /** Wipes habits, logs, moods and challenge progress; optionally our calendar events too. */
    fun clearAllData(removeCalendarEvents: Boolean, onDone: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllData()
            val removed = if (removeCalendarEvents) CalendarSyncManager.deleteAppEvents(getApplication()) else 0
            closeChallengeDetails()
            refreshAnalytics()
            if (removeCalendarEvents) loadCalendarEvents()
            onDone(
                if (removeCalendarEvents) "All data cleared and $removed calendar events removed. Fresh start 🌱"
                else "All data cleared. Fresh start 🌱"
            )
        }
    }

    fun onCalendarPermissionResult(granted: Boolean) {
        _isCalendarPermissionGranted.value = granted
        if (granted) {
            loadCalendarEvents()
        }
    }

    fun loadCalendarEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            if (CalendarSyncManager.hasCalendarPermission(context)) {
                _isCalendarPermissionGranted.value = true
                val cal = Calendar.getInstance()
                try {
                    val date = dateFormat.parse(_selectedDate.value)
                    if (date != null) cal.time = date
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val events = CalendarSyncManager.getEventsForDay(context, cal)
                _calendarEvents.value = events
            } else {
                _isCalendarPermissionGranted.value = false
            }
        }
    }

    fun syncAllHabitsToCalendar(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.syncAllHabitsToCalendar()
            loadCalendarEvents()
            onDone(count)
        }
    }

    // ---- Calendar selection ----
    val calendars = MutableStateFlow<List<CalendarSyncManager.CalendarInfo>>(emptyList())
    val shownCalendarIds = MutableStateFlow<Set<Long>?>(null)
    val targetCalendarId = MutableStateFlow<Long?>(null)

    fun loadCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val list = CalendarSyncManager.listCalendars(context)
            calendars.value = list
            shownCalendarIds.value = CalendarSyncManager.getShownCalendarIds(context)
            targetCalendarId.value = CalendarSyncManager.resolveTargetCalendarId(context, list)
        }
    }

    /** Saves the calendar choice; habit events move to the new target calendar. */
    fun saveCalendarChoice(shown: Set<Long>?, target: Long?, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val targetChanged = target != CalendarSyncManager.resolveTargetCalendarId(context)
            CalendarSyncManager.setShownCalendarIds(context, shown)
            CalendarSyncManager.setTargetCalendarId(context, target)
            if (targetChanged) repository.syncAllHabitsToCalendar()
            loadCalendars()
            loadCalendarEvents()
            onDone()
        }
    }

    fun refreshAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            _analyticsData.value = repository.getDeepAnalytics()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.setThemeMode(mode)
        }
    }
}
