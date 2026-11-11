package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.ChallengeJsonManager
import com.example.ui.theme.SproutAccentAmber
import com.example.ui.theme.SproutAccentGreenGlow
import com.example.ui.theme.SproutCardBorderDark
import com.example.ui.theme.SproutPrimary
import com.example.ui.theme.SproutPrimaryLight

@Composable
fun ImportChallengeDialog(
    initialJson: String = "",
    onDismiss: () -> Unit,
    onImport: (String) -> Unit,
    onOpenTemplate: () -> Unit
) {
    val context = LocalContext.current
    var jsonText by remember { mutableStateOf(initialJson) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var parsedTitle by remember { mutableStateOf<String?>(null) }
    var parsedHabitCount by remember { mutableStateOf(0) }

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
                    Toast.makeText(context, "Loaded JSON file successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF161421))
                .border(1.dp, SproutCardBorderDark, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SproutPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Import Challenge",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Upload or paste challenge JSON",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262335))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Action buttons: Pick File & View Template
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("application/json") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pick_json_file"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221E33)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = SproutPrimaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Pick .json File", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Button(
                        onClick = onOpenTemplate,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_get_template"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2038)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = SproutAccentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Get Template", fontSize = 12.sp, color = SproutAccentAmber)
                        }
                    }
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
                    label = { Text("Paste Challenge JSON", color = Color(0xFF94A3B8)) },
                    placeholder = {
                        Text(
                            "{\n  \"id\": \"my_challenge\",\n  \"title\": \"30-Day Focus\",\n  \"durationDays\": 30,\n  \"habits\": [...]\n}",
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SproutPrimaryLight,
                        unfocusedBorderColor = Color(0xFF332D42),
                        focusedContainerColor = Color(0xFF100F18),
                        unfocusedContainerColor = Color(0xFF100F18)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Validation Status Banner
                if (parsedTitle != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F2E1E))
                            .border(1.dp, SproutAccentGreenGlow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SproutAccentGreenGlow, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Valid: '$parsedTitle' ($parsedHabitCount linked habits)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SproutAccentGreenGlow
                            )
                        }
                    }
                } else if (parseError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF38141D))
                            .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(16.dp))
                            Text(
                                text = parseError ?: "JSON error",
                                fontSize = 11.sp,
                                color = Color(0xFFFB7185)
                            )
                        }
                    }
                }

                // Import Button
                Button(
                    onClick = {
                        if (jsonText.isNotBlank()) {
                            onImport(jsonText.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_import_challenge"),
                    colors = ButtonDefaults.buttonColors(containerColor = SproutPrimary),
                    shape = RoundedCornerShape(14.dp),
                    enabled = jsonText.isNotBlank() && parseError == null
                ) {
                    Text("Import & Start Challenge 🚀", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
