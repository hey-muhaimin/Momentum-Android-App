package com.momentum.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momentum.app.data.model.Habit
import com.momentum.app.ui.components.IconHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        starterHabits: List<Habit>,
        initialMoodScore: Int?,
        completedHabitNames: Set<String>,
        reminderHour: Int,
        reminderMinute: Int,
        enableReminder: Boolean
    ) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var currentStep by remember { mutableIntStateOf(0) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Seeded Suggestions
    val defaultSuggestions = remember {
        mutableStateListOf(
            Habit(name = "Sleep 7+ hrs", colorHex = "#7DD8C6", iconKey = "sleep", sortOrder = 0),
            Habit(name = "Move 20 min", colorHex = "#FFB84D", iconKey = "exercise", sortOrder = 1),
            Habit(name = "Read", colorHex = "#B08CFF", iconKey = "book", sortOrder = 2),
            Habit(name = "No phone in bed", colorHex = "#FF7A6E", iconKey = "phone", sortOrder = 3),
            Habit(name = "Drink water", colorHex = "#64B5F6", iconKey = "water", sortOrder = 4),
            Habit(name = "Journal", colorHex = "#A8E6CF", iconKey = "journal", sortOrder = 5)
        )
    }

    // Selected starter habits (Pre-select NONE)
    val selectedStarterHabits = remember { mutableStateListOf<Habit>() }

    // Dialog state for custom habit
    var showCustomHabitDialog by remember { mutableStateOf(false) }
    var customHabitName by remember { mutableStateOf("") }

    // Step 3 state
    var selectedMood by remember { mutableStateOf<Int?>(null) }
    val completedHabitNamesForToday = remember { mutableStateListOf<String>() }

    // Step 4 state
    var reminderHour by remember { mutableIntStateOf(20) } // Default 8:00 PM
    var reminderMinute by remember { mutableIntStateOf(0) }
    var showTimeDropdown by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onComplete(
            selectedStarterHabits.toList(),
            selectedMood,
            completedHabitNamesForToday.toSet(),
            reminderHour,
            reminderMinute,
            true
        )
    }

    val finishWithReminderChoice = { enable: Boolean ->
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onComplete(
                selectedStarterHabits.toList(),
                selectedMood,
                completedHabitNamesForToday.toSet(),
                reminderHour,
                reminderMinute,
                enable
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Navigation Bar with Back button for steps 1..3 and progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentStep--
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..3).forEach { step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (step <= currentStep) primaryColor else surfaceVariant)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step Content Switcher
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "onboarding_step_transition"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onStart = { currentStep = 1 })
                1 -> PickHabitsStep(
                    suggestions = defaultSuggestions,
                    selectedHabits = selectedStarterHabits,
                    onToggleHabit = { habit ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedStarterHabits.any { it.name == habit.name }) {
                            selectedStarterHabits.removeAll { it.name == habit.name }
                        } else {
                            selectedStarterHabits.add(habit)
                        }
                    },
                    onOpenCustomDialog = { showCustomHabitDialog = true },
                    onContinue = { currentStep = 2 }
                )
                2 -> FirstCheckInStep(
                    selectedHabits = selectedStarterHabits,
                    selectedMood = selectedMood,
                    completedHabitNames = completedHabitNamesForToday,
                    onMoodSelected = { rating ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedMood = rating
                    },
                    onToggleHabitForToday = { name ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (completedHabitNamesForToday.contains(name)) {
                            completedHabitNamesForToday.remove(name)
                        } else {
                            completedHabitNamesForToday.add(name)
                        }
                    },
                    onContinue = { currentStep = 3 }
                )
                3 -> ReminderStep(
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    showDropdown = showTimeDropdown,
                    onToggleDropdown = { showTimeDropdown = !showTimeDropdown },
                    onSelectTime = { h, m ->
                        reminderHour = h
                        reminderMinute = m
                        showTimeDropdown = false
                    },
                    onRemindMe = { finishWithReminderChoice(true) },
                    onSkip = { finishWithReminderChoice(false) }
                )
            }
        }
    }

    // Custom Habit Add Dialog
    if (showCustomHabitDialog) {
        AlertDialog(
            onDismissRequest = { showCustomHabitDialog = false },
            containerColor = surfaceContainer,
            title = {
                Text("Add Custom Habit", color = onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                OutlinedTextField(
                    value = customHabitName,
                    onValueChange = { customHabitName = it },
                    placeholder = { Text("e.g. Meditate 10m", color = onSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onSurface,
                        unfocusedTextColor = onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customHabitName.trim()
                        if (trimmed.isNotEmpty()) {
                            val newHabit = Habit(
                                name = trimmed,
                                colorHex = "#FFB84D",
                                iconKey = "star",
                                sortOrder = defaultSuggestions.size
                            )
                            defaultSuggestions.add(newHabit)
                            selectedStarterHabits.add(newHabit)
                            customHabitName = ""
                            showCustomHabitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomHabitDialog = false }) {
                    Text("Cancel", color = onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = "Momentum",
                color = primaryColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Momentum tracks your habits and your mood side by side — so you can see which habits actually help, not just which ones you did.",
                color = onSurface,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = onPrimary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Get started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickHabitsStep(
    suggestions: List<Habit>,
    selectedHabits: List<Habit>,
    onToggleHabit: (Habit) -> Unit,
    onOpenCustomDialog: () -> Unit,
    onContinue: () -> Unit
) {
    val count = selectedHabits.size
    val isValid = count >= 1

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Pick habits to start.",
                color = onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select 1 or more habits to build momentum.",
                color = onSurfaceVariant,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestions.forEach { habit ->
                    val isSelected = selectedHabits.any { it.name == habit.name }
                    val parseColor = runCatching {
                        Color(android.graphics.Color.parseColor(habit.colorHex))
                    }.getOrDefault(primaryColor)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) surfaceVariant else surfaceContainer)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) parseColor else outlineColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleHabit(habit) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = IconHelper.getIcon(habit.iconKey),
                            contentDescription = null,
                            tint = parseColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = habit.name,
                            color = onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = parseColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Custom Habit Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceContainer)
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onOpenCustomDialog() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom habit",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selected: $count habit${if (count != 1) "s" else ""}",
                color = if (isValid) secondaryColor else onSurfaceVariant,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onContinue,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = surfaceVariant,
                disabledContentColor = onSurfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FirstCheckInStep(
    selectedHabits: List<Habit>,
    selectedMood: Int?,
    completedHabitNames: List<String>,
    onMoodSelected: (Int) -> Unit,
    onToggleHabitForToday: (String) -> Unit,
    onContinue: () -> Unit
) {
    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val emojis = listOf(
        1 to "😔",
        2 to "😐",
        3 to "🙂",
        4 to "😊",
        5 to "⚡"
    )

    val hasLogged = selectedMood != null || completedHabitNames.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Today, $todayFormatted",
                color = primaryColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Let's log today.",
                color = onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Optionally rate your mood and check off completed habits.",
                color = onSurfaceVariant,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mood Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceContainer)
                    .border(1.dp, outlineColor, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "How's your mood & energy today?",
                        color = onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        emojis.forEach { (rating, emoji) ->
                            val isSelected = selectedMood == rating
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "onboarding_mood_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .shadow(
                                        elevation = if (isSelected) 6.dp else 2.dp,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        color = if (isSelected) primaryColor else surfaceVariant,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = outlineColor,
                                        shape = CircleShape
                                    )
                                    .clickable { onMoodSelected(rating) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CHECK OFF TODAY'S HABITS",
                color = onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                selectedHabits.forEach { habit ->
                    val isChecked = completedHabitNames.contains(habit.name)
                    val parseColor = runCatching {
                        Color(android.graphics.Color.parseColor(habit.colorHex))
                    }.getOrDefault(primaryColor)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceContainer)
                            .border(
                                width = 1.dp,
                                color = if (isChecked) parseColor else outlineColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onToggleHabitForToday(habit.name) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(parseColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconHelper.getIcon(habit.iconKey),
                                    contentDescription = null,
                                    tint = parseColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = habit.name,
                                color = onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) parseColor else surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isChecked) parseColor else outlineColor,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (hasLogged) "Save first log" else "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReminderStep(
    reminderHour: Int,
    reminderMinute: Int,
    showDropdown: Boolean,
    onToggleDropdown: () -> Unit,
    onSelectTime: (Int, Int) -> Unit,
    onRemindMe: () -> Unit,
    onSkip: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val times = listOf(
        Pair(19, 0) to "7:00 PM",
        Pair(20, 0) to "8:00 PM",
        Pair(20, 30) to "8:30 PM",
        Pair(21, 0) to "9:00 PM",
        Pair(21, 30) to "9:30 PM",
        Pair(22, 0) to "10:00 PM"
    )

    val selectedLabel = times.find { it.first.first == reminderHour && it.first.second == reminderMinute }?.second
        ?: String.format("%d:%02d", if (reminderHour > 12) reminderHour - 12 else reminderHour, reminderMinute) + if (reminderHour >= 12) " PM" else " AM"

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Daily Reminder",
                color = primaryColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Want a nudge if you haven't logged by evening?",
                color = onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceContainer)
                    .border(1.dp, outlineColor, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Evening Check-in",
                        color = onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "A quick reminder before bed to log your day",
                        color = onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Time Picker Button with Dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(surfaceVariant)
                                .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onToggleDropdown() }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedLabel,
                                color = onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "▾",
                                color = primaryColor,
                                fontSize = 14.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = onToggleDropdown,
                            modifier = Modifier.background(surfaceContainer)
                        ) {
                            times.forEach { (time, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = onSurface, fontFamily = FontFamily.Monospace) },
                                    onClick = { onSelectTime(time.first, time.second) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onRemindMe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Remind me", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Skip for now", color = onSurfaceVariant, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
