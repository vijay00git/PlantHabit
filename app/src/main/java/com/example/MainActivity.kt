package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.NotificationHelper
import com.example.ui.components.CustomBottomBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ChallengeDetailsDialog
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.CreateHabitDialog
import com.example.ui.screens.FocusModeDialog
import com.example.ui.screens.ImportChallengeDialog
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TemplateJsonDialog
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.PlantHabitTheme
import com.example.ui.theme.PlantTheme
import com.example.ui.viewmodel.BottomTab
import com.example.ui.viewmodel.SproutViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val viewModel: SproutViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            PlantHabitTheme(themeMode = themeMode) {
                // Keep status/nav bar icons readable when the in-app theme differs from the system's
                val isDark = PlantTheme.colors.isDark
                LaunchedEffect(isDark) {
                    val style = if (isDark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }
                SproutApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SproutApp(viewModel: SproutViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val planViewMode by viewModel.planViewMode.collectAsState()

    val habitsWithStatus by viewModel.habitsWithStatus.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val isCalendarPermissionGranted by viewModel.isCalendarPermissionGranted.collectAsState()
    val analyticsData by viewModel.analyticsData.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val moodLogs by viewModel.moodLogs.collectAsState()
    val todayMood = remember(moodLogs, selectedDate) { moodLogs.find { it.date == selectedDate } }

    val isCreateHabitOpen by viewModel.isCreateHabitOpen.collectAsState()
    val isAnalyticsOpen by viewModel.isAnalyticsOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isFocusModeOpen by viewModel.isFocusModeOpen.collectAsState()
    val isImportChallengeOpen by viewModel.isImportChallengeOpen.collectAsState()
    val isTemplateDownloadOpen by viewModel.isTemplateDownloadOpen.collectAsState()
    val selectedChallengeForDetails by viewModel.selectedChallengeForDetails.collectAsState()

    var templateDraft by remember { mutableStateOf("") }

    // Activity Result Launchers
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] == true
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] == true
        val granted = readGranted && writeGranted
        viewModel.onCalendarPermissionResult(granted)
        coroutineScope.launch {
            if (granted) {
                snackbarHostState.showSnackbar("Google Calendar synced successfully! 📅")
            } else {
                snackbarHostState.showSnackbar("Calendar permission is required to sync events.")
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Habit reminders enabled! 🔔")
            }
        }
    }

    // Auto-request notifications on Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Handle Back Button
    BackHandler(
        enabled = selectedChallengeForDetails != null ||
                isAnalyticsOpen ||
                isSettingsOpen ||
                isFocusModeOpen ||
                isImportChallengeOpen ||
                isTemplateDownloadOpen ||
                currentTab != BottomTab.TODAY
    ) {
        when {
            selectedChallengeForDetails != null -> viewModel.closeChallengeDetails()
            isAnalyticsOpen -> viewModel.isAnalyticsOpen.value = false
            isSettingsOpen -> viewModel.isSettingsOpen.value = false
            isFocusModeOpen -> viewModel.isFocusModeOpen.value = false
            isImportChallengeOpen -> viewModel.isImportChallengeOpen.value = false
            isTemplateDownloadOpen -> viewModel.isTemplateDownloadOpen.value = false
            currentTab != BottomTab.TODAY -> viewModel.setCurrentTab(BottomTab.TODAY)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PlantTheme.colors.background,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = !isAnalyticsOpen && !isFocusModeOpen && selectedChallengeForDetails == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                CustomBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setCurrentTab(it) },
                    onAddHabitClick = { viewModel.isCreateHabitOpen.value = true },
                    onOpenStats = { viewModel.isAnalyticsOpen.value = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Content — slides left/right following bottom-bar order
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally(tween(320)) { dir * it / 6 } + fadeIn(tween(260)))
                        .togetherWith(slideOutHorizontally(tween(320)) { -dir * it / 6 } + fadeOut(tween(180)))
                },
                label = "tabContent",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
            when (tab) {
                BottomTab.TODAY -> {
                    TodayScreen(
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.setSelectedDate(it) },
                        currentFilter = currentFilter,
                        onFilterSelected = { viewModel.setFilter(it) },
                        habitsWithStatus = habitsWithStatus,
                        onToggleHabit = { viewModel.toggleHabitCompleted(it.habit) },
                        onIncrementHabit = { habitWithStatus, amount ->
                            viewModel.incrementHabitProgress(habitWithStatus.habit, amount)
                        },
                        onOpenAnalytics = { viewModel.isAnalyticsOpen.value = true },
                        onOpenSettings = { viewModel.isSettingsOpen.value = true },
                        onOpenCalendarSync = {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        },
                        onOpenFocusMode = { viewModel.isFocusModeOpen.value = true },
                        activeStreakDays = analyticsData?.activeStreakDays ?: 0,
                        currentMood = todayMood,
                        onSelectMood = { score, label ->
                            viewModel.logMood(score, label)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Logged mood: $label! 🌱")
                            }
                        },
                        goalChallenges = challenges,
                        moodGoodPercent = analyticsData?.takeIf { it.moodTotalDays > 0 }?.moodGoodPercentage,
                        onIncrementGoal = { challengeId, delta ->
                            viewModel.incrementGoal(challengeId, delta)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added +${delta.toInt()} to goal progress! 🎯")
                            }
                        },
                        onOpenChallengeDetails = { viewModel.openChallengeDetails(it) }
                    )
                }

                BottomTab.PLAN -> {
                    PlanScreen(
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.setSelectedDate(it) },
                        viewMode = planViewMode,
                        onViewModeChange = { viewModel.setPlanViewMode(it) },
                        habitsWithStatus = habitsWithStatus,
                        calendarEvents = calendarEvents,
                        isCalendarPermissionGranted = isCalendarPermissionGranted,
                        onRequestCalendarPermission = {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        },
                        onSyncWithCalendar = {
                            viewModel.syncAllHabitsToCalendar()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Habit blocks synced to your Google Calendar! 🗓️")
                            }
                        },
                        onToggleHabit = { viewModel.toggleHabitCompleted(it.habit) },
                        onIncrementHabit = { habitWithStatus, amount ->
                            viewModel.incrementHabitProgress(habitWithStatus.habit, amount)
                        },
                        onAddTaskClick = { viewModel.isCreateHabitOpen.value = true }
                    )
                }

                BottomTab.CHALLENGES -> {
                    ChallengesScreen(
                        challenges = challenges,
                        onChallengeClick = { challenge ->
                            viewModel.openChallengeDetails(challenge)
                        },
                        onCreateCustomChallenge = {
                            viewModel.isCreateHabitOpen.value = true
                        },
                        onImportJsonClick = {
                            viewModel.isImportChallengeOpen.value = true
                        },
                        onDownloadTemplateClick = {
                            viewModel.isTemplateDownloadOpen.value = true
                        }
                    )
                }
            }
            }

            // Challenge Details Dialog (Popup showing tasks, habits, reminders before accept/reject)
            selectedChallengeForDetails?.let { challenge ->
                ChallengeDetailsDialog(
                    challenge = challenge,
                    onAccept = {
                        viewModel.acceptChallenge(challenge)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Challenge accepted! Tasks & reminders added to schedule 🌱")
                        }
                    },
                    onLeave = {
                        viewModel.leaveChallenge(challenge.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Left challenge and cleared linked habits.")
                        }
                    },
                    onDismiss = { viewModel.closeChallengeDetails() },
                    onPickBanner = { uri ->
                        viewModel.setChallengeBanner(challenge.id, uri) { _, message ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    },
                    onRemoveBanner = {
                        viewModel.setChallengeBanner(challenge.id, null) { _, message ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    }
                )
            }

            // Visual Analytics Overlay
            AnimatedVisibility(
                visible = isAnalyticsOpen,
                enter = slideInVertically(tween(380)) { it / 3 } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(260)) { it / 3 } + fadeOut(tween(220))
            ) {
                AnalyticsScreen(
                    analyticsData = analyticsData,
                    onClose = { viewModel.isAnalyticsOpen.value = false }
                )
            }

            // Focus Mode Dialog
            if (isFocusModeOpen) {
                FocusModeDialog(
                    activeHabits = habitsWithStatus.filter { !it.isCompleted },
                    onCompleteHabit = { viewModel.toggleHabitCompleted(it.habit) },
                    onDismiss = { viewModel.isFocusModeOpen.value = false }
                )
            }

            // Create Habit Dialog
            if (isCreateHabitOpen) {
                CreateHabitDialog(
                    onDismiss = { viewModel.isCreateHabitOpen.value = false },
                    onSave = { title, cat, tod, time, target, unit, reminder, syncCal ->
                        viewModel.createHabit(
                            title = title,
                            category = cat,
                            timeOfDay = tod,
                            scheduledTime = time,
                            targetValue = target,
                            unit = unit,
                            reminderEnabled = reminder,
                            syncGoogleCalendar = syncCal
                        )
                        viewModel.isCreateHabitOpen.value = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Planted new habit '$title'! 🌱")
                        }
                    }
                )
            }

            // Import Challenge JSON Dialog
            if (isImportChallengeOpen) {
                ImportChallengeDialog(
                    initialJson = templateDraft,
                    onDismiss = {
                        viewModel.isImportChallengeOpen.value = false
                        templateDraft = ""
                    },
                    onImport = { json, bannerPhoto ->
                        viewModel.importChallengeFromJson(json, bannerPhoto) { success, message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                                if (success) {
                                    viewModel.isImportChallengeOpen.value = false
                                    templateDraft = ""
                                }
                            }
                        }
                    },
                    onOpenTemplate = {
                        viewModel.isImportChallengeOpen.value = false
                        viewModel.isTemplateDownloadOpen.value = true
                    }
                )
            }

            // Download & View Template JSON Dialog
            if (isTemplateDownloadOpen) {
                TemplateJsonDialog(
                    onDismiss = { viewModel.isTemplateDownloadOpen.value = false },
                    onUseTemplateInImport = { templateJson ->
                        templateDraft = templateJson
                        viewModel.isTemplateDownloadOpen.value = false
                        viewModel.isImportChallengeOpen.value = true
                    }
                )
            }

            // Settings & Preferences Dialog
            if (isSettingsOpen) {
                SettingsDialog(
                    currentThemeMode = themeMode,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    isCalendarSyncEnabled = isCalendarPermissionGranted,
                    onSyncNow = {
                        if (!isCalendarPermissionGranted) {
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        } else {
                            viewModel.syncAllHabitsToCalendar()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All habits synced to Google Calendar! 🗓️")
                            }
                        }
                    },
                    onSendTestReminder = {
                        val firstHabit = habitsWithStatus.firstOrNull()?.habit
                        NotificationHelper.showReminderNotification(
                            viewModel.getApplication(),
                            firstHabit?.id ?: 1L,
                            firstHabit?.title ?: "Hydration Check 🌱",
                            "Keep your streak going! Drink a glass of water."
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Test reminder dispatched! Check notification tray 🔔")
                        }
                    },
                    onDismiss = { viewModel.isSettingsOpen.value = false },
                    onClearAllData = { removeEvents ->
                        viewModel.isSettingsOpen.value = false
                        viewModel.clearAllData(removeEvents) { message ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    }
                )
            }
        }
    }
}
