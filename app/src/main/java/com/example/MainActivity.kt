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
import com.example.data.model.isFinished
import com.example.service.NotificationHelper
import com.example.ui.components.CustomBottomBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ChallengeDetailsDialog
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.CreateHabitDialog
import com.example.ui.screens.FocusModeDialog
import com.example.ui.screens.HabitDetailsSheet
import com.example.ui.screens.ImportChallengeDialog
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TemplateJsonDialog
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.PlantHabitTheme
import com.example.ui.theme.PlantTheme
import com.example.ui.viewmodel.BottomTab
import com.example.ui.viewmodel.SproutViewModel
import com.example.data.repository.HabitRepository
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
    val context = androidx.compose.ui.platform.LocalContext.current
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
    val editingHabit by viewModel.editingHabit.collectAsState()
    val detailsHabit by viewModel.habitDetails.collectAsState()
    val todayLayout by viewModel.todayLayout.collectAsState()
    val timerHabit by viewModel.timerHabit.collectAsState()
    val archivedHabits by viewModel.archivedHabits.collectAsState()
    val calendars by viewModel.calendars.collectAsState()
    val shownCalendarIds by viewModel.shownCalendarIds.collectAsState()
    val targetCalendarId by viewModel.targetCalendarId.collectAsState()
    val allHabits by viewModel.allHabits.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isFocusModeOpen by viewModel.isFocusModeOpen.collectAsState()
    val isImportChallengeOpen by viewModel.isImportChallengeOpen.collectAsState()
    val isTemplateDownloadOpen by viewModel.isTemplateDownloadOpen.collectAsState()
    val selectedChallengeForDetails by viewModel.selectedChallengeForDetails.collectAsState()

    var templateDraft by remember { mutableStateOf("") }
    // Celebrate a finished challenge once per launch
    var celebratedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(challenges) {
        if (!celebratedOnce) {
            challenges.firstOrNull { it.isFinished() }?.let {
                celebratedOnce = true
                viewModel.openChallengeDetails(it)
            }
        }
    }
    var pendingRestore by remember { mutableStateOf<com.example.data.local.BackupManager.Parsed?>(null) }

    // Backup: system "save as" picker; Restore: system file picker
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportBackup(uri) { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.previewBackup(uri) { parsed, error ->
            if (parsed != null) pendingRestore = parsed
            else coroutineScope.launch { snackbarHostState.showSnackbar(error ?: "Can't restore that file") }
        }
    }

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

    // First run: onboarding (it asks for notifications itself)
    val onboardingDone by viewModel.onboardingDone.collectAsState()
    if (!onboardingDone) {
        com.example.ui.screens.OnboardingScreen(
            challenges = challenges,
            // Updated (not freshly installed) apps already hold the user's own data
            hasExistingHabits = remember {
                val ctx = viewModel.getApplication<android.app.Application>()
                val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                info.firstInstallTime != info.lastUpdateTime
            },
            onRequestNotifications = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onFinish = { viewModel.completeOnboarding(it) }
        )
        return
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
                isSettingsOpen ||
                isFocusModeOpen ||
                isImportChallengeOpen ||
                isTemplateDownloadOpen ||
                currentTab != BottomTab.TODAY
    ) {
        when {
            selectedChallengeForDetails != null -> viewModel.closeChallengeDetails()
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
                visible = !isFocusModeOpen && selectedChallengeForDetails == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                CustomBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setCurrentTab(it) },
                    onAddHabitClick = { viewModel.isCreateHabitOpen.value = true }
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
                        onOpenHabitDetails = { viewModel.habitDetails.value = it },
                        onIncrementHabit = { habitWithStatus, amount ->
                            viewModel.incrementHabitProgress(habitWithStatus.habit, amount)
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
                        onOpenChallengeDetails = { viewModel.openChallengeDetails(it) },
                        layout = todayLayout,
                        onLayoutChange = { viewModel.setTodayLayout(it) },
                        onSaveHabitOrder = { viewModel.saveHabitOrder(it) },
                        onStartTimer = { viewModel.timerHabit.value = it }
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
                            viewModel.syncAllHabitsToCalendar { count ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("$count habits synced as repeating calendar events 🗓️")
                                }
                            }
                        },
                        onToggleHabit = { viewModel.toggleHabitCompleted(it.habit) },
                        onIncrementHabit = { habitWithStatus, amount ->
                            viewModel.incrementHabitProgress(habitWithStatus.habit, amount)
                        },
                        onAddTaskClick = { viewModel.isCreateHabitOpen.value = true },
                        onOpenHabitDetails = { viewModel.habitDetails.value = it }
                    )
                }

                BottomTab.STATS -> {
                    LaunchedEffect(Unit) { viewModel.refreshAnalytics() }
                    AnalyticsScreen(
                        analyticsData = analyticsData,
                        habits = allHabits,
                        logs = allLogs,
                        onOpenSettings = { viewModel.isSettingsOpen.value = true },
                        onOpenHabit = { viewModel.habitDetails.value = it }
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
                        },
                        onToggleFavorite = { ch -> viewModel.setChallengeFavorite(ch.id, !ch.isFavorite) },
                        onMoveToGroup = { ch, group ->
                            viewModel.setChallengeGroup(ch.id, group)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (group.isBlank()) "Removed '${ch.title}' from its group" else "Moved '${ch.title}' to 📁 $group"
                                )
                            }
                        },
                        onShare = { ch ->
                            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "${ch.title} — PlantHabit challenge")
                                putExtra(android.content.Intent.EXTRA_TEXT, viewModel.challengeAsJson(ch))
                            }
                            context.startActivity(android.content.Intent.createChooser(send, "Share challenge"))
                        },
                        onDelete = { ch ->
                            viewModel.deleteChallenge(ch) { msg ->
                                coroutineScope.launch {
                                    val builtIn = com.example.data.local.BuiltInChallenges.isBuiltIn(ch.id)
                                    val result = snackbarHostState.showSnackbar(
                                        if (builtIn) "Hid '${ch.title}'" else msg,
                                        actionLabel = if (builtIn) "Undo" else null,
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) viewModel.unhideChallenge(ch.id)
                                }
                            }
                        },
                        onUnhide = { ch ->
                            viewModel.unhideChallenge(ch.id)
                            coroutineScope.launch { snackbarHostState.showSnackbar("'${ch.title}' is back 🌱") }
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
                    onToggleFavorite = { viewModel.setChallengeFavorite(challenge.id, !challenge.isFavorite) },
                    onRestart = {
                        viewModel.restartChallenge(challenge.id)
                        coroutineScope.launch { snackbarHostState.showSnackbar("'${challenge.title}' restarted — day 1 again! 🌱") }
                    },
                    onFinish = { keep ->
                        viewModel.finishChallenge(challenge.id, keep)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (keep) "Challenge finished — your habits stay 🏆" else "Challenge finished and its habits removed 🏆"
                            )
                        }
                    },
                    onRemoveBanner = {
                        viewModel.setChallengeBanner(challenge.id, null) { _, message ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    }
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

            // Built-in timer for minute-based habits
            timerHabit?.let { h ->
                com.example.ui.screens.HabitTimerDialog(
                    habit = h,
                    reduceMotion = com.example.ui.components.LocalReduceMotion.current,
                    onLogMinutes = { minutes -> viewModel.incrementHabitProgress(h.habit, minutes) },
                    onDismiss = { viewModel.timerHabit.value = null }
                )
            }

            // Habit details & history sheet
            detailsHabit?.let { opened ->
                val liveHabit by remember(opened.id) { viewModel.repository.habitFlow(opened.id) }.collectAsState(initial = opened)
                val habitLogs by remember(opened.id) { viewModel.repository.logsForHabit(opened.id) }.collectAsState(initial = emptyList())
                val habit = liveHabit
                if (habit == null) {
                    // Deleted while open
                    LaunchedEffect(Unit) { viewModel.habitDetails.value = null }
                } else {
                    val completedDates = remember(habitLogs) { habitLogs.filter { it.isCompleted }.map { it.date }.toSet() }
                    val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
                    HabitDetailsSheet(
                        habit = habit,
                        logs = habitLogs,
                        currentStreak = remember(habit, completedDates) { viewModel.repository.currentStreak(habit, completedDates, todayStr) },
                        bestStreak = remember(habit, completedDates) { viewModel.repository.bestStreak(habit, completedDates) },
                        onToggleDate = { date -> viewModel.toggleHabitOnDate(habit, date) },
                        onEdit = {
                            viewModel.habitDetails.value = null
                            viewModel.editingHabit.value = habit
                        },
                        onDismiss = { viewModel.habitDetails.value = null },
                        onArchive = {
                            viewModel.habitDetails.value = null
                            viewModel.setHabitArchived(habit.id, true)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    "Archived '${habit.title}' — find it in Settings", actionLabel = "Undo",
                                    duration = androidx.compose.material3.SnackbarDuration.Long
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) viewModel.setHabitArchived(habit.id, false)
                            }
                        }
                    )
                }
            }

            // Create / edit habit sheet
            val editing = editingHabit
            if (isCreateHabitOpen || editing != null) {
                CreateHabitDialog(
                    initial = editing,
                    onDismiss = {
                        viewModel.isCreateHabitOpen.value = false
                        viewModel.editingHabit.value = null
                    },
                    onSave = { draft ->
                        viewModel.saveHabit(draft, editing)
                        viewModel.isCreateHabitOpen.value = false
                        viewModel.editingHabit.value = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (editing != null) "Saved changes to '${draft.title}' ✏️" else "Planted new habit '${draft.title}'! 🌱"
                            )
                        }
                    },
                    onDelete = editing?.let { habit ->
                        {
                            viewModel.editingHabit.value = null
                            viewModel.deleteHabit(habit.id) { snapshot ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        "Deleted '${habit.title}'", actionLabel = "Undo",
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed && snapshot != null) {
                                        viewModel.restoreHabit(snapshot)
                                    }
                                }
                            }
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
            LaunchedEffect(isSettingsOpen) { if (isSettingsOpen) viewModel.loadCalendars() }
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
                            viewModel.syncAllHabitsToCalendar { count ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("$count habits synced as repeating calendar events 🗓️")
                                }
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
                    calendars = calendars,
                    shownCalendarIds = shownCalendarIds,
                    targetCalendarId = targetCalendarId,
                    onOpenCalendarPicker = { viewModel.loadCalendars() },
                    onSaveCalendarChoice = { shown, target ->
                        viewModel.saveCalendarChoice(shown, target) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Calendar settings saved 🗓️") }
                        }
                    },
                    onBackup = {
                        val stamp = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        backupLauncher.launch("PlantHabit-backup-$stamp.json")
                    },
                    archivedHabits = archivedHabits,
                    onUnarchive = { h ->
                        viewModel.setHabitArchived(h.id, false)
                        coroutineScope.launch { snackbarHostState.showSnackbar("'${h.title}' is back on Today 🌱") }
                    },
                    onRestore = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                    onClearAllData = { removeEvents ->
                        viewModel.isSettingsOpen.value = false
                        viewModel.clearAllData(removeEvents) { message ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    }
                )
            }

            pendingRestore?.let { parsed ->
                val sm = parsed.summary
                val date = java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(sm.exportedAt))
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { pendingRestore = null },
                    containerColor = PlantTheme.colors.surface,
                    title = { androidx.compose.material3.Text("Restore backup?", color = PlantTheme.colors.textPrimary) },
                    text = {
                        androidx.compose.material3.Text(
                            "Backup from $date:\n• ${sm.habits} habits\n• ${sm.checkIns} check-ins\n• ${sm.moods} mood logs\n" +
                                "• ${sm.challenges} challenges\n• ${sm.photos} banner photos\n\n" +
                                "This replaces everything currently in the app.",
                            color = PlantTheme.colors.textSecondary
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            pendingRestore = null
                            viewModel.isSettingsOpen.value = false
                            viewModel.restoreBackup(parsed) { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
                        }) { androidx.compose.material3.Text("Restore", color = PlantTheme.colors.primary) }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { pendingRestore = null }) {
                            androidx.compose.material3.Text("Cancel", color = PlantTheme.colors.textSecondary)
                        }
                    }
                )
            }
        }
    }
}
