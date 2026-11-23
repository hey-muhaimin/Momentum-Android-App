package com.momentum.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momentum.app.data.model.Habit
import com.momentum.app.ui.components.AddHabitSheet
import com.momentum.app.ui.components.BrainDumpSheet
import com.momentum.app.ui.components.HabitDetailSheet
import com.momentum.app.ui.components.MoodEnergySheet
import com.momentum.app.ui.screens.ChainScreen
import com.momentum.app.ui.screens.FocusScreen
import com.momentum.app.ui.screens.HabitsScreen
import com.momentum.app.ui.screens.InsightsScreen
import com.momentum.app.ui.screens.MainViewModel
import com.momentum.app.ui.screens.OnboardingScreen
import com.momentum.app.ui.screens.SettingsScreen
import com.momentum.app.ui.screens.TodayScreen
import com.momentum.app.ui.theme.MomentumTheme
import com.momentum.app.worker.ReminderScheduler

sealed interface SubScreen {
    data object Settings : SubScreen
    data class Chain(val habitId: Long?) : SubScreen
}

sealed interface ActiveSheet {
    data object MoodEnergy : ActiveSheet
    data class HabitDetail(val habit: Habit) : ActiveSheet
    data object AddHabit : ActiveSheet
    data object BrainDump : ActiveSheet
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ReminderScheduler.scheduleDailyReminder(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode.lowercase()) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MomentumTheme(darkTheme = isDark) {
                val hasOnboarded by viewModel.hasOnboarded.collectAsStateWithLifecycle()

                val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                val activeHabits by viewModel.activeHabits.collectAsStateWithLifecycle()
                val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
                val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
                val habitImpacts by viewModel.habitImpacts.collectAsStateWithLifecycle()
                val timeLaggedImpacts by viewModel.timeLaggedImpacts.collectAsStateWithLifecycle()
                val selectedLagWindow by viewModel.selectedLagWindow.collectAsStateWithLifecycle()
                val moodLogs by viewModel.moodLogs.collectAsStateWithLifecycle()
                val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
                val habitStreaks by viewModel.habitStreaks.collectAsStateWithLifecycle()
                val brainNotes by viewModel.brainNotes.collectAsStateWithLifecycle()
                val selectedChainHabitId by viewModel.selectedChainHabitId.collectAsStateWithLifecycle()
                val selectedChainLogs by viewModel.selectedChainLogs.collectAsStateWithLifecycle()

                var currentTab by remember { mutableIntStateOf(0) }
                var currentSubScreen by remember { mutableStateOf<SubScreen?>(null) }
                var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbarMessage()
                    }
                }

                if (!hasOnboarded) {
                    OnboardingScreen(
                        onComplete = { starterHabits, mood, completedHabits, hour, min, enableReminder ->
                            viewModel.completeOnboarding(
                                starterHabits = starterHabits,
                                initialMood = mood,
                                completedHabitNames = completedHabits,
                                enableReminder = enableReminder,
                                reminderHour = hour,
                                reminderMinute = min
                            )
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (currentSubScreen == null) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == 0,
                                        onClick = { currentTab = 0 },
                                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                                        label = { Text("Today", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 1,
                                        onClick = { currentTab = 1 },
                                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Habits") },
                                        label = { Text("Habits", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 2,
                                        onClick = { currentTab = 2 },
                                        icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                                        label = { Text("Insights", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 3,
                                        onClick = { currentTab = 3 },
                                        icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
                                        label = { Text("Focus", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        val modifier = Modifier.padding(innerPadding)

                        AnimatedContent(
                            targetState = currentSubScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "subscreen_transition"
                        ) { subScreen ->
                            when (subScreen) {
                                is SubScreen.Settings -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onBack = { currentSubScreen = null },
                                        modifier = modifier
                                    )
                                }
                                is SubScreen.Chain -> {
                                    ChainScreen(
                                        habits = activeHabits,
                                        selectedHabitId = subScreen.habitId ?: selectedChainHabitId,
                                        logs = selectedChainLogs,
                                        onSelectHabit = { habitId -> viewModel.selectChainHabit(habitId) },
                                        onBack = { currentSubScreen = null },
                                        modifier = modifier
                                    )
                                }
                                null -> {
                                    when (currentTab) {
                                        0 -> TodayScreen(
                                            activeHabits = activeHabits,
                                            todayMood = todayMood,
                                            todayLogs = todayLogs,
                                            calculateStreak = { habit -> habitStreaks[habit.id]?.currentStreak ?: 0 },
                                            onToggleHabit = { habitId -> viewModel.toggleHabitToday(habitId) },
                                            onConfirmPassiveLog = { habitId -> viewModel.confirmPassiveLog(habitId) },
                                            onOpenMoodEnergySheet = { activeSheet = ActiveSheet.MoodEnergy },
                                            onOpenHabitDetail = { habit -> activeSheet = ActiveSheet.HabitDetail(habit) },
                                            onOpenAddHabitSheet = { activeSheet = ActiveSheet.AddHabit },
                                            onOpenSettings = { currentSubScreen = SubScreen.Settings },
                                            onNavigateToInsights = { currentTab = 2 },
                                            modifier = modifier
                                        )
                                        1 -> HabitsScreen(
                                            viewModel = viewModel,
                                            onOpenHabitDetail = { habit -> activeSheet = ActiveSheet.HabitDetail(habit) },
                                            onOpenAddHabitSheet = { activeSheet = ActiveSheet.AddHabit },
                                            modifier = modifier
                                        )
                                        2 -> InsightsScreen(
                                            habitImpacts = habitImpacts,
                                            timeLaggedImpacts = timeLaggedImpacts,
                                            selectedLagWindow = selectedLagWindow,
                                            onSelectLagWindow = { lag -> viewModel.selectLagWindow(lag) },
                                            moodLogs = moodLogs,
                                            activeHabits = activeHabits,
                                            recentLogs = recentLogs,
                                            modifier = modifier
                                        )
                                        3 -> FocusScreen(
                                            viewModel = viewModel,
                                            onOpenBrainDumpSheet = { activeSheet = ActiveSheet.BrainDump },
                                            modifier = modifier
                                        )
                                    }
                                }
                            }
                        }

                        // Modal Bottom Sheets
                        activeSheet?.let { sheet ->
                            ModalBottomSheet(
                                onDismissRequest = { activeSheet = null },
                                containerColor = MaterialTheme.colorScheme.surface,
                                dragHandle = null
                            ) {
                                when (sheet) {
                                    is ActiveSheet.MoodEnergy -> {
                                        MoodEnergySheet(
                                            initialMood = todayMood?.score ?: 7,
                                            initialEnergy = todayMood?.energyScore ?: 8,
                                            onSave = { m, e ->
                                                viewModel.submitDailyMetrics(m, e)
                                                activeSheet = null
                                            },
                                            onDismiss = { activeSheet = null }
                                        )
                                    }
                                    is ActiveSheet.HabitDetail -> {
                                        val streak = habitStreaks[sheet.habit.id]?.currentStreak ?: 4
                                        HabitDetailSheet(
                                            habit = sheet.habit,
                                            currentStreak = streak,
                                            consistencyPct = 78,
                                            daysDoneCount = 21,
                                            onOpenChainHistory = { habitId ->
                                                viewModel.selectChainHabit(habitId)
                                                activeSheet = null
                                                currentSubScreen = SubScreen.Chain(habitId)
                                            },
                                            onArchive = { habitId ->
                                                viewModel.archiveHabit(habitId)
                                                activeSheet = null
                                            },
                                            onDelete = { habit ->
                                                viewModel.deleteHabit(habit)
                                                activeSheet = null
                                            },
                                            onDismiss = { activeSheet = null }
                                        )
                                    }
                                    is ActiveSheet.AddHabit -> {
                                        AddHabitSheet(
                                            onSave = { name, icon, color, source, h, m ->
                                                viewModel.addCustomHabit(name, icon, color, source, h, m)
                                                activeSheet = null
                                            },
                                            onDismiss = { activeSheet = null }
                                        )
                                    }
                                    is ActiveSheet.BrainDump -> {
                                        BrainDumpSheet(
                                            brainNotes = brainNotes,
                                            onAddNote = { text -> viewModel.addBrainNote(text) },
                                            onToggleNote = { id -> viewModel.toggleBrainNote(id) },
                                            onDeleteNote = { id -> viewModel.deleteBrainNote(id) },
                                            onDismiss = { activeSheet = null }
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
}
