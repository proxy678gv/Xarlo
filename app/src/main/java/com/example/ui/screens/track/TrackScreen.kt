package com.example.ui.screens.track

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitEntity
import com.example.ui.components.HealthDisclaimerCard
import com.example.ui.components.ProgressKpiCard
import com.example.ui.components.XarloCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.HabitWithStatus
import com.example.ui.viewmodel.XarloViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackScreen(
    viewModel: XarloViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habitsWithStatus.collectAsState()
    val allWaterLogs by viewModel.allWaterLogs.collectAsState()
    val allWorkouts by viewModel.allWorkoutSessions.collectAsState()
    val allSleepLogs by viewModel.allSleepLogs.collectAsState()
    val allFocusSessions by viewModel.allFocusSessions.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showLogSleepDialog by remember { mutableStateOf(false) }

    val totalFocusMins = remember(allFocusSessions) {
        allFocusSessions.sumOf { it.durationMinutes }
    }

    val avgSleepDurationHours = remember(allSleepLogs) {
        if (allSleepLogs.isNotEmpty()) {
            val avgMins = allSleepLogs.takeLast(7).map { it.durationMinutes }.average()
            "%.1f".format(Locale.getDefault(), avgMins / 60.0)
        } else "7.5"
    }

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
                        text = "Track & Analytics",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Habits • Sleep • Workouts • Focus consistency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddHabitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_habit_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Habit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Summary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressKpiCard(
                    title = "Workouts",
                    value = "${allWorkouts.size}",
                    subtitle = "sessions logged",
                    accentColor = AccentViolet,
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                ProgressKpiCard(
                    title = "Sleep Avg",
                    value = "${avgSleepDurationHours}h",
                    subtitle = "7-day average",
                    accentColor = AccentCyan,
                    icon = Icons.Default.Bedtime,
                    modifier = Modifier.weight(1f),
                    onClick = { showLogSleepDialog = true }
                )
                ProgressKpiCard(
                    title = "Focus Time",
                    value = "${totalFocusMins / 60}h",
                    subtitle = "${totalFocusMins % 60}m recorded",
                    accentColor = StatusWarn,
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Habit Tracker Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Consistency",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap to toggle today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(habits, key = { it.habit.id }) { habitWithStatus ->
            HabitRowItem(
                item = habitWithStatus,
                onToggle = { viewModel.toggleHabit(habitWithStatus.habit.id) },
                onDelete = { viewModel.deleteHabit(habitWithStatus.habit) }
            )
        }

        // Sleep Tracker Card
        item {
            XarloCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sleep Quality & Rest",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: 10:00 PM – 5:30 AM (7.5 hours)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showLogSleepDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("log_sleep_button")
                    ) {
                        Text(
                            text = "Log Sleep",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentCyan
                        )
                    }
                }

                if (allSleepLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val recentSleep = allSleepLogs.last()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Last Logged: ${recentSleep.dateKey}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(recentSleep.durationMinutes / 60.0).format(1)} hrs • Rating ${recentSleep.qualityRating}/5",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (recentSleep.notes.isNotBlank()) {
                                    Text(
                                        text = recentSleep.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hydration Weekly Trend Chart
        item {
            XarloCard {
                Text(
                    text = "Weekly Hydration Intake",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Volume consumed over last 7 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                HydrationWeeklyBarChart(waterLogs = allWaterLogs)
            }
        }

        // Health Disclaimer Note
        item {
            HealthDisclaimerCard()
        }
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onSave = { title, category, targetDays ->
                viewModel.addHabit(title, category, targetDays)
                showAddHabitDialog = false
            }
        )
    }

    // Log Sleep Dialog
    if (showLogSleepDialog) {
        LogSleepDialog(
            onDismiss = { showLogSleepDialog = false },
            onSave = { bedH, bedM, wakeH, wakeM, rating, notes ->
                viewModel.logSleep(bedH, bedM, wakeH, wakeM, rating, notes)
                showLogSleepDialog = false
            }
        )
    }
}

@Composable
fun HabitRowItem(
    item: HabitWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.isCompletedToday) StatusGood.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("habit_card_${item.habit.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isCompletedToday) StatusGood else Color.Transparent
                            )
                            .border(
                                1.5.dp,
                                if (item.isCompletedToday) StatusGood else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isCompletedToday) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.habit.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.completedThisWeek}/${item.habit.targetDaysPerWeek} days this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.currentStreak > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusWarn.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, StatusWarn.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = StatusWarn,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusWarn
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HydrationWeeklyBarChart(
    waterLogs: List<com.example.data.local.entity.WaterLogEntity>,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val cal = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val dailyVolumes = remember(waterLogs) {
        val list = mutableListOf<Int>()
        val c = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0..6) {
            val dKey = sdf.format(c.time)
            val dayTotal = waterLogs.filter { it.dateKey == dKey }.sumOf { it.amountMl }
            list.add(dayTotal)
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val maxVolume = (dailyVolumes.maxOrNull() ?: 3500).coerceAtLeast(3500)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val barCount = 7
            val barSpacing = size.width / (barCount * 2)
            val barWidth = barSpacing

            for (i in 0 until barCount) {
                val vol = dailyVolumes.getOrElse(i) { 0 }
                val barHeight = (vol.toFloat() / maxVolume.toFloat()) * (size.height - 20f)
                val left = (i * 2 + 0.5f) * barSpacing
                val top = size.height - barHeight

                // Background track
                drawRoundRect(
                    color = Color(0xFF1E2640),
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Fill bar
                if (barHeight > 0) {
                    drawRoundRect(
                        color = if (vol >= 3000) AccentCyan else AccentViolet,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayLabels.forEach { d ->
                Text(
                    text = d,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, targetDays: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetDays by remember { mutableStateOf(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title (e.g. Read 20 pages)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Target days per week: $targetDays", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = targetDays.toFloat(),
                    onValueChange = { targetDays = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, "CUSTOM", targetDays)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet)
            ) {
                Text("Add Habit", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogSleepDialog(
    onDismiss: () -> Unit,
    onSave: (bedH: Int, bedM: Int, wakeH: Int, wakeM: Int, rating: Int, notes: String) -> Unit
) {
    var bedHour by remember { mutableStateOf(22) }
    var bedMin by remember { mutableStateOf(0) }
    var wakeHour by remember { mutableStateOf(5) }
    var wakeMin by remember { mutableStateOf(30) }
    var rating by remember { mutableStateOf(5) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sleep Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bedHour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 0..23) bedHour = h } },
                        label = { Text("Bed Hour (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wakeHour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 0..23) wakeHour = h } },
                        label = { Text("Wake Hour (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Sleep Quality (1-5): $rating", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Woke up refreshed)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(bedHour, bedMin, wakeHour, wakeMin, rating, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("Save Sleep", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun Double.format(digits: Int) = "%.${digits}f".format(Locale.getDefault(), this)
