package com.planthabit.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.planthabit.data.local.BuiltInChallenges
import com.planthabit.data.model.ChallengeEntity
import com.planthabit.ui.components.IconBubble
import com.planthabit.ui.components.bounceClick
import com.planthabit.ui.components.rememberHaptics
import com.planthabit.ui.theme.AppShapes
import com.planthabit.ui.theme.PlantTheme

/** How the Discover list is split into sections. */
enum class ChallengeGrouping(val label: String) {
    NONE("No grouping"), CATEGORY("Category"), STATUS("Status"), MY_GROUPS("My groups")
}

enum class ChallengeSort(val label: String) {
    A_Z("A → Z"), SHORTEST("Shortest first"), LONGEST("Longest first")
}

/* ---------------------------------------------------------------------------------------- */
/* Toolbar: search + group/sort menu                                                          */
/* ---------------------------------------------------------------------------------------- */

@Composable
internal fun ChallengeToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    grouping: ChallengeGrouping,
    onGroupingChange: (ChallengeGrouping) -> Unit,
    sort: ChallengeSort,
    onSortChange: (ChallengeSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val focus = LocalFocusManager.current
    var menuOpen by remember { mutableStateOf(false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search challenges", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(20.dp).bounceClick { onQueryChange("") }
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = challengeTextFieldColors(),
            shape = AppShapes.pill,
            modifier = Modifier.weight(1f).heightIn(min = 50.dp).testTag("challenge_search")
        )
        Spacer(Modifier.width(10.dp))
        Box {
            val active = grouping != ChallengeGrouping.NONE || sort != ChallengeSort.A_Z
            IconBubble(
                background = if (active) c.primary else c.card,
                size = 50.dp,
                modifier = Modifier
                    .border(1.dp, c.border, CircleShape)
                    .bounceClick { menuOpen = true }
                    .testTag("challenge_view_menu")
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Group & sort", tint = if (active) c.onPrimary else c.textPrimary)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = c.surface
            ) {
                MenuLabel("GROUP BY")
                ChallengeGrouping.entries.forEach { g ->
                    MenuChoice(g.label, selected = g == grouping) { onGroupingChange(g); menuOpen = false }
                }
                HorizontalDivider(color = c.divider)
                MenuLabel("SORT")
                ChallengeSort.entries.forEach { s ->
                    MenuChoice(s.label, selected = s == sort) { onSortChange(s); menuOpen = false }
                }
            }
        }
    }
}

@Composable
private fun MenuLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = PlantTheme.colors.textMuted,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun MenuChoice(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = if (selected) c.primary else c.textPrimary) },
        trailingIcon = { if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = c.primary) },
        onClick = onClick
    )
}

/* ---------------------------------------------------------------------------------------- */
/* Section header with collapse chevron                                                       */
/* ---------------------------------------------------------------------------------------- */

@Composable
internal fun CollapsibleSectionHeader(
    title: String,
    emoji: String,
    count: Int,
    collapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val rotation by animateFloatAsState(if (collapsed) -90f else 0f, spring(dampingRatio = 0.7f), label = "chevron")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .bounceClick(pressedScale = 0.98f, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(AppShapes.pill)
                .background(c.cardMuted)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("$count", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Default.ExpandMore,
            contentDescription = if (collapsed) "Expand" else "Collapse",
            tint = c.textSecondary,
            modifier = Modifier.graphicsLayer { rotationZ = rotation }
        )
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Star button                                                                                 */
/* ---------------------------------------------------------------------------------------- */

/** Animated favourite star; [onPhoto] uses a frosted dark background for banners. */
@Composable
internal fun FavoriteStar(
    favorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onPhoto: Boolean = true,
) {
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val scale by animateFloatAsState(if (favorite) 1.15f else 1f, spring(dampingRatio = 0.35f), label = "starScale")
    val tint by animateColorAsState(if (favorite) c.sun else if (onPhoto) Color.White else c.textMuted, tween(200), label = "starTint")
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (onPhoto) Color.Black.copy(alpha = 0.38f) else c.cardMuted)
            .bounceClick(haptic = false) {
                if (!favorite) haptics.success() else haptics.tick()
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (favorite) "Remove from favourites" else "Add to favourites",
            tint = tint,
            modifier = Modifier.size(20.dp).graphicsLayer { scaleX = scale; scaleY = scale }
        )
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Long-press actions                                                                          */
/* ---------------------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChallengeActionsSheet(
    challenge: ChallengeEntity,
    onToggleFavorite: () -> Unit,
    onMoveToGroup: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    val builtIn = BuiltInChallenges.isBuiltIn(challenge.id)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        scrimColor = c.scrim,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(challenge.title, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Text(
                if (challenge.groupName.isBlank()) challenge.category else "${challenge.category} · 📁 ${challenge.groupName}",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )
            Spacer(Modifier.height(14.dp))
            ActionRow(
                icon = if (challenge.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                tint = c.sun, bubble = c.sunSoft,
                text = if (challenge.isFavorite) "Remove from favourites" else "Add to favourites",
                tag = "action_favorite"
            ) { onToggleFavorite(); onDismiss() }
            ActionRow(Icons.Default.Folder, c.sky, c.skySoft, if (challenge.groupName.isBlank()) "Add to a group…" else "Move to another group…", "action_group") {
                onMoveToGroup()
            }
            ActionRow(Icons.Default.Share, c.lavender, c.lavenderSoft, "Share as JSON", "action_share") { onShare(); onDismiss() }
            ActionRow(
                icon = if (builtIn) Icons.Default.VisibilityOff else Icons.Default.Delete,
                tint = c.berry, bubble = c.berrySoft,
                text = if (builtIn) "Hide challenge" else "Delete challenge",
                tag = "action_delete"
            ) { onDelete() }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, tint: Color, bubble: Color, text: String, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(background = bubble, size = 40.dp) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = PlantTheme.colors.textPrimary)
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Group picker                                                                                */
/* ---------------------------------------------------------------------------------------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoveToGroupDialog(
    challenge: ChallengeEntity,
    existingGroups: List<String>,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    var selected by remember { mutableStateOf(challenge.groupName) }
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = AppShapes.xl,
        icon = {
            IconBubble(background = c.skySoft, size = 48.dp) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = c.sky)
            }
        },
        title = { Text("Group \"${challenge.title}\"", style = MaterialTheme.typography.titleLarge, color = c.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existingGroups.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        existingGroups.forEach { g ->
                            GroupChip(g, selected = selected == g && newName.isBlank()) {
                                selected = g
                                newName = ""
                            }
                        }
                        if (challenge.groupName.isNotBlank()) {
                            GroupChip("No group", selected = selected.isBlank() && newName.isBlank()) {
                                selected = ""
                                newName = ""
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.take(30) },
                    label = { Text("New group name") },
                    placeholder = { Text("e.g. Morning set, Winter goals") },
                    singleLine = true,
                    colors = challengeTextFieldColors(),
                    shape = AppShapes.medium,
                    modifier = Modifier.fillMaxWidth().testTag("input_group_name")
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(newName.trim().ifBlank { selected }) }, modifier = Modifier.testTag("btn_save_group")) {
                Text("Save", style = MaterialTheme.typography.labelLarge, color = c.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
    )
}

@Composable
private fun GroupChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = PlantTheme.colors
    val bg by animateColorAsState(if (selected) c.sky else c.skySoft, tween(200), label = "groupChip")
    Row(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(bg)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (text == "No group") text else "📁 $text",
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) c.onAccent else c.sky
        )
    }
}

/* ---------------------------------------------------------------------------------------- */
/* Delete / hide confirmation                                                                  */
/* ---------------------------------------------------------------------------------------- */

@Composable
internal fun DeleteChallengeDialog(
    challenge: ChallengeEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    val builtIn = BuiltInChallenges.isBuiltIn(challenge.id)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = AppShapes.xl,
        icon = {
            IconBubble(background = c.berrySoft, size = 48.dp) {
                Icon(if (builtIn) Icons.Default.VisibilityOff else Icons.Default.Delete, contentDescription = null, tint = c.berry)
            }
        },
        title = {
            Text(
                if (builtIn) "Hide \"${challenge.title}\"?" else "Delete \"${challenge.title}\"?",
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary
            )
        },
        text = {
            val joined = if (challenge.isJoined) " You've joined it, so its habits will be removed too." else ""
            Text(
                if (builtIn) "It's a built-in challenge, so it'll be hidden. You can bring it back any time from Hidden challenges.$joined"
                else "This permanently removes the challenge and its banner photo.$joined",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("btn_confirm_delete_challenge")) {
                Text(if (builtIn) "Hide" else "Delete", style = MaterialTheme.typography.labelLarge, color = c.berry)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
    )
}

/* ---------------------------------------------------------------------------------------- */
/* Hidden challenges                                                                           */
/* ---------------------------------------------------------------------------------------- */

@Composable
internal fun HiddenChallengesDialog(
    hidden: List<ChallengeEntity>,
    onRestore: (ChallengeEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PlantTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        shape = AppShapes.xl,
        title = { Text("Hidden challenges", style = MaterialTheme.typography.titleLarge, color = c.textPrimary) },
        text = {
            if (hidden.isEmpty()) {
                Text("Nothing hidden.", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hidden, key = { it.id }) { ch ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(challengeIcon(ch), contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(ch.title, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onRestore(ch) }, modifier = Modifier.testTag("restore_${ch.id}")) {
                                Text("Restore", style = MaterialTheme.typography.labelLarge, color = c.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", style = MaterialTheme.typography.labelLarge, color = c.primary)
            }
        }
    )
}
