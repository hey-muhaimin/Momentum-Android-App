package com.momentum.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class StarterHabitItem(
    val name: String,
    val iconSymbol: String,
    val iconKey: String,
    val colorHex: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        starterHabitNames: List<String>,
        initialMoodScore: Int?,
        completedHabitNames: Set<String>,
        reminderHour: Int,
        reminderMinute: Int,
        enableReminder: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    BackHandler(enabled = currentStep > 0) {
        currentStep--
    }

    val defaultHabits = remember {
        mutableStateListOf(
            StarterHabitItem("Sleep 7+ hrs", "☾", "sleep", "#1D352F"),
            StarterHabitItem("Move 20 min", "↗", "move", "#3A2F1B"),
            StarterHabitItem("Read", "▤", "book", "#263318"),
            StarterHabitItem("No phone in bed", "▣", "phone", "#3A221F"),
            StarterHabitItem("Drink water", "◈", "water", "#1B2B3A"),
            StarterHabitItem("Journal", "✎", "journal", "#1D352F")
        )
    }

    // Default pre-selected habits
    val selectedHabitNames = remember {
        mutableStateListOf("Sleep 7+ hrs", "Move 20 min")
    }

    // Step 3 check-in state
    var selectedMoodScore by remember { mutableStateOf<Int?>(4) } // Default 😊
    val completedHabitNames = remember {
        mutableStateListOf("Sleep 7+ hrs")
    }

    // Step 4 reminder state
    var reminderHour by remember { mutableIntStateOf(20) } // 8:00 PM
    var reminderMinute by remember { mutableIntStateOf(0) }
    var showTimeDropdown by remember { mutableStateOf(false) }

    // Dialog for adding custom habit
    var showCustomHabitDialog by remember { mutableStateOf(false) }
    var customHabitInput by remember { mutableStateOf("") }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onComplete(
            selectedHabitNames.toList(),
            selectedMoodScore,
            completedHabitNames.toSet(),
            reminderHour,
            reminderMinute,
            true
        )
    }

    val finishOnboarding = { enableReminder: Boolean ->
        if (enableReminder && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onComplete(
                selectedHabitNames.toList(),
                selectedMoodScore,
                completedHabitNames.toSet(),
                reminderHour,
                reminderMinute,
                enableReminder
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        // Step indicator bars and back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                IconButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (0..3).forEach { stepIdx ->
                    val isActive = stepIdx <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        // Animated step content
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "onboarding_step_content"
        ) { step ->
            when (step) {
                0 -> StepWelcome(onStart = { currentStep = 1 })
                1 -> StepPickHabits(
                    habitItems = defaultHabits,
                    selectedNames = selectedHabitNames,
                    onToggle = { name ->
                        if (selectedHabitNames.contains(name)) {
                            selectedHabitNames.remove(name)
                        } else {
                            selectedHabitNames.add(name)
                        }
                    },
                    onOpenCustomDialog = { showCustomHabitDialog = true },
                    onContinue = { currentStep = 2 }
                )
                2 -> StepFirstCheckIn(
                    selectedHabitNames = selectedHabitNames,
                    selectedMood = selectedMoodScore,
                    completedNames = completedHabitNames,
                    onSelectMood = { selectedMoodScore = it },
                    onToggleCompleted = { name ->
                        if (completedHabitNames.contains(name)) {
                            completedHabitNames.remove(name)
                        } else {
                            completedHabitNames.add(name)
                        }
                    },
                    onContinue = { currentStep = 3 }
                )
                3 -> StepDailyReminder(
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    showDropdown = showTimeDropdown,
                    onToggleDropdown = { showTimeDropdown = !showTimeDropdown },
                    onSelectTime = { h, m ->
                        reminderHour = h
                        reminderMinute = m
                        showTimeDropdown = false
                    },
                    onRemindMe = { finishOnboarding(true) },
                    onSkip = { finishOnboarding(false) }
                )
            }
        }
    }

    // Custom Habit Dialog
    if (showCustomHabitDialog) {
        AlertDialog(
            onDismissRequest = { showCustomHabitDialog = false },
            title = { Text("Custom Habit", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customHabitInput,
                    onValueChange = { customHabitInput = it },
                    placeholder = { Text("e.g. Meditate 10 min") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customHabitInput.trim()
                        if (trimmed.isNotEmpty()) {
                            val newItem = StarterHabitItem(trimmed, "✦", "star", "#303B16")
                            defaultHabits.add(newItem)
                            selectedHabitNames.add(trimmed)
                            customHabitInput = ""
                            showCustomHabitDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomHabitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StepWelcome(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Leaf icon
            val leafColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.size(62.dp)) {
                val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.16f, size.height * 0.84f)
                    cubicTo(
                        size.width * 0.16f, size.height * 0.46f,
                        size.width * 0.41f, size.height * 0.21f,
                        size.width * 0.84f, size.height * 0.17f
                    )
                    cubicTo(
                        size.width * 0.84f, size.height * 0.59f,
                        size.width * 0.59f, size.height * 0.84f,
                        size.width * 0.16f, size.height * 0.84f
                    )
                    close()
                }
                drawPath(path, color = leafColor, style = stroke)

                val veinPath = Path().apply {
                    moveTo(size.width * 0.16f, size.height * 0.84f)
                    cubicTo(
                        size.width * 0.33f, size.height * 0.63f,
                        size.width * 0.50f, size.height * 0.50f,
                        size.width * 0.75f, size.height * 0.42f
                    )
                }
                drawPath(veinPath, color = leafColor, style = stroke)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Momentum",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Understand yourself.\nMove forward.",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 40.sp,
                fontSize = 32.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Build routines and discover how they connect to your mood and energy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Your data stays on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Get started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepPickHabits(
    habitItems: List<StarterHabitItem>,
    selectedNames: List<String>,
    onToggle: (String) -> Unit,
    onOpenCustomDialog: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "What would you like\nto build?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Select 1 or more habits. You can change these later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    habitItems.forEach { item ->
                        val isSelected = selectedNames.contains(item.name)
                        Surface(
                            onClick = { onToggle(item.name) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.iconSymbol,
                                    fontSize = 15.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 13.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Custom habit chip
                    Surface(
                        onClick = onOpenCustomDialog,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Custom habit",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "SELECTED: ${selectedNames.size} HABITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Button(
            onClick = onContinue,
            enabled = selectedNames.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StepFirstCheckIn(
    selectedHabitNames: List<String>,
    selectedMood: Int?,
    completedNames: List<String>,
    onSelectMood: (Int) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onContinue: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()) }
    val todayDate = remember { LocalDate.now().format(dateFormatter) }

    val moodOptions = listOf(
        1 to "😔",
        2 to "😐",
        3 to "🙂",
        4 to "😊",
        5 to "⚡"
    )

    val moodDescription = when (selectedMood) {
        1 -> "Feeling low today. Take it easy."
        2 -> "Feeling okay. A neutral, quiet day."
        3 -> "Doing well. Calm and steady."
        4 -> "Feeling good! Energy is high today."
        5 -> "Supercharged! Ready to conquer."
        else -> "Rate how you are feeling right now."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Today, $todayDate".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Let's log today.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Optional — rate your mood and check off anything you've already done.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
            }

            // Mood Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "How's your mood & energy today?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            moodOptions.forEach { (score, emoji) ->
                                val isSelected = selectedMood == score
                                Surface(
                                    onClick = { onSelectMood(score) },
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 22.sp)
                                    }
                                }
                            }
                        }

                        Text(
                            text = moodDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "CHECK OFF TODAY'S HABITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            items(selectedHabitNames.size) { idx ->
                val name = selectedHabitNames[idx]
                val isCompleted = completedNames.contains(name)

                ElevatedCard(
                    onClick = { onToggleCompleted(name) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    name.contains("Sleep", ignoreCase = true) -> "☾"
                                    name.contains("Move", ignoreCase = true) -> "↗"
                                    name.contains("Water", ignoreCase = true) -> "◈"
                                    name.contains("Phone", ignoreCase = true) -> "▣"
                                    name.contains("Read", ignoreCase = true) -> "▤"
                                    else -> "✦"
                                },
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isCompleted) "Completed today" else "Not logged yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCompleted) {
                                    Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Save first log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepDailyReminder(
    reminderHour: Int,
    reminderMinute: Int,
    showDropdown: Boolean,
    onToggleDropdown: () -> Unit,
    onSelectTime: (Int, Int) -> Unit,
    onRemindMe: () -> Unit,
    onSkip: () -> Unit
) {
    val timeOptions = listOf(
        Pair(19, 0) to "7:00 PM",
        Pair(20, 0) to "8:00 PM",
        Pair(20, 30) to "8:30 PM",
        Pair(21, 0) to "9:00 PM",
        Pair(21, 30) to "9:30 PM",
        Pair(22, 0) to "10:00 PM"
    )

    val currentLabel = timeOptions.find { it.first.first == reminderHour && it.first.second == reminderMinute }?.second
        ?: String.format(Locale.getDefault(), "%d:%02d %s", if (reminderHour > 12) reminderHour - 12 else reminderHour, reminderMinute, if (reminderHour >= 12) "PM" else "AM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DAILY REMINDER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Want a nudge if you haven't logged by evening?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(28.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔔", fontSize = 26.sp)
                    }

                    Text(
                        text = "Evening Check-in",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    )

                    Text(
                        text = "A quick reminder before bed to log your day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Time chip with dropdown
                    Box {
                        Surface(
                            onClick = onToggleDropdown,
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("◷", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = currentLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("⌄", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = onToggleDropdown,
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            timeOptions.forEach { (timePair, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                                    },
                                    onClick = { onSelectTime(timePair.first, timePair.second) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "You can change or turn this off anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRemindMe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Remind me", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
