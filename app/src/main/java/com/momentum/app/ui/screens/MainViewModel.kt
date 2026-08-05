package com.momentum.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.momentum.app.data.local.AppDatabase
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitImpact
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.HabitStreak
import com.momentum.app.data.model.MoodLog
import com.momentum.app.data.preferences.UserPreferencesRepository
import com.momentum.app.data.repository.MomentumRepository
import com.momentum.app.util.StreakCalculator
import com.momentum.app.widget.MomentumWidgetProvider
import com.momentum.app.worker.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MomentumRepository(db.habitDao(), db.habitLogDao(), db.moodLogDao())
    private val userPrefs = UserPreferencesRepository(application)
    val healthConnectManager = com.momentum.app.data.health.HealthConnectManager(application)

    private val _selectedLagWindow = MutableStateFlow(com.momentum.app.data.model.LagWindow.T1_PREVIOUS_DAY)
    val selectedLagWindow: StateFlow<com.momentum.app.data.model.LagWindow> = _selectedLagWindow.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val timeLaggedImpacts: StateFlow<List<com.momentum.app.data.model.TimeLaggedImpact>> = _selectedLagWindow.flatMapLatest { lag ->
        repository.observeTimeLaggedImpacts(90, lag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeHabits: StateFlow<List<Habit>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood: StateFlow<MoodLog?> = repository.observeTodayMood()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayLogs: StateFlow<List<HabitLog>> = repository.observeTodayHabitLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<HabitLog>> = repository.observeRecentLogs(365)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitStreaks: StateFlow<Map<Long, HabitStreak>> = combine(activeHabits, recentLogs) { habits, logs ->
        val logsByHabit = logs.groupBy { it.habitId }
        habits.associate { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            habit.id to StreakCalculator.calculateStreak(habitLogs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val moodLogs: StateFlow<List<MoodLog>> = repository.observeMoodHistory(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitImpacts: StateFlow<List<HabitImpact>> = repository.observeHabitImpacts(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasOnboarded: StateFlow<Boolean> = userPrefs.hasOnboarded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true) // default true while loading

    val contextInsights: StateFlow<List<com.momentum.app.data.analysis.ContextInsight>> = MutableStateFlow(

        com.momentum.app.data.analysis.ContextEngine.generateContextInsights()
    ).asStateFlow()

    private val _isDisruptionDismissed = MutableStateFlow(false)
    val isDisruptionDismissed: StateFlow<Boolean> = _isDisruptionDismissed.asStateFlow()

    val disruptionState: StateFlow<com.momentum.app.data.analysis.DisruptionState> = combine(recentLogs, _isDisruptionDismissed) { logs, dismissed ->
        if (dismissed) {
            com.momentum.app.data.analysis.DisruptionState(false, null, false)
        } else {
            com.momentum.app.data.analysis.DisruptionEngine.evaluateDisruption(logs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.momentum.app.data.analysis.DisruptionState(false, null, false))

    fun dismissDisruptionBanner() {
        _isDisruptionDismissed.value = true
    }


    private val _experiments = MutableStateFlow(
        listOf(
            com.momentum.app.data.model.Experiment(
                id = "exp_1",
                title = "Morning Walk vs Evening Walk",
                hypothesis = "Moving before 9 AM produces higher afternoon mood lift than evening exercise.",
                variantA = "Morning Walk (8 AM)",
                variantB = "Evening Walk (6 PM)",
                durationDays = 14,
                startDate = LocalDate.now().minusDays(5),
                currentDay = 5,
                activeVariant = "Variant A",
                variantAMoodLiftPct = 28,
                variantBMoodLiftPct = 6
            )
        )
    )
    val experiments: StateFlow<List<com.momentum.app.data.model.Experiment>> = _experiments.asStateFlow()

    val wearOSStatus: StateFlow<com.momentum.app.data.wear.WearOSStatus> = MutableStateFlow(
        com.momentum.app.data.wear.WearOSManager.getWearStatus(application)
    ).asStateFlow()

    fun switchExperimentVariant(experimentId: String, newVariant: String) {
        _experiments.value = _experiments.value.map { exp ->
            if (exp.id == experimentId) exp.copy(activeVariant = newVariant) else exp
        }
    }

    val reminderTime: StateFlow<Pair<Int, Int>> = userPrefs.reminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(21, 0))

    val reminderTimesList: StateFlow<List<Pair<Int, Int>>> = userPrefs.reminderTimesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Pair(21, 0)))

    val isReminderEnabled: StateFlow<Boolean> = userPrefs.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    private val _selectedChainHabitId = MutableStateFlow<Long?>(null)
    val selectedChainHabitId: StateFlow<Long?> = _selectedChainHabitId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedChainLogs: StateFlow<List<HabitLog>> = _selectedChainHabitId.flatMapLatest { habitId ->
        if (habitId != null) {
            repository.observeLogsForHabit(habitId, 35)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        com.momentum.app.worker.HealthConnectSyncWorker.schedule(getApplication())
        ReminderScheduler.scheduleEveningMetricsReminder(getApplication())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        viewModelScope.launch {
            combine(userPrefs.reminderTimesList, userPrefs.isReminderEnabled) { times, enabled ->
                ReminderScheduler.scheduleDailyReminders(getApplication(), times, enabled)
            }.collect()
        }
        viewModelScope.launch {
            activeHabits.collect { habits ->
                ReminderScheduler.scheduleHabitReminders(getApplication(), habits)
                
                // Evaluate passive habits on load
                val today = LocalDate.now()
                val passiveHabits = habits.filter { it.sourceType != com.momentum.app.data.model.SourceType.MANUAL }
                if (passiveHabits.isNotEmpty()) {
                    val logs = repository.observeTodayHabitLogs().first()
                    passiveHabits.forEach { habit ->
                        val existing = logs.find { it.habitId == habit.id }
                        val evaluated = healthConnectManager.evaluatePassiveHabit(habit, today, existing)
                        if (evaluated != null && evaluated != existing) {
                            db.habitLogDao().upsertHabitLog(evaluated)
                        }
                    }
                }
            }
        }
    }

    fun updateReminderTimesSettings(times: List<Pair<Int, Int>>, enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setReminderTimes(times, enabled)
            ReminderScheduler.scheduleDailyReminders(getApplication(), times, enabled)
        }
    }

    fun updateReminderSettings(hour: Int, minute: Int, enabled: Boolean) {
        updateReminderTimesSettings(listOf(Pair(hour, minute)), enabled)
    }


    fun openHabitDetail(habit: Habit) {
        _editingHabit.value = habit
    }

    fun openNewHabitSheet() {
        _editingHabit.value = Habit(name = "", colorHex = "#7DD8C6", iconKey = "star")
    }

    fun dismissHabitDetail() {
        _editingHabit.value = null
    }

    fun selectChainHabit(habitId: Long) {
        _selectedChainHabitId.value = habitId
    }

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun logMood(rating: Int) {
        viewModelScope.launch {
            repository.logMood(LocalDate.now(), rating)
        }
    }

    fun submitDailyMetrics(mood: Float?, energy: Float?) {
        if (mood == null || energy == null || mood < 1f || energy < 1f || mood > 10f || energy > 10f) {
            _snackbarMessage.value = "Please select both mood and energy levels (1-10) before submitting."
            return
        }

        viewModelScope.launch {
            val moodInt = mood.toInt().coerceIn(1, 10)
            val energyInt = energy.toInt().coerceIn(1, 10)
            repository.logDailyMetrics(LocalDate.now(), moodInt, energyInt)
            _snackbarMessage.value = "Daily metrics logged! Mood: $moodInt/10, Energy: $energyInt/10."
        }
    }

    fun selectLagWindow(lag: com.momentum.app.data.model.LagWindow) {
        _selectedLagWindow.value = lag
    }

    fun confirmPassiveHabitLog(habitId: Long) {
        viewModelScope.launch {
            repository.confirmPassiveLog(habitId, LocalDate.now())
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleTodayHabit(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, LocalDate.now())
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            if (habit.id == 0L) {
                repository.addHabit(habit)
            } else {
                repository.updateHabit(habit)
            }
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId)
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun completeOnboarding(
        starterHabits: List<Habit>,
        initialMoodScore: Int?,
        completedHabitNames: Set<String>,
        reminderHour: Int,
        reminderMinute: Int,
        enableReminder: Boolean
    ) {
        viewModelScope.launch {
            repository.clearAllData()
            starterHabits.forEachIndexed { index, habit ->
                val habitId = repository.addHabit(
                    habit.copy(
                        id = 0L,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        sortOrder = index
                    )
                )
                if (completedHabitNames.contains(habit.name)) {
                    repository.toggleHabitLog(habitId, LocalDate.now())
                }
            }
            if (initialMoodScore != null && initialMoodScore in 1..5) {
                repository.logMood(LocalDate.now(), initialMoodScore)
            }
            userPrefs.setReminderTime(reminderHour, reminderMinute, enableReminder)
            userPrefs.setOnboarded(true)
            MomentumWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun getStreakForHabit(habitId: Long): HabitStreak {
        return habitStreaks.value[habitId] ?: HabitStreak(0, 0)
    }

    fun calculateHabitStreak(habitId: Long, logs: List<HabitLog>): HabitStreak {
        val habitLogs = logs.filter { it.habitId == habitId }
        return StreakCalculator.calculateStreak(habitLogs)
    }
}
