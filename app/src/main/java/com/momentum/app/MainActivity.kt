package com.momentum.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.momentum.app.worker.ReminderScheduler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momentum.app.ui.components.HabitDetailSheet
import com.momentum.app.ui.screens.ChainScreen
import com.momentum.app.ui.screens.InsightsScreen
import com.momentum.app.ui.screens.MainViewModel
import com.momentum.app.ui.screens.OnboardingScreen
import com.momentum.app.ui.screens.TodayScreen
import com.momentum.app.ui.theme.Amber
import com.momentum.app.ui.theme.BgDark
import com.momentum.app.ui.theme.GreenLight
import com.momentum.app.ui.theme.GreenPrimary
import com.momentum.app.ui.theme.MomentumTheme
import com.momentum.app.ui.theme.NavBackground
import com.momentum.app.ui.theme.SageAccent
import com.momentum.app.ui.theme.SurfaceDark
import com.momentum.app.ui.theme.SurfaceHairline
import com.momentum.app.ui.theme.TextMuted
import com.momentum.app.ui.theme.TextPrimary

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

        val initialTab = intent?.getStringExtra("navigate_to") ?: "today"

        setContent {
            MomentumTheme {
                var activeTab by remember { mutableStateOf(initialTab) }

                val hasOnboarded by viewModel.hasOnboarded.collectAsStateWithLifecycle()
                val timeLaggedImpacts by viewModel.timeLaggedImpacts.collectAsStateWithLifecycle()
                val selectedLagWindow by viewModel.selectedLagWindow.collectAsStateWithLifecycle()
                val activeHabits by viewModel.activeHabits.collectAsStateWithLifecycle()
                val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
                val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
                val moodLogs by viewModel.moodLogs.collectAsStateWithLifecycle()
                val habitImpacts by viewModel.habitImpacts.collectAsStateWithLifecycle()
                val habitStreaks by viewModel.habitStreaks.collectAsStateWithLifecycle()
                val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
                val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()
                val selectedChainHabitId by viewModel.selectedChainHabitId.collectAsStateWithLifecycle()
                val selectedChainLogs by viewModel.selectedChainLogs.collectAsStateWithLifecycle()
                val reminderTimesList by viewModel.reminderTimesList.collectAsStateWithLifecycle()
                val isReminderEnabled by viewModel.isReminderEnabled.collectAsStateWithLifecycle()

                if (!hasOnboarded) {
                    OnboardingScreen(
                        onComplete = { starterHabits, initialMoodScore, completedHabitNames, reminderHour, reminderMinute, enableReminder ->
                            viewModel.completeOnboarding(
                                starterHabits,
                                initialMoodScore,
                                completedHabitNames,
                                reminderHour,
                                reminderMinute,
                                enableReminder
                            )
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = activeTab == "today",
                                    onClick = { activeTab = "today" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                                    label = {
                                        Text(
                                            "TODAY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                NavigationBarItem(
                                    selected = activeTab == "insights",
                                    onClick = { activeTab = "insights" },
                                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Insights") },
                                    label = {
                                        Text(
                                            "INSIGHTS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                NavigationBarItem(
                                    selected = activeTab == "chain",
                                    onClick = { activeTab = "chain" },
                                    icon = { Icon(Icons.Default.Link, contentDescription = "Chain") },
                                    label = {
                                        Text(
                                            "CHAIN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
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
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (activeTab) {
                                "today" -> TodayScreen(
                                    activeHabits = activeHabits,
                                    todayMood = todayMood,
                                    todayLogs = todayLogs,
                                    reminderTimesList = reminderTimesList,
                                    isReminderEnabled = isReminderEnabled,
                                    onUpdateReminderSettings = { times, enabled ->
                                        viewModel.updateReminderTimesSettings(times, enabled)
                                    },
                                    onMoodSelected = { rating -> viewModel.logMood(rating) },
                                    onToggleHabit = { habitId -> viewModel.toggleTodayHabit(habitId) },
                                    onConfirmPassiveLog = { habitId -> viewModel.confirmPassiveHabitLog(habitId) },
                                    onEditHabit = { habit -> viewModel.openHabitDetail(habit) },
                                    onAddHabit = { viewModel.openNewHabitSheet() },
                                    calculateStreak = { habit ->
                                        habitStreaks[habit.id]?.currentStreak ?: 0
                                    }
                                )


                                "insights" -> InsightsScreen(
                                    habitImpacts = habitImpacts,
                                    timeLaggedImpacts = timeLaggedImpacts,
                                    selectedLagWindow = selectedLagWindow,
                                    onSelectLagWindow = { window -> viewModel.selectLagWindow(window) },
                                    moodLogs = moodLogs,
                                    activeHabits = activeHabits,
                                    recentLogs = recentLogs,
                                    onEditHabit = { habit -> viewModel.openHabitDetail(habit) }
                                )

                                "chain" -> ChainScreen(
                                    activeHabits = activeHabits,
                                    selectedHabitId = selectedChainHabitId,
                                    selectedLogs = selectedChainLogs,
                                    onSelectHabit = { habitId -> viewModel.selectChainHabit(habitId) },
                                    calculateStreak = { habit ->
                                        habitStreaks[habit.id]?.currentStreak ?: 0
                                    }
                                )
                            }
                        }
                    }

                    // Habit Detail Bottom Sheet modal
                    if (editingHabit != null) {
                        HabitDetailSheet(
                            habit = editingHabit,
                            onDismiss = { viewModel.dismissHabitDetail() },
                            onSave = { updatedHabit -> viewModel.saveHabit(updatedHabit) },
                            onArchive = { habitId -> viewModel.archiveHabit(habitId) },
                            onDelete = { habit -> viewModel.deleteHabit(habit) }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
