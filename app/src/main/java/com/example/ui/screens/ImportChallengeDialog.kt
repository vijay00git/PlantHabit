package com.example.ui.screens

import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.AddPhotoAlternate
import coil.compose.AsyncImage
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.ChallengeJsonManager
import com.example.ui.components.IconBubble
import com.example.ui.components.bounceClick
import com.example.ui.components.rememberHaptics
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.leafGradient

@Composable
fun ImportChallengeDialog(
    initialJson: String = "",
    onDismiss: () -> Unit,
    onImport: (json: String, bannerPhoto: Uri?) -> Unit,
    onOpenTemplate: () -> Unit
) {
    val context = LocalContext.current
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    var jsonText by remember { mutableStateOf(initialJson) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var parsedTitle by remember { mutableStateOf<String?>(null) }
    var parsedHabitCount by remember { mutableStateOf(0) }
    var bannerPhoto by remember { mutableStateOf<Uri?>(null) }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            haptics.success()
            bannerPhoto = uri
        }
    }

    // Validate preview whenever text changes
    fun validateCurrentJson(text: String) {
        if (text.isBlank()) {
            parseError = null
            parsedTitle = null
            parsedHabitCount = 0
            return
        }
        val result = ChallengeJsonManager.parseChallengeJson(text)
        result.fold(
            onSuccess = { (challenge, habits) ->
                parseError = null
                parsedTitle = challenge.title
                parsedHabitCount = habits.size
            },
            onFailure = { error ->
                parseError = error.localizedMessage ?: "Invalid JSON format"
                parsedTitle = null
                parsedHabitCount = 0
            }
        )
    }

    // Show validation immediately when opened with a pre-filled draft (e.g. from the template).
    LaunchedEffect(Unit) { validateCurrentJson(jsonText) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    jsonText = content
                    validateCurrentJson(content)
                    haptics.success()
                    Toast.makeText(context, "Loaded JSON file successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                haptics.reject()
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ChallengeDialogSurface {
            ChallengeDialogHeader(
                icon = Icons.Default.FileUpload,
                accent = AccentPair(c.primary, c.primarySoft),
                title = "Import Challenge",
                subtitle = "Upload or paste challenge JSON",
                onClose = onDismiss
            )

            // Action buttons: Pick File & View Template
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChallengeOutlinedButton(
                    text = "Pick .json",
                    icon = Icons.Default.FolderOpen,
                    accent = c.primary,
                    onClick = { filePickerLauncher.launch("application/json") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_pick_json_file")
                )
                ChallengeOutlinedButton(
                    text = "Get Template",
                    icon = Icons.Default.Download,
                    accent = c.sun,
                    onClick = onOpenTemplate,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_get_template")
                )
            }

            // JSON Text Input
            OutlinedTextField(
                value = jsonText,
                onValueChange = {
                    jsonText = it
                    validateCurrentJson(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("input_challenge_json"),
                label = { Text("Paste Challenge JSON") },
                placeholder = {
                    Text(
                        "{\n  \"id\": \"my_challenge\",\n  \"title\": \"30-Day Focus\",\n  \"durationDays\": 30,\n  \"habits\": [...]\n}",
                        color = c.textMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = c.textPrimary
                ),
                colors = challengeTextFieldColors(),
                shape = AppShapes.medium
            )

            // Validation status banner (animates between none / valid / error)
            AnimatedContent(
                targetState = parsedTitle to parseError,
                contentKey = { (title, error) ->
                    when {
                        title != null -> "valid"
                        error != null -> "error"
                        else -> "none"
                    }
                },
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.96f)) togetherWith
                        fadeOut(tween(150))
                },
                label = "jsonValidation"
            ) { (title, error) ->
                when {
                    title != null -> StatusBanner(
                        icon = Icons.Default.Check,
                        text = "Valid: '$title' ($parsedHabitCount linked habits)",
                        accent = AccentPair(c.primary, c.primarySoft)
                    )
                    error != null -> StatusBanner(
                        icon = Icons.Default.Info,
                        text = error,
                        accent = AccentPair(c.berry, c.berrySoft)
                    )
                    else -> Box(Modifier.fillMaxWidth())
                }
            }

            // Optional banner photo for the imported challenge
            BannerPhotoPicker(
                photo = bannerPhoto,
                onPick = {
                    bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onClear = {
                    haptics.tick()
                    bannerPhoto = null
                }
            )

            // Import Button
            ChallengeGradientButton(
                text = "Import & Start Challenge",
                icon = Icons.Default.RocketLaunch,
                onClick = {
                    if (jsonText.isNotBlank()) {
                        onImport(jsonText.trim(), bannerPhoto)
                    }
                },
                enabled = jsonText.isNotBlank() && parseError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_submit_import_challenge")
            )
        }
    }
}

/** Empty "add photo" slot, or a preview of the chosen photo with change/remove actions. */
@Composable
private fun BannerPhotoPicker(
    photo: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    val c = PlantTheme.colors
    AnimatedContent(
        targetState = photo,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
        label = "bannerPicker"
    ) { current ->
        if (current == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(onClick = onPick)
                    .clip(AppShapes.medium)
                    .background(c.cardMuted)
                    .border(1.dp, c.border, AppShapes.medium)
                    .padding(14.dp)
                    .testTag("btn_import_pick_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconBubble(background = c.skySoft, size = 40.dp) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = c.sky, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Add a banner photo", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                    Text(
                        "Optional · shown on the challenge card and details",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(AppShapes.medium)
            ) {
                AsyncImage(
                    model = current,
                    contentDescription = "Selected banner photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PhotoOverlayButton(Icons.Default.AddPhotoAlternate, "Change banner photo", onPick)
                    PhotoOverlayButton(Icons.Default.Close, "Remove banner photo", onClear)
                }
            }
        }
    }
}

@Composable
private fun PhotoOverlayButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .bounceClick(onClick = onClick)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun StatusBanner(icon: ImageVector, text: String, accent: AccentPair) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.small)
            .background(accent.soft)
            .border(1.dp, accent.color.copy(alpha = 0.35f), AppShapes.small)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent.color, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = accent.color)
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Shared dialog building blocks for the Challenge dialogs                                      */
/* (ImportChallengeDialog, TemplateJsonDialog, ChallengeDetailsDialog, ChallengesScreen)        */
/* ------------------------------------------------------------------------------------------ */

/** Themed dialog container: surface, hairline border, xl corners, scrollable column. */
@Composable
internal fun ChallengeDialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = PlantTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .clip(AppShapes.xl)
            .background(c.surface)
            .border(1.dp, c.border, AppShapes.xl)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

/** Dialog title row: tinted icon bubble, title/subtitle and a round close button. */
@Composable
internal fun ChallengeDialogHeader(
    icon: ImageVector,
    accent: AccentPair,
    title: String,
    subtitle: String,
    onClose: () -> Unit,
) {
    val c = PlantTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(background = accent.soft, size = 44.dp) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent.color,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        ChallengeCloseButton(onClick = onClose)
    }
}

/** Small round close button. [onPhoto] uses a dark translucent backing for use over images. */
@Composable
internal fun ChallengeCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPhoto: Boolean = false,
) {
    val c = PlantTheme.colors
    Box(
        modifier = modifier
            .size(34.dp)
            .bounceClick(pressedScale = 0.88f, onClick = onClick)
            .clip(AppShapes.pill)
            .background(if (onPhoto) Color.Black.copy(alpha = 0.35f) else c.cardMuted),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = if (onPhoto) Color.White else c.textPrimary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/** Filled primary action on a gradient (leafGradient by default). */
@Composable
internal fun ChallengeGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    brush: Brush? = null,
    contentColor: Color? = null,
    height: Dp = 52.dp,
    haptic: Boolean = true,
) {
    val c = PlantTheme.colors
    val fg by animateColorAsState(
        targetValue = if (enabled) (contentColor ?: c.onPrimary) else c.textMuted,
        animationSpec = tween(250),
        label = "gradientButtonFg"
    )
    Box(
        modifier = modifier
            .height(height)
            .bounceClick(enabled = enabled, haptic = haptic, onClick = onClick)
            .clip(AppShapes.medium)
            .background(if (enabled) (brush ?: c.leafGradient) else Brush.linearGradient(listOf(c.cardMuted, c.cardMuted))),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
        }
    }
}

/** Secondary outlined action: accent-coloured hairline, label and icon. */
@Composable
internal fun ChallengeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color? = null,
    height: Dp = 48.dp,
    haptic: Boolean = true,
) {
    val c = PlantTheme.colors
    val fg = accent ?: c.textSecondary
    Box(
        modifier = modifier
            .height(height)
            .bounceClick(haptic = haptic, onClick = onClick)
            .clip(AppShapes.medium)
            .border(1.dp, if (accent != null) accent.copy(alpha = 0.45f) else c.border, AppShapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
        }
    }
}

/** Theme-aware OutlinedTextField colours shared by challenge dialogs. */
@Composable
internal fun challengeTextFieldColors() = PlantTheme.colors.let { c ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.border,
        focusedContainerColor = c.cardMuted,
        unfocusedContainerColor = c.cardMuted,
        cursorColor = c.primary,
        focusedLabelColor = c.primary,
        unfocusedLabelColor = c.textSecondary,
        focusedPlaceholderColor = c.textMuted,
        unfocusedPlaceholderColor = c.textMuted,
    )
}
