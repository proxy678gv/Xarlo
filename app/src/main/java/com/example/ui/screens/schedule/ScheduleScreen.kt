package com.example.ui.screens.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InitialData
import com.example.data.local.WorkoutDayPlan
import com.example.data.local.entity.ScheduleItemEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScheduleWithStatus
import com.example.ui.viewmodel.XarloViewModel

@Composable
fun ScheduleScreen(
    viewModel: XarloViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedulesWithStatus.collectAsState()
    val selectedWorkoutDay by viewModel.selectedWorkoutDay.collectAsState()
    val activeWorkoutState by viewModel.activeWorkoutState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ScheduleItemEntity?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "WATER", "MEDICATION", "MEAL", "WORKOUT", "SLEEP", "CUSTOM")

    val filteredSchedules = remember(schedules, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            schedules
        } else {
            schedules.filter { it.item.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentViolet,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("add_schedule_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Schedule Activity",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = "Daily Schedule",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tap a check when completed • Survives reboots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.resetTodayProgress() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("reset_today_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Category Filter Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentViolet else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) AccentViolet else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = cat }
                                .testTag("category_filter_$cat")
                        ) {
                            Text(
                                text = cat.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Schedule Items List
            if (filteredSchedules.isEmpty()) {
                item {
                    XarloCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No activities in this category",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredSchedules, key = { it.item.id }) { itemWithStatus ->
                    ScheduleFullRow(
                        item = itemWithStatus,
                        onToggle = { viewModel.toggleSchedule(itemWithStatus.item.id) },
                        onEdit = { editingItem = itemWithStatus.item }
                    )
                }
            }

            // 4-Day Home Workout Plan Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "4-Day Home Workout Plan",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Form over speed • Built from supplied plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Workout Day Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(InitialData.workoutPlans) { index, plan ->
                        val isSelected = selectedWorkoutDay == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentViolet else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) AccentViolet else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .clickable { viewModel.selectWorkoutDay(index) }
                                .testTag("workout_day_tab_${plan.dayNumber}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = plan.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) AccentVioletLight else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Selected Workout Day Details Card
            val currentPlan = InitialData.workoutPlans.getOrElse(selectedWorkoutDay) { InitialData.workoutPlans[0] }
            item {
                WorkoutPlanCard(
                    plan = currentPlan,
                    activeState = activeWorkoutState,
                    onStartWorkout = { viewModel.startWorkoutSession(selectedWorkoutDay) },
                    onToggleExercise = { ex -> viewModel.toggleExerciseDone(ex) },
                    onFinishWorkout = { viewModel.finishWorkoutSession() },
                    onSkipRest = { viewModel.skipRestTimer() }
                )
            }

            // Health Note Disclaimer
            item {
                HealthDisclaimerCard()
            }
        }
    }

    // Add Schedule Dialog
    if (showAddDialog) {
        AddEditScheduleDialog(
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, timeStr, hour, minute, recurrence, note ->
                viewModel.addSchedule(title, timeStr, hour, minute, recurrence, note)
                showAddDialog = false
            }
        )
    }

    // Edit Schedule Dialog
    if (editingItem != null) {
        AddEditScheduleDialog(
            initialItem = editingItem,
            onDismiss = { editingItem = null },
            onSave = { title, timeStr, hour, minute, recurrence, note ->
                val updated = editingItem!!.copy(
                    title = title,
                    timeStr = timeStr,
                    hour = hour,
                    minute = minute,
                    recurrence = recurrence,
                    note = note
                )
                viewModel.updateSchedule(updated)
                editingItem = null
            },
            onDelete = {
                viewModel.deleteSchedule(editingItem!!.id)
                editingItem = null
            }
        )
    }
}

@Composable
fun ScheduleFullRow(
    item: ScheduleWithStatus,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.isCompletedToday) StatusGood.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("schedule_card_${item.item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Time box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentViolet.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentViolet.copy(alpha = 0.3f)),
                    modifier = Modifier.width(76.dp)
                ) {
                    Text(
                        text = item.item.timeStr,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = AccentVioletLight,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.item.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (item.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (item.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                        if (item.item.recurrence != "DAILY") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = item.item.recurrence.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (item.item.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.item.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_schedule_check_${item.item.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
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
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutPlanCard(
    plan: WorkoutDayPlan,
    activeState: com.example.ui.viewmodel.ActiveWorkoutState,
    onStartWorkout: () -> Unit,
    onToggleExercise: (com.example.data.local.Exercise) -> Unit,
    onFinishWorkout: () -> Unit,
    onSkipRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    XarloCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${plan.title} • ${plan.subtitle}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!activeState.isWorkoutActive || activeState.plan.dayNumber != plan.dayNumber) {
                Button(
                    onClick = onStartWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("start_workout_button_${plan.dayNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Session", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Button(
                    onClick = onFinishWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGood),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("finish_workout_button_${plan.dayNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finish (${activeState.elapsedSeconds / 60}m)", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Rest Timer Banner if active
        if (activeState.isResting && activeState.plan.dayNumber == plan.dayNumber) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentCyan.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Resting after ${activeState.currentRestingExerciseName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeState.restRemainingSeconds}s remaining",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AccentCyan
                            )
                        }
                    }

                    TextButton(onClick = onSkipRest) {
                        Text("Skip Rest", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Exercises
        plan.exercises.forEachIndexed { idx, ex ->
            val isDone = activeState.completedExerciseIds.contains(ex.id) && activeState.plan.dayNumber == plan.dayNumber

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isDone) StatusGood.copy(alpha = 0.4f) else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        onToggleExercise(ex)
                    }
                    .testTag("exercise_item_${ex.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${idx + 1}. ${ex.name}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentViolet.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = ex.target,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = AccentVioletLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ex.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) StatusGood else Color.Transparent
                            )
                            .border(
                                1.5.dp,
                                if (isDone) StatusGood else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
