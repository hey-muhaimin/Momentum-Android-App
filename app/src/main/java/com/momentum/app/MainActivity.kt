package com.momentum.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momentum.app.ui.screens.FocusScreen
import com.momentum.app.ui.screens.HabitsScreen
import com.momentum.app.ui.screens.InsightsScreen
import com.momentum.app.ui.screens.MainViewModel
import com.momentum.app.ui.screens.TodayScreen
import com.momentum.app.ui.theme.MomentumTheme
import com.momentum.app.worker.ReminderScheduler

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
            MomentumTheme {
                var currentTab by remember { mutableStateOf(0) }
                val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                val activeHabits by viewModel.activeHabits.collectAsStateWithLifecycle()
                val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
                val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
                val habitImpacts by viewModel.habitImpacts.collectAsStateWithLifecycle()
                val timeLaggedImpacts by viewModel.timeLaggedImpacts.collectAsStateWithLifecycle()
                val selectedLagWindow by viewModel.selectedLagWindow.collectAsStateWithLifecycle()
                val contextInsights by viewModel.contextInsights.collectAsStateWithLifecycle()
                val experiments by viewModel.experiments.collectAsStateWithLifecycle()
                val wearOSStatus by viewModel.wearOSStatus.collectAsStateWithLifecycle()
                val moodLogs by viewModel.moodLogs.collectAsStateWithLifecycle()
                val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
                val habitStreaks by viewModel.habitStreaks.collectAsStateWithLifecycle()
                val disruptionState by viewModel.disruptionState.collectAsStateWithLifecycle()
                val reminderTimesList by viewModel.reminderTimesList.collectAsStateWithLifecycle()
                val isReminderEnabled by viewModel.isReminderEnabled.collectAsStateWithLifecycle()

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbarMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                                label = { Text("Today") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Habits") },
                                label = { Text("Habits") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                                label = { Text("Insights") }
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
                                label = { Text("Focus") }
                            )
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)
                    when (currentTab) {
                        0 -> TodayScreen(
                            activeHabits = activeHabits,
                            todayMood = todayMood,
                            todayLogs = todayLogs,
                            reminderTimesList = reminderTimesList,
                            isReminderEnabled = isReminderEnabled,
                            onUpdateReminderSettings = { times, enabled ->
                                // handled in prefs if needed
                            },
                            onMoodSelected = { score ->
                                viewModel.logMood(score)
                            },
                            onSubmitDailyMetrics = { mood, energy ->
                                if (mood != null && energy != null) {
                                    viewModel.submitDailyMetrics(mood, energy)
                                }
                            },
                            onToggleHabit = { habitId ->
                                viewModel.toggleHabitToday(habitId)
                            },
                            onConfirmPassiveLog = { habitId ->
                                viewModel.confirmPassiveLog(habitId)
                            },
                            onEditHabit = { habit ->
                                viewModel.startEditingHabit(habit)
                            },
                            onAddHabit = {
                                viewModel.startAddingHabit()
                            },
                            calculateStreak = { habit ->
                                habitStreaks[habit.id]?.currentStreak ?: 0
                            },
                            disruptionState = disruptionState,
                            onDismissDisruption = {
                                viewModel.dismissDisruptionBanner()
                            },
                            modifier = modifier
                        )
                        1 -> HabitsScreen(
                            viewModel = viewModel,
                            modifier = modifier
                        )
                        2 -> InsightsScreen(
                            habitImpacts = habitImpacts,
                            timeLaggedImpacts = timeLaggedImpacts,
                            selectedLagWindow = selectedLagWindow,
                            onSelectLagWindow = { lag ->
                                viewModel.selectLagWindow(lag)
                            },
                            contextInsights = contextInsights,
                            experiments = experiments,
                            wearOSStatus = wearOSStatus,
                            onSwitchVariant = { expId, variant ->
                                viewModel.switchExperimentVariant(expId, variant)
                            },
                            moodLogs = moodLogs,
                            activeHabits = activeHabits,
                            recentLogs = recentLogs,
                            onEditHabit = { habit ->
                                viewModel.startEditingHabit(habit)
                            },
                            modifier = modifier
                        )
                        3 -> FocusScreen(
                            viewModel = viewModel,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}
