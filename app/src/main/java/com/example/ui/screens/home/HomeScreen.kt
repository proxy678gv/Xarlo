package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.InitialData
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.XarloViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: XarloViewModel,
    onNavigateTo: (String) -> Unit,
    onOpenZlva: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedulesWithStatus.collectAsState()
    val nextItem by viewModel.nextScheduledItem.collectAsState()
    val waterSlots by viewModel.waterSlotsState.collectAsState()
    val waterTotalMl by viewModel.todayWaterTotalMl.collectAsState()
    val habits by viewModel.habitsWithStatus.collectAsState()
    val todayFocusSessions by viewModel.todayFocusSessions.collectAsState()

    val totalSchedules = schedules.size
    val completedSchedules = schedules.count { it.isCompletedToday }
    val schedulePercent = if (totalSchedules > 0) (completedSchedules * 100) / totalSchedules else 0

    val todayWaterCount = waterSlots.count { it }
    val totalFocusCompleted = todayFocusSessions.size

    val dateFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date & Greeting Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = dateFormatted.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentVioletLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Plan your day.\nFocus on what matters.",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Hero Card with Cosmic background
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CosmicPanel,
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Cosmic hero background image with dark overlay
                    Image(
                        painter = painterResource(id = R.drawable.bg_xarlo_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        CosmicBackground.copy(alpha = 0.6f),
                                        CosmicPanel.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEXT PLANNED ITEM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = AccentCyan
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CosmicPanelElevated.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, CosmicBorder)
                                ) {
                                    Text(
                                        text = "4 workout days",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextSecondaryDark,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CosmicPanelElevated.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, CosmicBorder)
                                ) {
                                    Text(
                                        text = "7 water goals",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextSecondaryDark,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (nextItem != null) {
                            Text(
                                text = "${nextItem!!.item.timeStr} — ${nextItem!!.item.title}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 21.sp
                                ),
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextItem!!.item.note.ifEmpty { "Scheduled activity" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        } else {
                            Text(
                                text = "All activities completed!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = StatusGood
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Great discipline today. Check out focus or workouts below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hero quick action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateTo("focus") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_start_focus_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Focus", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = { onNavigateTo("schedule") },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicPanelElevated),
                                border = BorderStroke(1.dp, CosmicBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_today_schedule_button")
                            ) {
                                Text(
                                    "Today's Plan",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "Add Water",
                    icon = Icons.Default.WaterDrop,
                    tint = AccentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.addCustomWater(250) }
                )
                QuickActionChip(
                    label = "Workouts",
                    icon = Icons.Default.FitnessCenter,
                    tint = AccentViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("schedule") }
                )
                QuickActionChip(
                    label = "Ask ZLVA",
                    icon = Icons.Default.AutoAwesome,
                    tint = AccentVioletLight,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenZlva
                )
            }
        }

        // KPI Progress Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressKpiCard(
                    title = "Today's Focus",
                    value = "$totalFocusCompleted",
                    subtitle = "sessions",
                    accentColor = AccentViolet,
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("focus") }
                )
                ProgressKpiCard(
                    title = "Water Intake",
                    value = "$todayWaterCount / 7",
                    subtitle = "$waterTotalMl ml",
                    accentColor = AccentCyan,
                    icon = Icons.Default.WaterDrop,
                    modifier = Modifier.weight(1f)
                )
                ProgressKpiCard(
                    title = "Schedule",
                    value = "$schedulePercent%",
                    subtitle = "$completedSchedules of $totalSchedules",
                    accentColor = StatusGood,
                    icon = Icons.Default.CheckCircleOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("schedule") }
                )
            }
        }

        // Water Hydration Tracker Section
        item {
            XarloCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Water Intake",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Supplied plan target: 3–3.5 L/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AccentCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "$waterTotalMl ml",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(InitialData.waterSlots) { index, slotTime ->
                        val isFilled = waterSlots.getOrElse(index) { false }
                        GlassWaterWidget(
                            timeSlot = slotTime,
                            isFilled = isFilled,
                            onToggle = { viewModel.toggleWaterSlot(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap a glass to log 250 ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = { viewModel.addCustomWater(250) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("+250 ml Custom", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                    }
                }
            }
        }

        // Today's Plan Preview List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tap circle to toggle completion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = { onNavigateTo("schedule") }) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AccentVioletLight
                    )
                }
            }
        }

        // Top schedule items (Preview 5 items)
        val previewItems = schedules.take(5)
        items(previewItems, key = { it.item.id }) { scheduleWithStatus ->
            ScheduleRowItem(
                item = scheduleWithStatus,
                onToggle = { viewModel.toggleSchedule(scheduleWithStatus.item.id) }
            )
        }

        // Wake • Mobility • Sleep Info Card
        item {
            XarloCard {
                Text(
                    text = "Wake • Mobility • Sleep Routine",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoutineBox(
                        title = "5:30 AM Wake",
                        subtitle = "Water, sunlight & morning stretch",
                        icon = Icons.Default.WbSunny,
                        tint = StatusWarn,
                        modifier = Modifier.weight(1f)
                    )
                    RoutineBox(
                        title = "Mobility",
                        subtitle = "Tadasana, Marjariasana, Shavasana",
                        icon = Icons.Default.SelfImprovement,
                        tint = AccentViolet,
                        modifier = Modifier.weight(1f)
                    )
                    RoutineBox(
                        title = "10:00 PM Sleep",
                        subtitle = "7.5 hrs planned rest",
                        icon = Icons.Default.Bedtime,
                        tint = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Health Disclaimer Card
        item {
            HealthDisclaimerCard()
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_action_$label")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ScheduleRowItem(
    item: com.example.ui.viewmodel.ScheduleWithStatus,
    onToggle: () -> Unit,
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
            .testTag("schedule_row_${item.item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Time tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentViolet.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentViolet.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = item.item.timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = AccentVioletLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (item.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (item.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (item.item.note.isNotBlank()) {
                        Text(
                            text = item.item.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

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
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
