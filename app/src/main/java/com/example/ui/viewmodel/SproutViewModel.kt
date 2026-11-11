package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    AI,
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

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
    val isAiPlannerOpen = MutableStateFlow(false)
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
            val iconName = when (category.lowercase()) {
                "health" -> "water"
                "fitness" -> "walk"
                "mindfulness" -> "sparkle"
                else -> "alarm"
            }
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

    fun importChallengeFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val parseResult = com.example.data.repository.ChallengeJsonManager.parseChallengeJson(jsonString)
            parseResult.fold(
                onSuccess = { (challenge, habits) ->
                    repository.importChallengeWithHabits(challenge, habits)
                    refreshAnalytics()
                    onResult(true, "Successfully imported '${challenge.title}' with ${habits.size} habits! 🏆")
                },
                onFailure = { error ->
                    onResult(false, "Invalid JSON: ${error.localizedMessage ?: "Please check format"}")
                }
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

    fun syncAllHabitsToCalendar() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val cal = Calendar.getInstance()
            try {
                val date = dateFormat.parse(_selectedDate.value)
                if (date != null) cal.time = date
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val habits = habitsWithStatus.value
            for (h in habits) {
                CalendarSyncManager.syncHabitToGoogleCalendar(context, h.habit, cal)
            }
            loadCalendarEvents()
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
