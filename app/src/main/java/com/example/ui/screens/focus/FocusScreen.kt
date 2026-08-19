package com.example.ui.screens.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProgressKpiCard
import com.example.ui.components.XarloCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.FocusPreset
import com.example.ui.viewmodel.XarloViewModel
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: XarloViewModel,
    modifier: Modifier = Modifier
) {
    val focusMode by viewModel.focusMode.collectAsState()
    val isRunning by viewModel.focusRunning.collectAsState()
    val remainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.focusTotalSeconds.collectAsState()
    val currentCycle by viewModel.focusCurrentCycle.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val focusTaskName by viewModel.focusTaskName.collectAsState()
    val todaySessions by viewModel.todayFocusSessions.collectAsState()

    var isEditingTask by remember { mutableStateOf(false) }
    var taskInputText by remember { mutableStateOf(focusTaskName) }

    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else 0f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pomodoro Focus",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Single-task rhythm • Continuous background timer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentViolet.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentViolet.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Cycle $currentCycle / ${selectedPreset.targetCycles}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AccentVioletLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Main Timer Clock Card
        item {
            XarloCard(
                borderColor = if (isRunning) AccentViolet.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (focusMode) {
                            "Focus" -> AccentCyan.copy(alpha = 0.15f)
                            "Short Break" -> StatusGood.copy(alpha = 0.15f)
                            else -> StatusWarn.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (focusMode) {
                                "Focus" -> AccentCyan
                                "Short Break" -> StatusGood
                                else -> StatusWarn
                            }
                        )
                    ) {
                        Text(
                            text = focusMode.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = when (focusMode) {
                                "Focus" -> AccentCyan
                                "Short Break" -> StatusGood
                                else -> StatusWarn
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Big Tabular Timer Text
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AccentViolet, AccentCyan)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Current Task Label with edit
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isEditingTask = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = focusTaskName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer Controls Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.toggleFocusTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) StatusWarn else AccentViolet
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("focus_start_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isRunning) Color.Black else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRunning) "Pause" else "Start",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRunning) Color.Black else Color.White
                            )
                        }

                        Button(
                            onClick = { viewModel.resetFocusTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("focus_reset_button")
                        ) {
                            Text("Reset", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = { viewModel.skipFocusInterval() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("focus_skip_button")
                        ) {
                            Text("Skip", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Focus Technique Presets
        item {
            Text(
                text = "Focus Techniques",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetCard(
                        preset = viewModel.focusPresets[0],
                        isSelected = selectedPreset.name == "Classic",
                        onSelect = { viewModel.setFocusPreset(it) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetCard(
                        preset = viewModel.focusPresets[1],
                        isSelected = selectedPreset.name == "Deep Work",
                        onSelect = { viewModel.setFocusPreset(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetCard(
                        preset = viewModel.focusPresets[2],
                        isSelected = selectedPreset.name == "90-Minute",
                        onSelect = { viewModel.setFocusPreset(it) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetCard(
                        preset = viewModel.focusPresets[3],
                        isSelected = selectedPreset.name == "Mini Sprint",
                        onSelect = { viewModel.setFocusPreset(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // How to Use Guide
        item {
            XarloCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = StatusWarn,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to maximize focus",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GuideBullet("1. Choose one specific task before pressing Start.")
                    GuideBullet("2. During Focus, silence non-essential interruptions.")
                    GuideBullet("3. During Break, stand, stretch, hydrate or walk.")
                    GuideBullet("4. After configured cycles, take a longer recovery break.")
                    GuideBullet("5. Consistency beats intensity — short sessions accumulate.")
                }
            }
        }

        // Today's Completed Sessions History
        item {
            Text(
                text = "Today's Completed Sessions (${todaySessions.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (todaySessions.isEmpty()) {
            item {
                XarloCard {
                    Text(
                        text = "No focus sessions completed yet today. Start your first sprint above!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(todaySessions, key = { it.id }) { session ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusGood,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = session.taskName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${session.presetName} • ${session.durationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentViolet.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "+${session.durationMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentVioletLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Task Dialog
    if (isEditingTask) {
        AlertDialog(
            onDismissRequest = { isEditingTask = false },
            title = { Text("Set Focus Task") },
            text = {
                OutlinedTextField(
                    value = taskInputText,
                    onValueChange = { taskInputText = it },
                    label = { Text("Task / Goal Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskInputText.isNotBlank()) {
                            viewModel.setFocusTask(taskInputText)
                        }
                        isEditingTask = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingTask = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PresetCard(
    preset: FocusPreset,
    isSelected: Boolean,
    onSelect: (FocusPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AccentViolet.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, if (isSelected) AccentViolet else MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable { onSelect(preset) }
            .testTag("preset_card_${preset.name}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) AccentVioletLight else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${preset.focusMinutes} focus / ${preset.shortBreakMinutes} break",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GuideBullet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
