package com.example.ui.screens

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
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    var confirmClear by remember { mutableStateOf(false) }

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

@Composable
private fun ClearDataConfirmDialog(
    canRemoveCalendarEvents: Boolean,
    onConfirm: (removeCalendarEvents: Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    val c = PlantTheme.colors
    var removeEvents by remember { mutableStateOf(false) }
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
