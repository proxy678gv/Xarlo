package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ai.zlva.ZlvaIntent
import com.example.ai.zlva.ZlvaMessage
import com.example.data.local.entity.ScheduleItemEntity
import com.example.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZlvaAssistantModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    messages: List<ZlvaMessage>,
    onSendMessage: (String) -> Unit,
    onExecuteIntent: (ZlvaIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = CosmicBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CosmicBorder) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
            .fillMaxHeight(0.92f)
            .testTag("zlva_bottom_sheet")
    ) {
        var inputText by remember { mutableStateOf("") }
        val listState = rememberLazyListState()

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(AccentViolet, AccentCyan)
                                )
                            )
                            .padding(1.5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_xarlo_icon),
                            contentDescription = "ZLVA",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(11.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ZLVA AI Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusGood.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = StatusGood
                                )
                            }
                        }
                        Text(
                            text = "Deterministic Natural Language Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
            }

            // Quick command chips
            val quickPrompts = listOf(
                "Add reading at 8 PM",
                "Add 250 ml water",
                "Start Deep Work",
                "Move workout to 7 PM",
                "Mark workout complete",
                "Change sleep time to 10 PM",
                "How much water have I had today?",
                "Remind me to drink water every 2 hours"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicPanelElevated,
                        border = BorderStroke(1.dp, CosmicBorder),
                        modifier = Modifier.clickable {
                            onSendMessage(prompt)
                        }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = AccentVioletLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ZlvaMessageBubble(
                        message = msg,
                        onExecuteIntent = onExecuteIntent
                    )
                }
            }

            // Input Bar
            Surface(
                color = CosmicPanel,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask ZLVA (e.g. Add study at 8 PM)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedDark
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("zlva_input_field")
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) AccentViolet else CosmicPanelElevated
                            )
                            .testTag("zlva_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else TextMutedDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZlvaMessageBubble(
    message: ZlvaMessage,
    onExecuteIntent: (ZlvaIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            color = if (message.isUser) AccentViolet else CosmicPanel,
            border = if (message.isUser) null else BorderStroke(1.dp, CosmicBorder),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = if (message.isUser) Color.White else TextPrimaryDark
                )

                // Structured Action Confirmation Card
                if (message.pendingIntent != null && message.structuredPreviewTitle != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicBackground,
                        border = BorderStroke(1.dp, AccentViolet.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.structuredPreviewTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentVioletLight
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            message.structuredPreviewDetails?.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onExecuteIntent(message.pendingIntent) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("zlva_confirm_action_button")
                                ) {
                                    Text(
                                        text = "Confirm & Apply",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditScheduleDialog(
    initialItem: ScheduleItemEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, timeStr: String, hour: Int, minute: Int, recurrence: String, note: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var hour by remember { mutableStateOf(initialItem?.hour ?: 8) }
    var minute by remember { mutableStateOf(initialItem?.minute ?: 0) }
    var recurrence by remember { mutableStateOf(initialItem?.recurrence ?: "DAILY") }
    var note by remember { mutableStateOf(initialItem?.note ?: "") }

    val recurrenceOptions = listOf("DAILY", "ONCE", "WEEKDAYS", "WEEKENDS", "WEEKLY")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (initialItem == null) "Add Schedule Activity" else "Edit Activity",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title (e.g. Workout / Medication)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time picker row
                Text(
                    text = "Time (24h / AM-PM)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", hour),
                        onValueChange = { str ->
                            val h = str.toIntOrNull()
                            if (h != null && h in 0..23) hour = h
                        },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", minute),
                        onValueChange = { str ->
                            val m = str.toIntOrNull()
                            if (m != null && m in 0..59) minute = m
                        },
                        label = { Text("Min (0-59)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurrence
                Text(
                    text = "Recurrence",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(recurrenceOptions) { opt ->
                        val isSel = recurrence == opt
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) AccentViolet else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) AccentViolet else MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { recurrence = opt }
                        ) {
                            Text(
                                text = opt.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Dosage / Instructions") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = StatusDanger)
                        ) {
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val ampm = if (hour >= 12) "PM" else "AM"
                                    val dH = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                                    val timeStr = String.format(Locale.getDefault(), "%d:%02d %s", dH, minute, ampm)
                                    onSave(title, timeStr, hour, minute, recurrence, note)
                                }
                            },
                            enabled = title.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_schedule_button")
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
