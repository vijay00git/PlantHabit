package com.planthabit.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.planthabit.data.repository.ChallengeJsonManager
import com.planthabit.ui.components.bounceClick
import com.planthabit.ui.components.rememberHaptics
import com.planthabit.ui.theme.AccentPair
import com.planthabit.ui.theme.AppShapes
import com.planthabit.ui.theme.PlantTheme
import kotlinx.coroutines.delay

@Composable
fun TemplateJsonDialog(
    onDismiss: () -> Unit,
    onUseTemplateInImport: (String) -> Unit
) {
    val context = LocalContext.current
    val c = PlantTheme.colors
    val haptics = rememberHaptics()
    val templateJson = remember { ChallengeJsonManager.getExampleTemplateJson() }

    // Copy confirmation: each tap bumps the counter, which restarts the "Copied ✓" timer.
    var copyCount by remember { mutableIntStateOf(0) }
    var showCopied by remember { mutableStateOf(false) }
    LaunchedEffect(copyCount) {
        if (copyCount > 0) {
            showCopied = true
            delay(1600)
            showCopied = false
        }
    }

    // Save document launcher
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(templateJson.toByteArray())
                    haptics.success()
                    Toast.makeText(context, "Saved template file successfully! 📄", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                haptics.reject()
                Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ChallengeDialogSurface {
            ChallengeDialogHeader(
                icon = Icons.Default.DataObject,
                accent = AccentPair(c.sun, c.sunSoft),
                title = "Challenge JSON Template",
                subtitle = "Use this format to build your own challenges",
                onClose = onDismiss
            )

            // Description note
            Text(
                text = "Edit the fields below, change durationDays and add habits. Save this file to your phone, customize it, and import it at any time!",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary
            )

            // JSON code box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(AppShapes.medium)
                    .background(c.cardMuted)
                    .border(1.dp, c.border, AppShapes.medium)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    Text(
                        text = templateJson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = c.textPrimary
                    )
                }

                // "JSON" language tag in the corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(AppShapes.pill)
                        .background(c.sunSoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "JSON", style = MaterialTheme.typography.labelSmall, color = c.sun)
                }
            }

            // Action Buttons: Download JSON file, Copy to Clipboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChallengeOutlinedButton(
                    text = "Download .json",
                    icon = Icons.Default.Download,
                    accent = c.sun,
                    onClick = { saveFileLauncher.launch("sprout_challenge_template.json") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_download_template_file"),
                    height = 46.dp
                )

                // Copy with animated "Copied ✓" confirmation
                val copyAccent = if (showCopied) c.primary else c.sky
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .bounceClick(haptic = false) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("PlantHabit Challenge Template", templateJson)
                            clipboard.setPrimaryClip(clip)
                            haptics.success()
                            copyCount++
                        }
                        .clip(AppShapes.medium)
                        .background(if (showCopied) c.primarySoft else c.skySoft)
                        .border(1.dp, copyAccent.copy(alpha = 0.45f), AppShapes.medium)
                        .testTag("btn_copy_template_json"),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = showCopied,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.8f)) togetherWith
                                (fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                        },
                        label = "copyState"
                    ) { copied ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (copied) c.primary else c.sky,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (copied) "Copied ✓" else "Copy JSON",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (copied) c.primary else c.sky
                            )
                        }
                    }
                }
            }

            ChallengeGradientButton(
                text = "Use Template in Import Editor",
                icon = Icons.Default.PlayArrow,
                onClick = {
                    onUseTemplateInImport(templateJson)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_load_into_import")
            )
        }
    }
}
