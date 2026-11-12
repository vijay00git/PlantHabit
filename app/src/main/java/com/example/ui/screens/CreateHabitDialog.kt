package com.example.ui.screens

import com.example.data.model.scheduleLabel
import com.example.data.model.scheduledDays
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import com.example.ui.components.iconAccent
import com.example.ui.components.AppIcon
import com.example.data.model.HabitEntity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.ui.components.AnimatedCounter
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.categoryColors
import com.example.ui.theme.leafGradient
import com.example.ui.theme.sunriseGradient
import kotlinx.coroutines.launch

private data class TimeOfDayOption(val value: String, val label: String, val icon: ImageVector)

private val timeOfDayOptions = listOf(
    TimeOfDayOption("MORNING", "Morning", Icons.Default.WbTwilight),
    TimeOfDayOption("AFTERNOON", "Afternoon", Icons.Default.WbSunny),
    TimeOfDayOption("EVENING", "Evening", Icons.Default.Bedtime),
)

/** Categories offered in the habit sheet, with their icons. */
internal val HABIT_CATEGORIES = listOf(
    "Health" to AppIcon.HEART,
    "Fitness" to AppIcon.GYM,
    "Mindfulness" to AppIcon.MEDITATE,
    "Sleep" to AppIcon.BED,
    "Nutrition" to AppIcon.FOOD,
    "Productivity" to AppIcon.FOCUS,
    "Learning" to AppIcon.STUDY,
    "Finance" to AppIcon.SAVINGS,
    "Social" to AppIcon.PEOPLE,
    "Creativity" to AppIcon.ART,
    "Home" to AppIcon.CLEAN,
    "Self-care" to AppIcon.SMILE,
)

private fun categoryIcon(category: String): ImageVector =
    HABIT_CATEGORIES.firstOrNull { it.first.equals(category, ignoreCase = true) }?.second?.vector ?: AppIcon.SPARKLE.vector

/** Sensible stepper increment per unit, so "ml" or "steps" don't need hundreds of taps. */
private fun stepFor(unit: String): Int = when (unit) {
    "ml" -> 250
    "steps" -> 1000
    "mins" -> 5
    else -> 1
}

/** Everything the habit sheet collects; used for both creating and editing. */
data class HabitDraft(
    val title: String,
    val category: String,
    val timeOfDay: String,
    val scheduledTime: String,
    val targetValue: Int,
    val unit: String,
    val reminderEnabled: Boolean,
    val syncGoogleCalendar: Boolean,
    val iconName: String,
    /** Calendar.DAY_OF_WEEK values (1 = Sun … 7 = Sat) the habit repeats on. */
    val days: Set<Int> = (1..7).toSet(),
    /** >0 = "N times a week" on any days. */
    val timesPerWeek: Int = 0,
    /** Extra daily reminder times ("HH:mm"). */
    val extraReminders: List<String> = emptyList(),
)

/**
 * Create a habit, or edit [initial] when it's non-null (then [onDelete] shows a Delete button).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onSave: (HabitDraft) -> Unit,
    initial: HabitEntity? = null,
    onDelete: (() -> Unit)? = null,
) {
    val isEdit = initial != null
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Health") }
    var timeOfDay by remember { mutableStateOf(initial?.timeOfDay ?: "MORNING") }
    var scheduledTime by remember { mutableStateOf(initial?.scheduledTime ?: "08:00") }
    var target by remember { mutableIntStateOf(initial?.targetValue ?: 1) }
    var unit by remember { mutableStateOf(initial?.unit ?: "tasks") }
    var reminderEnabled by remember { mutableStateOf(initial?.reminderEnabled ?: true) }
    var syncGoogleCalendar by remember { mutableStateOf(initial?.syncWithGoogleCalendar ?: true) }
    // Icon follows the title until the user picks one themselves
    var pickedIcon by remember { mutableStateOf(initial?.iconName) }
    val iconName = pickedIcon ?: AppIcon.guessFromTitle(title, category).key
    var days by remember { mutableStateOf(initial?.scheduledDays() ?: (1..7).toSet()) }
    var timesPerWeek by remember { mutableIntStateOf(initial?.timesPerWeek ?: 0) }
    var extraReminders by remember {
        mutableStateOf(initial?.extraReminderTimes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList())
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    fun pickTime(initialTime: String, onPicked: (String) -> Unit) {
        val parts = initialTime.split(":")
        android.app.TimePickerDialog(
            context,
            { _, h, m -> onPicked("%02d:%02d".format(h, m)) },
            parts.getOrNull(0)?.toIntOrNull() ?: 12,
            parts.getOrNull(1)?.toIntOrNull() ?: 0,
            true
        ).show()
    }
    var isSaving by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // Keep categories/units an existing habit uses even if they're not in the default lists
    val categories = (HABIT_CATEGORIES.map { it.first } + listOfNotNull(initial?.category)).distinct()
    val units = (listOf("tasks", "ml", "steps", "mins", "pages", "km", "glasses") + listOfNotNull(initial?.unit)).distinct()
    val canSave = title.isNotBlank() && days.isNotEmpty() && !isSaving

    fun closeAnimated() {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    fun save() {
        if (title.isBlank() || isSaving) return
        isSaving = true
        haptics.success()
        val finalTitle = title.trim()
        val finalTime = scheduledTime.ifEmpty { "08:00" }
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onSave(
                HabitDraft(
                    title = finalTitle,
                    category = category,
                    timeOfDay = timeOfDay,
                    scheduledTime = finalTime,
                    targetValue = target,
                    unit = unit,
                    reminderEnabled = reminderEnabled,
                    syncGoogleCalendar = syncGoogleCalendar,
                    iconName = iconName,
                    days = if (timesPerWeek > 0) (1..7).toSet() else days,
                    timesPerWeek = timesPerWeek,
                    extraReminders = extraReminders,
                )
            )
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = c.surface,
            shape = AppShapes.xl,
            title = { Text("Delete \"${initial?.title}\"?", style = MaterialTheme.typography.titleLarge, color = c.textPrimary) },
            text = {
                Text(
                    "Its reminders and history will be removed too. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    haptics.reject()
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDelete() }
                }, modifier = Modifier.testTag("btn_confirm_delete_habit")) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge, color = c.berry)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = c.surface,
        contentColor = c.textPrimary,
        scrimColor = c.scrim,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(AppShapes.pill)
                    .background(c.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isEdit) "Edit habit ✏️" else "New Habit 🌱",
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.textPrimary
                    )
                    Text(
                        text = if (isEdit) "Tweak it until it fits your day" else "Plant a small seed, watch it grow",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.cardMuted)
                        .bounceClick { closeAnimated() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = c.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Habit title") },
                placeholder = { Text("e.g., Drink 8 glasses of water") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_habit_title"),
                colors = plantFieldColors(),
                shape = AppShapes.medium
            )

            // Category Chips
            FieldSection(label = "CATEGORY") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        HabitCategoryChip(
                            category = cat,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
            }

            // Icon picker
            FieldSection(label = "ICON") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppIcon.entries, key = { it.key }) { icon ->
                        val selected = icon.key == iconName
                        val accent = c.iconAccent(icon)
                        val scale by animateFloatAsState(if (selected) 1.12f else 1f, spring(dampingRatio = 0.5f), label = "iconPick")
                        Box(
                            modifier = Modifier
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (selected) accent.color else accent.soft)
                                .bounceClick { pickedIcon = icon.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon.vector,
                                contentDescription = icon.key,
                                tint = if (selected) c.onAccent else accent.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Repeat days
            FieldSection(label = "REPEAT · " + (if (timesPerWeek > 0) "${timesPerWeek}× A WEEK" else scheduleLabel(days).uppercase())) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Every day" to (1..7).toSet(),
                            "Weekdays" to setOf(2, 3, 4, 5, 6),
                            "Weekends" to setOf(1, 7),
                        ).forEach { (label, preset) ->
                            UnitChip(label = label, selected = timesPerWeek == 0 && days == preset, onClick = { timesPerWeek = 0; days = preset })
                        }
                        UnitChip(label = "Times a week", selected = timesPerWeek > 0, onClick = { if (timesPerWeek == 0) timesPerWeek = 3 })
                    }
                    if (timesPerWeek > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Any days, this many times:", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
                            StepperButton(Icons.Default.Remove, "Fewer", enabled = timesPerWeek > 1) { timesPerWeek-- }
                            Text("$timesPerWeek×", style = MaterialTheme.typography.titleLarge, color = c.textPrimary, modifier = Modifier.padding(horizontal = 14.dp))
                            StepperButton(Icons.Default.Add, "More", enabled = timesPerWeek < 6) { timesPerWeek++ }
                        }
                    } else Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Monday-first week
                        listOf(2 to "M", 3 to "T", 4 to "W", 5 to "T", 6 to "F", 7 to "S", 1 to "S").forEach { (dow, letter) ->
                            val on = dow in days
                            val bg by animateColorAsState(if (on) c.primary else c.cardMuted, tween(200), label = "dayBg")
                            val fg by animateColorAsState(if (on) c.onPrimary else c.textSecondary, tween(200), label = "dayFg")
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .bounceClick { days = if (on) days - dow else days + dow }
                                    .testTag("day_toggle_$dow"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(letter, style = MaterialTheme.typography.labelLarge, color = fg)
                            }
                        }
                    }
                }
            }

            // Time of Day
            FieldSection(label = "TIME OF DAY") {
                TimeOfDaySegmented(
                    selected = timeOfDay,
                    onSelect = { timeOfDay = it }
                )
            }

            // Schedule Time & Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                FieldSection(label = "TIME", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = scheduledTime,
                        onValueChange = { scheduledTime = it },
                        placeholder = { Text("HH:mm") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = plantFieldColors(),
                        shape = AppShapes.medium
                    )
                }

                FieldSection(label = "DAILY TARGET", modifier = Modifier.weight(1f)) {
                    TargetStepper(
                        value = target,
                        onValueChange = { target = it.coerceIn(1, 1_000_000) },
                        onMinus = {
                            val step = stepFor(unit)
                            target = maxOf(1, ((target - 1) / step) * step)
                        },
                        onPlus = {
                            val step = stepFor(unit)
                            target = ((target / step) + 1) * step
                        },
                        canDecrease = target > 1
                    )
                }
            }

            // Unit selection
            FieldSection(label = "UNIT") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    units.forEach { u ->
                        UnitChip(label = u, selected = unit == u, onClick = { unit = u })
                    }
                }
            }

            // Reminder + calendar switches
            PlantCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = c.card,
                contentPadding = 14.dp
            ) {
                ToggleRow(
                    icon = Icons.Default.Alarm,
                    tint = c.sunrise,
                    bubble = c.sunriseSoft,
                    title = "Daily reminder",
                    subtitle = "Flexible exact push alerts",
                    checked = reminderEnabled,
                    onCheckedChange = {
                        haptics.tick()
                        reminderEnabled = it
                    }
                )
                if (reminderEnabled) {
                    // Extra reminder times
                    FlowRow(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        extraReminders.forEach { t ->
                            Row(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.sunriseSoft)
                                    .bounceClick { extraReminders = extraReminders - t }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⏰ $t", style = MaterialTheme.typography.labelLarge, color = c.sunrise)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.Close, contentDescription = "Remove $t", tint = c.sunrise, modifier = Modifier.size(14.dp))
                            }
                        }
                        if (extraReminders.size < 7) {
                            Row(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.cardMuted)
                                    .bounceClick {
                                        pickTime(scheduledTime) { t ->
                                            if (t != scheduledTime && t !in extraReminders) extraReminders = (extraReminders + t).sorted()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("btn_add_reminder_time"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Another reminder", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(c.divider)
                )
                ToggleRow(
                    icon = Icons.Default.CalendarMonth,
                    tint = c.sky,
                    bubble = c.skySoft,
                    title = "Sync with Google Calendar",
                    subtitle = "Across all devices",
                    checked = syncGoogleCalendar,
                    onCheckedChange = {
                        haptics.tick()
                        syncGoogleCalendar = it
                    }
                )
            }

            // Save Button
            val enabledAlpha by animateFloatAsState(
                targetValue = if (canSave) 1f else 0f,
                animationSpec = tween(300),
                label = "saveEnabled"
            )
            val saveTextColor by animateColorAsState(
                targetValue = if (canSave) c.onPrimary else c.textMuted,
                animationSpec = tween(300),
                label = "saveText"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(enabled = canSave, haptic = false) { save() }
                    .clip(AppShapes.medium)
                    .background(c.cardMuted)
                    .background(c.leafGradient, alpha = enabledAlpha)
                    .testTag("btn_save_habit"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEdit) "Save changes" else "Plant This Habit 🌱",
                    style = MaterialTheme.typography.titleMedium,
                    color = saveTextColor
                )
            }

            if (isEdit && onDelete != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(AppShapes.medium)
                        .background(c.berrySoft)
                        .bounceClick {
                            haptics.reject()
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDelete() }
                        }
                        .testTag("btn_delete_habit"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = c.berry, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete habit", style = MaterialTheme.typography.labelLarge, color = c.berry)
                }
            }
        }
    }
}

@Composable
private fun FieldSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = PlantTheme.colors.textMuted
        )
        content()
    }
}

@Composable
private fun plantFieldColors(): TextFieldColors {
    val c = PlantTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        focusedContainerColor = c.cardMuted,
        unfocusedContainerColor = c.cardMuted,
        focusedBorderColor = c.primary,
        unfocusedBorderColor = Color.Transparent,
        cursorColor = c.primary,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.textSecondary,
        focusedPlaceholderColor = c.textMuted,
        unfocusedPlaceholderColor = c.textMuted,
        focusedLeadingIconColor = c.primary,
        unfocusedLeadingIconColor = c.textSecondary,
    )
}

@Composable
private fun HabitCategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(category)
    val bg by animateColorAsState(
        targetValue = if (selected) accent.color else accent.soft,
        animationSpec = tween(280),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) c.onAccent else accent.color,
        animationSpec = tween(280),
        label = "chipFg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "chipScale"
    )

    Row(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .bounceClick(onClick = onClick)
            .clip(AppShapes.pill)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("chip_cat_$category"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Text(text = category, style = MaterialTheme.typography.labelLarge, color = fg)
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(tween(250)) + fadeIn(tween(250)),
            exit = shrinkHorizontally(tween(200)) + fadeOut(tween(150))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun UnitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    val bg by animateColorAsState(if (selected) c.primarySoft else c.cardMuted, tween(250), label = "unitBg")
    val fg by animateColorAsState(if (selected) c.primary else c.textSecondary, tween(250), label = "unitFg")
    val stroke by animateColorAsState(if (selected) c.primary else Color.Transparent, tween(250), label = "unitBorder")
    Box(
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .clip(AppShapes.pill)
            .background(bg)
            .border(1.5.dp, stroke, AppShapes.pill)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

/** Segmented control with a sliding sunrise-gradient pill. */
@Composable
private fun TimeOfDaySegmented(selected: String, onSelect: (String) -> Unit) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val selectedIndex = timeOfDayOptions.indexOfFirst { it.value == selected }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(AppShapes.medium)
            .background(c.cardMuted)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / timeOfDayOptions.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "todIndicator"
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(AppShapes.small)
                .background(c.sunriseGradient)
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            timeOfDayOptions.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val fg by animateColorAsState(
                    targetValue = if (isSelected) c.onAccent else c.textSecondary,
                    animationSpec = tween(250),
                    label = "todFg"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(AppShapes.small)
                        .bounceClick(haptic = false) {
                            if (!isSelected) haptics.tick()
                            onSelect(option.value)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(option.icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = fg,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    canDecrease: Boolean
) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(AppShapes.medium)
            .background(c.cardMuted)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepperButton(
            icon = Icons.Default.Remove,
            description = "Decrease target",
            enabled = canDecrease,
            onClick = onMinus
        )
        // Tap the number to type an exact target (e.g. 3000)
        var text by remember(value) { mutableStateOf(value.toString()) }
        BasicTextField(
            value = text,
            onValueChange = { new ->
                val digits = new.filter { it.isDigit() }.take(7)
                text = digits
                digits.toIntOrNull()?.let { if (it > 0) onValueChange(it) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            textStyle = MaterialTheme.typography.titleLarge.copy(color = c.textPrimary, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(c.primary),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .testTag("input_habit_target")
        )
        StepperButton(
            icon = Icons.Default.Add,
            description = "Increase target",
            enabled = true,
            onClick = onPlus
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val c = PlantTheme.colors
    val bg by animateColorAsState(if (enabled) c.primarySoft else c.card, tween(200), label = "stepBg")
    val fg by animateColorAsState(if (enabled) c.primary else c.textMuted, tween(200), label = "stepFg")
    IconBubble(
        background = bg,
        size = 38.dp,
        modifier = Modifier.bounceClick(enabled = enabled, pressedScale = 0.85f, onClick = onClick)
    ) {
        Icon(icon, contentDescription = description, tint = fg, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    tint: Color,
    bubble: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(background = bubble, size = 40.dp) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.onPrimary,
                checkedTrackColor = c.primary,
                checkedBorderColor = c.primary,
                uncheckedThumbColor = c.textMuted,
                uncheckedTrackColor = c.cardMuted,
                uncheckedBorderColor = c.border,
            )
        )
    }
}
