package com.momentum.app.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.SourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailSheet(
    habit: Habit?,
    onDismiss: () -> Unit,
    onSave: (Habit) -> Unit,
    onArchive: ((Long) -> Unit)?,
    onDelete: ((Habit) -> Unit)?
) {
    if (habit == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(habit) { mutableStateOf(habit.name) }
    var colorHex by remember(habit) { mutableStateOf(habit.colorHex) }
    var iconKey by remember(habit) { mutableStateOf(habit.iconKey) }
    var sourceType by remember(habit) { mutableStateOf(habit.sourceType) }
    var targetValue by remember(habit) {
        mutableStateOf(habit.healthConnectTargetValue?.toString() ?: when (habit.sourceType) {
            SourceType.HEALTH_CONNECT_SLEEP -> "7.0"
            SourceType.HEALTH_CONNECT_STEPS -> "8000"
            else -> ""
        })
    }
    var reminderTimesList by remember(habit) {
        val initial = habit.getReminderTimesList()
        mutableStateOf(if (initial.isNotEmpty()) initial else listOf(Pair(9, 0)))
    }
    var hasReminder by remember(habit) { mutableStateOf(habit.getReminderTimesList().isNotEmpty()) }

    val presetColors = listOf("#7DD8C6", "#FFB84D", "#B08CFF", "#FF7A6E", "#64B5F6", "#F48FB1")

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceContainer,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (habit.id == 0L) "New Habit" else "Edit Habit",
                color = onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = outlineColor,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = onSurfaceVariant,
                    focusedTextColor = onSurface,
                    unfocusedTextColor = onSurface
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Color picker
            Text("Color Accent", color = onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                presetColors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Icon picker
            Text("Icon", color = onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(IconHelper.availableKeys) { key ->
                    val isSelected = iconKey.equals(key, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) surfaceVariant else surfaceContainer)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) primaryColor else outlineColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { iconKey = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIcon(key),
                            contentDescription = key,
                            tint = if (isSelected) primaryColor else onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tracking Source Selector (Health Connect vs Manual)
            Text("Tracking Method", color = onSurfaceVariant, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val sources = listOf(
                    Pair(SourceType.MANUAL, "Manual Log"),
                    Pair(SourceType.HEALTH_CONNECT_SLEEP, "Health Connect Sleep"),
                    Pair(SourceType.HEALTH_CONNECT_STEPS, "Health Connect Steps")
                )
                sources.forEach { (type, label) ->
                    val isSel = sourceType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) primaryColor.copy(alpha = 0.15f) else surfaceVariant)
                            .border(1.dp, if (isSel) primaryColor else outlineColor, RoundedCornerShape(12.dp))
                            .clickable { sourceType = type }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) primaryColor else onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (sourceType != SourceType.MANUAL) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = {
                        Text(if (sourceType == SourceType.HEALTH_CONNECT_SLEEP) "Target Hours (e.g., 7.5)" else "Target Step Count (e.g., 8000)")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = outlineColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = onSurfaceVariant,
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Habit Specific Reminder Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceVariant)
                    .border(1.dp, outlineColor, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Habit Reminders", color = onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Notify specifically for this habit", color = onSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = onSurfaceVariant,
                            uncheckedTrackColor = surfaceContainer
                        )
                    )
                }

                if (hasReminder) {
                    ReminderTimesManager(
                        times = reminderTimesList,
                        onTimesChanged = { reminderTimesList = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val timesToSave = if (hasReminder && reminderTimesList.isNotEmpty()) reminderTimesList else emptyList()
                        val parsedTarget = targetValue.toDoubleOrNull()
                        onSave(
                            habit.copy(
                                name = name.trim(),
                                colorHex = colorHex,
                                iconKey = iconKey,
                                sourceType = sourceType,
                                healthConnectTargetValue = parsedTarget,
                                reminderTimes = Habit.timesListToString(timesToSave),
                                reminderHour = timesToSave.firstOrNull()?.first,
                                reminderMinute = timesToSave.firstOrNull()?.second
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Habit", fontWeight = FontWeight.Bold)
            }

            if (habit.id != 0L) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onArchive != null) {
                        TextButton(
                            onClick = {
                                onArchive(habit.id)
                                onDismiss()
                            }
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Archive", color = onSurfaceVariant)
                        }
                    }

                    if (onDelete != null) {
                        TextButton(
                            onClick = {
                                onDelete(habit)
                                onDismiss()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
