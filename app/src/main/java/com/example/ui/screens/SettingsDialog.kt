package com.example.ui.screens

import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Tune
import com.example.service.CalendarSyncManager
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme

private data class ThemeOption(val mode: String, val label: String, val icon: ImageVector)

private val themeOptions = listOf(
    ThemeOption("SYSTEM", "System", Icons.Default.PhoneAndroid),
    ThemeOption("LIGHT", "Light", Icons.Default.LightMode),
    ThemeOption("DARK", "Dark", Icons.Default.DarkMode),
)

@Composable
fun SettingsDialog(
    currentThemeMode: String, // "SYSTEM", "LIGHT" or "DARK"
    onThemeModeChange: (String) -> Unit,
    isCalendarSyncEnabled: Boolean,
    onSyncNow: () -> Unit,
    onSendTestReminder: () -> Unit,
    onDismiss: () -> Unit,
    /** Clears all user data; the flag says whether to also delete our calendar events. */
    onClearAllData: (removeCalendarEvents: Boolean) -> Unit = {},
    calendars: List<CalendarSyncManager.CalendarInfo> = emptyList(),
    shownCalendarIds: Set<Long>? = null,
    targetCalendarId: Long? = null,
    onOpenCalendarPicker: () -> Unit = {},
    onSaveCalendarChoice: (shown: Set<Long>?, target: Long?) -> Unit = { _, _ -> },
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {},
    archivedHabits: List<com.example.data.model.HabitEntity> = emptyList(),
    onUnarchive: (com.example.data.model.HabitEntity) -> Unit = {},
) {
    var showArchived by remember { mutableStateOf(false) }
    if (showArchived) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showArchived = false },
            containerColor = PlantTheme.colors.surface,
            shape = AppShapes.xl,
            title = { Text("Archived habits", style = MaterialTheme.typography.titleLarge, color = PlantTheme.colors.textPrimary) },
            text = {
                if (archivedHabits.isEmpty()) {
                    Text("Nothing archived. Archive a habit from its details to pause it without losing its history.",
                        style = MaterialTheme.typography.bodyMedium, color = PlantTheme.colors.textSecondary)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(archivedHabits, key = { it.id }) { h ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(h.title, style = MaterialTheme.typography.bodyMedium, color = PlantTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                TextButton(onClick = { onUnarchive(h) }, modifier = Modifier.testTag("unarchive_${h.id}")) {
                                    Text("Restore", style = MaterialTheme.typography.labelLarge, color = PlantTheme.colors.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchived = false }) {
                    Text("Done", style = MaterialTheme.typography.labelLarge, color = PlantTheme.colors.primary)
                }
            }
        )
    }
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    var confirmClear by remember { mutableStateOf(false) }
    var pickCalendars by remember { mutableStateOf(false) }

    if (pickCalendars) {
        CalendarPickerDialog(
            calendars = calendars,
            initialShown = shownCalendarIds,
            initialTarget = targetCalendarId,
            onSave = { shown, target ->
                pickCalendars = false
                haptics.success()
                onSaveCalendarChoice(shown, target)
            },
            onCancel = { pickCalendars = false }
        )
    }

    if (confirmClear) {
        ClearDataConfirmDialog(
            canRemoveCalendarEvents = isCalendarSyncEnabled,
            onConfirm = { removeEvents ->
                confirmClear = false
                haptics.reject()
                onClearAllData(removeEvents)
            },
            onCancel = { confirmClear = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.xl)
                .background(c.surface)
                .border(1.dp, c.border, AppShapes.xl)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Preferences",
                            style = MaterialTheme.typography.headlineSmall,
                            color = c.textPrimary
                        )
                        Text(
                            text = "Make PlantHabit feel like yours",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c.cardMuted)
                            .bounceClick(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = c.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Appearance
                SettingsGroup(title = "APPEARANCE", index = 0) {
                    SettingsRowHeader(
                        icon = Icons.Default.Palette,
                        tint = c.lavender,
                        bubble = c.lavenderSoft,
                        title = "Theme",
                        subtitle = "Follow your phone or pick a look"
                    )
                    Spacer(Modifier.height(14.dp))
                    ThemeSegmentedControl(
                        selectedMode = currentThemeMode,
                        onSelect = { mode ->
                            if (mode != currentThemeMode) {
                                haptics.tick()
                                onThemeModeChange(mode)
                            }
                        }
                    )
                }

                // Google Calendar & Tasks Sync
                SettingsGroup(title = "DEVICE & GOOGLE CALENDAR SYNC", index = 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsRowHeader(
                            icon = Icons.Default.CalendarMonth,
                            tint = c.sky,
                            bubble = c.skySoft,
                            title = "Sync Google Calendar",
                            subtitle = "Time blocks & reminders on all devices",
                            modifier = Modifier.weight(1f)
                        )
                        StatusPill(active = isCalendarSyncEnabled)
                    }
                    Spacer(Modifier.height(14.dp))
                    SettingsActionButton(
                        text = "Sync now",
                        icon = Icons.Default.Sync,
                        container = c.primary,
                        content = c.onPrimary,
                        modifier = Modifier.testTag("btn_sync_now"),
                        onClick = onSyncNow
                    )
                    if (isCalendarSyncEnabled) {
                        Spacer(Modifier.height(10.dp))
                        val target = calendars.firstOrNull { it.id == targetCalendarId }
                        SettingsActionButton(
                            text = target?.let { "Calendars · habits go to ${it.displayName}" } ?: "Choose calendars",
                            icon = Icons.Default.Tune,
                            container = c.skySoft,
                            content = c.sky,
                            modifier = Modifier.testTag("btn_choose_calendars"),
                            onClick = {
                                onOpenCalendarPicker()
                                pickCalendars = true
                            }
                        )
                    }
                }

                // Notification System
                SettingsGroup(title = "NOTIFICATIONS & SCHEDULING", index = 2) {
                    SettingsRowHeader(
                        icon = Icons.Default.NotificationsActive,
                        tint = c.sunrise,
                        bubble = c.sunriseSoft,
                        title = "Flexible Reminder Engine",
                        subtitle = "Exact alarms, 15-min snooze and quick mark-done actions from the notification shade."
                    )
                    Spacer(Modifier.height(14.dp))
                    SettingsActionButton(
                        text = "Send test reminder",
                        icon = Icons.Default.NotificationsActive,
                        container = c.sunriseSoft,
                        content = c.sunrise,
                        modifier = Modifier.testTag("btn_test_reminder"),
                        onClick = onSendTestReminder
                    )
                }

                // Data
                SettingsGroup(title = "YOUR DATA", index = 3) {
                    SettingsRowHeader(
                        icon = Icons.Default.CloudUpload,
                        tint = c.primary,
                        bubble = c.primarySoft,
                        title = "Backup",
                        subtitle = "Save everything — habits, history, moods, challenges and photos — to a file you can keep anywhere (Drive, Downloads…)."
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsActionButton(
                            text = "Back up",
                            icon = Icons.Default.CloudUpload,
                            container = c.primary,
                            content = c.onPrimary,
                            modifier = Modifier.weight(1f).testTag("btn_backup"),
                            onClick = onBackup
                        )
                        SettingsActionButton(
                            text = "Restore",
                            icon = Icons.Default.CloudDownload,
                            container = c.primarySoft,
                            content = c.primary,
                            modifier = Modifier.weight(1f).testTag("btn_restore"),
                            onClick = onRestore
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SettingsActionButton(
                        text = "Archived habits (${archivedHabits.size})",
                        icon = Icons.Default.Archive,
                        container = c.sunSoft,
                        content = c.sun,
                        modifier = Modifier.testTag("btn_archived_habits"),
                        onClick = { showArchived = true }
                    )
                    Spacer(Modifier.height(18.dp))
                    SettingsRowHeader(
                        icon = Icons.Default.DeleteSweep,
                        tint = c.berry,
                        bubble = c.berrySoft,
                        title = "Start fresh",
                        subtitle = "Clear all habits, check-ins, moods and challenge progress. Challenges, your banner photos and settings stay."
                    )
                    Spacer(Modifier.height(14.dp))
                    SettingsActionButton(
                        text = "Clear all data",
                        icon = Icons.Default.DeleteSweep,
                        container = c.berrySoft,
                        content = c.berry,
                        modifier = Modifier.testTag("btn_clear_all_data"),
                        onClick = { confirmClear = true }
                    )
                }
            }
        }
    }
}

/**
 * Lets the user pick which calendars appear in Plan and which one receives habit events,
 * grouped by Google account.
 */
@Composable
private fun CalendarPickerDialog(
    calendars: List<CalendarSyncManager.CalendarInfo>,
    initialShown: Set<Long>?,
    initialTarget: Long?,
    onSave: (shown: Set<Long>?, target: Long?) -> Unit,
    onCancel: () -> Unit,
) {
    val c = PlantTheme.colors
    // null = "follow the Calendar app" (calendars switched on there)
    var followCalendarApp by remember(initialShown) { mutableStateOf(initialShown == null) }
    var shown by remember(calendars, initialShown) {
        mutableStateOf(initialShown ?: calendars.filter { it.visible }.map { it.id }.toSet())
    }
    var target by remember(initialTarget) { mutableStateOf(initialTarget) }
    val byAccount = calendars.groupBy { it.accountName }

    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.xl)
                .background(c.surface)
                .border(1.dp, c.border, AppShapes.xl)
                .padding(20.dp)
        ) {
            Text("Google Calendar", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
            Text(
                "Choose where habit blocks go and whose events show in Plan.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
            Spacer(Modifier.height(14.dp))

            if (calendars.isEmpty()) {
                Text(
                    "No calendars found. Add a Google account in your phone's settings and allow calendar access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
            }

            if (calendars.isNotEmpty()) LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item(key = "target_header") {
                    PickerSectionLabel("ADD HABIT BLOCKS TO")
                }
                byAccount.forEach { (account, list) ->
                    val writable = list.filter { it.writable }
                    if (writable.isEmpty()) return@forEach
                    item(key = "t_acc_$account") { AccountLabel(account) }
                    items(writable, key = { "t_${it.id}" }) { cal ->
                        CalendarRow(
                            cal = cal,
                            selected = target == cal.id,
                            radio = true,
                            onClick = { target = cal.id }
                        )
                    }
                }

                item(key = "shown_header") {
                    Spacer(Modifier.height(10.dp))
                    PickerSectionLabel("SHOW EVENTS FROM")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.medium)
                            .bounceClick(pressedScale = 0.98f) {
                                followCalendarApp = !followCalendarApp
                                if (followCalendarApp) shown = calendars.filter { it.visible }.map { it.id }.toSet()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = followCalendarApp,
                            onCheckedChange = {
                                followCalendarApp = it
                                if (it) shown = calendars.filter { cal -> cal.visible }.map { cal -> cal.id }.toSet()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = c.primary, uncheckedColor = c.textMuted)
                        )
                        Text(
                            "Same as my Calendar app (calendars switched on there)",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textPrimary
                        )
                    }
                }
                byAccount.forEach { (account, list) ->
                    item(key = "s_acc_$account") { AccountLabel(account) }
                    items(list, key = { "s_${it.id}" }) { cal ->
                        CalendarRow(
                            cal = cal,
                            selected = cal.id in shown,
                            radio = false,
                            enabled = !followCalendarApp,
                            onClick = { shown = if (cal.id in shown) shown - cal.id else shown + cal.id }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                }
                TextButton(
                    onClick = { onSave(if (followCalendarApp) null else shown, target) },
                    modifier = Modifier.testTag("btn_save_calendars")
                ) {
                    Text("Save", style = MaterialTheme.typography.labelLarge, color = c.primary)
                }
            }
        }
    }
}

@Composable
private fun PickerSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = PlantTheme.colors.textMuted,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun AccountLabel(account: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PlantTheme.colors.sky, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(account, style = MaterialTheme.typography.titleSmall, color = PlantTheme.colors.textPrimary)
    }
}

@Composable
private fun CalendarRow(
    cal: CalendarSyncManager.CalendarInfo,
    selected: Boolean,
    radio: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(if (selected && radio) c.primarySoft else Color.Transparent)
            .bounceClick(enabled = enabled, pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(cal.color or 0xFF000000.toInt()))
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cal.displayName, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
            if (cal.isPrimary) Text("Main calendar", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
        if (radio) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = c.primary, unselectedColor = c.textMuted)
            )
        } else {
            Checkbox(
                checked = selected,
                onCheckedChange = { onClick() },
                enabled = enabled,
                colors = CheckboxDefaults.colors(checkedColor = c.primary, uncheckedColor = c.textMuted)
            )
        }
    }
}

@Composable
private fun ClearDataConfirmDialog(
    canRemoveCalendarEvents: Boolean,
    onConfirm: (removeCalendarEvents: Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    val c = PlantTheme.colors
    // Habit events repeat, so removing them is the sensible default
    var removeEvents by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = c.surface,
        shape = AppShapes.xl,
        icon = {
            IconBubble(background = c.berrySoft, size = 52.dp) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = c.berry, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text("Clear all data?", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This deletes every habit and its reminders, all check-ins and mood logs, and resets " +
                        "every challenge to not-joined. Challenges, banner photos and your theme are kept.\n\n" +
                        "This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
                if (canRemoveCalendarEvents) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.medium)
                            .bounceClick(pressedScale = 0.98f) { removeEvents = !removeEvents }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = removeEvents,
                            onCheckedChange = { removeEvents = it },
                            colors = CheckboxDefaults.colors(checkedColor = c.berry, uncheckedColor = c.textMuted)
                        )
                        Text(
                            "Also remove PlantHabit events from my Google Calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(removeEvents) }, modifier = Modifier.testTag("btn_confirm_clear")) {
                Text("Clear everything", style = MaterialTheme.typography.labelLarge, color = c.berry)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
    )
}

@Composable
private fun SettingsGroup(
    title: String,
    index: Int,
    content: @Composable () -> Unit
) {
    val c = PlantTheme.colors
    Column(
        modifier = Modifier.staggeredEntrance(index),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = c.textMuted,
            modifier = Modifier.padding(start = 4.dp)
        )
        PlantCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.large,
            containerColor = c.card,
            contentPadding = 14.dp
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRowHeader(
    icon: ImageVector,
    tint: Color,
    bubble: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(background = bubble, size = 40.dp) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}

@Composable
private fun StatusPill(active: Boolean) {
    val c = PlantTheme.colors
    val bg by animateColorAsState(if (active) c.primarySoft else c.cardMuted, tween(300), label = "statusBg")
    val fg by animateColorAsState(if (active) c.primary else c.textMuted, tween(300), label = "statusFg")
    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(fg))
            Text(text = if (active) "Active" else "Off", style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}

@Composable
private fun SettingsActionButton(
    text: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .bounceClick(pressedScale = 0.97f, onClick = onClick)
            .clip(AppShapes.medium)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

/** 3-way segmented control with a springy sliding pill behind the selected option. */
@Composable
private fun ThemeSegmentedControl(
    selectedMode: String,
    onSelect: (String) -> Unit
) {
    val c = PlantTheme.colors
    val selectedIndex = themeOptions.indexOfFirst { it.mode == selectedMode }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(AppShapes.medium)
            .background(c.cardMuted)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / themeOptions.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "themeIndicator"
        )

        // Sliding pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(AppShapes.small)
                .background(c.primary)
        )

        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            themeOptions.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) c.onPrimary else c.textSecondary,
                    animationSpec = tween(250),
                    label = "themeOptionColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(AppShapes.small)
                        .bounceClick(haptic = false) { onSelect(option.mode) }
                        .testTag("theme_toggle_${option.mode}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedContent(
                            targetState = isSelected,
                            transitionSpec = {
                                (scaleIn(initialScale = 0.6f, animationSpec = tween(250)) + fadeIn(tween(250))) togetherWith
                                    fadeOut(tween(150))
                            },
                            label = "themeIcon"
                        ) { _ ->
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
