package com.momentum.app.ui.screens

import android.app.Application
import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.momentum.app.data.local.AppDatabase
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitImpact
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.HabitStreak
import com.momentum.app.data.model.LagWindow
import com.momentum.app.data.model.MoodLog
import com.momentum.app.data.model.SourceType
import com.momentum.app.data.model.TimeLaggedImpact
import com.momentum.app.data.preferences.UserPreferencesRepository
import com.momentum.app.data.repository.MomentumRepository
import com.momentum.app.util.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class TimerMode(val label: String) {
    FOCUS("FOCUS SPRINT"),
    BREAK("SHORT BREAK")
}

data class TimerPreset(
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int
)

data class BrainNote(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MomentumRepository(db.habitDao(), db.habitLogDao(), db.moodLogDao())
    private val userPrefs = UserPreferencesRepository(application)

    val hasOnboarded: StateFlow<Boolean> = userPrefs.hasOnboarded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<String> = userPrefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPrefs.setThemeMode(mode)
        }
    }

    val activeHabits: StateFlow<List<Habit>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<HabitLog>> = repository.observeTodayHabitLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood: StateFlow<MoodLog?> = repository.observeTodayMood()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val moodLogs: StateFlow<List<MoodLog>> = repository.observeMoodHistory(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<HabitLog>> = repository.observeRecentLogs(365)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitImpacts: StateFlow<List<HabitImpact>> = repository.observeHabitImpacts(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedLagWindow = MutableStateFlow(LagWindow.T1_PREVIOUS_DAY)
    val selectedLagWindow: StateFlow<LagWindow> = _selectedLagWindow.asStateFlow()

    fun selectLagWindow(lag: LagWindow) {
        _selectedLagWindow.value = lag
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val timeLaggedImpacts: StateFlow<List<TimeLaggedImpact>> = _selectedLagWindow.flatMapLatest { lag ->
        repository.observeTimeLaggedImpacts(90, lag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitStreaks: StateFlow<Map<Long, HabitStreak>> = combine(activeHabits, recentLogs) { habits, logs ->
        val logsByHabit = logs.groupBy { it.habitId }
        habits.associate { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            habit.id to StreakCalculator.calculateStreak(habitLogs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val contextInsights: StateFlow<List<com.momentum.app.data.analysis.ContextInsight>> = MutableStateFlow(
        com.momentum.app.data.analysis.ContextEngine.generateContextInsights()
    ).asStateFlow()

    private val _isDisruptionDismissed = MutableStateFlow(false)
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

    fun switchExperimentVariant(experimentId: String, newVariant: String) {
        _experiments.value = _experiments.value.map { exp ->
            if (exp.id == experimentId) exp.copy(activeVariant = newVariant) else exp
        }
    }

    val wearOSStatus: StateFlow<com.momentum.app.data.wear.WearOSStatus> = MutableStateFlow(
        com.momentum.app.data.wear.WearOSManager.getWearStatus(application)
    ).asStateFlow()

    val reminderTime: StateFlow<Pair<Int, Int>> = userPrefs.reminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(20, 0))

    val reminderTimesList: StateFlow<List<Pair<Int, Int>>> = userPrefs.reminderTimesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Pair(20, 0)))

    val isReminderEnabled: StateFlow<Boolean> = userPrefs.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateReminderSettings(times: List<Pair<Int, Int>>, enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setReminderTimes(times, enabled)
        }
    }

    // Timer state
    val presets = listOf(
        TimerPreset("25m Focus", 25, 5),
        TimerPreset("50m Deep", 50, 10),
        TimerPreset("15m Break", 15, 3)
    )

    private val _selectedPreset = MutableStateFlow(presets[0])
    val selectedPreset: StateFlow<TimerPreset> = _selectedPreset.asStateFlow()

    private val _timerMode = MutableStateFlow(TimerMode.FOCUS)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(presets[0].focusMinutes * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _totalFocusMinutesCompletedToday = MutableStateFlow(80) // 1h 20m as per mockup
    val totalFocusMinutesCompletedToday: StateFlow<Int> = _totalFocusMinutesCompletedToday.asStateFlow()

    // Brain dump notes
    private val _brainNotes = MutableStateFlow(
        listOf(
            BrainNote(text = "Reply to Sam about the deck", isDone = true),
            BrainNote(text = "Check train times for Friday", isDone = false),
            BrainNote(text = "Idea: move stand-up to 10:30", isDone = false)
        )
    )
    val brainNotes: StateFlow<List<BrainNote>> = _brainNotes.asStateFlow()

    // Settings preferences
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedChainHabitId = MutableStateFlow<Long?>(null)
    val selectedChainHabitId: StateFlow<Long?> = _selectedChainHabitId.asStateFlow()

    fun selectChainHabit(habitId: Long?) {
        _selectedChainHabitId.value = habitId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedChainLogs: StateFlow<List<HabitLog>> = _selectedChainHabitId.flatMapLatest { habitId ->
        if (habitId != null) {
            repository.observeLogsForHabit(habitId, 35)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val habits = repository.activeHabits.firstOrNull() ?: emptyList()
            if (habits.isEmpty()) {
                repository.addHabit(Habit(name = "Sleep 7+ hrs", iconKey = "sleep", colorHex = "#1D352F", sourceType = SourceType.HEALTH_CONNECT_SLEEP))
                repository.addHabit(Habit(name = "Move 20 min", iconKey = "move", colorHex = "#3A2F1B", sourceType = SourceType.MANUAL))
                repository.addHabit(Habit(name = "Drink water", iconKey = "water", colorHex = "#1B2B3A", sourceType = SourceType.MANUAL))
                repository.addHabit(Habit(name = "No phone in bed", iconKey = "phone", colorHex = "#3A221F", sourceType = SourceType.MANUAL))
                repository.addHabit(Habit(name = "Read 10 pages", iconKey = "book", colorHex = "#303B16", sourceType = SourceType.MANUAL))
            }
        }
    }

    fun completeOnboarding(
        starterHabits: List<String>,
        initialMood: Int?,
        completedHabitNames: Set<String> = emptySet(),
        enableReminder: Boolean = true,
        reminderHour: Int = 20,
        reminderMinute: Int = 0
    ) {
        viewModelScope.launch {
            starterHabits.forEach { name ->
                val (icon, color, source) = when {
                    name.contains("Sleep", ignoreCase = true) -> Triple("sleep", "#1D352F", SourceType.HEALTH_CONNECT_SLEEP)
                    name.contains("Move", ignoreCase = true) -> Triple("move", "#3A2F1B", SourceType.MANUAL)
                    name.contains("Water", ignoreCase = true) -> Triple("water", "#1B2B3A", SourceType.MANUAL)
                    name.contains("Phone", ignoreCase = true) -> Triple("phone", "#3A221F", SourceType.MANUAL)
                    name.contains("Read", ignoreCase = true) -> Triple("book", "#263318", SourceType.MANUAL)
                    name.contains("Journal", ignoreCase = true) -> Triple("edit", "#1D352F", SourceType.MANUAL)
                    else -> Triple("star", "#303B16", SourceType.MANUAL)
                }
                val id = repository.addHabit(Habit(name = name, iconKey = icon, colorHex = color, sourceType = source))
                if (completedHabitNames.contains(name)) {
                    repository.toggleHabitLog(id, LocalDate.now())
                }
            }
            if (initialMood != null) {
                repository.logMood(LocalDate.now(), initialMood, 8)
            }
            if (enableReminder) {
                userPrefs.setReminderTime(reminderHour, reminderMinute, true)
            }
            userPrefs.setOnboarded(true)
        }
    }

    fun selectPreset(preset: TimerPreset) {
        pauseTimer()
        _selectedPreset.value = preset
        _timerMode.value = if (preset.label.contains("Break", ignoreCase = true)) TimerMode.BREAK else TimerMode.FOCUS
        _remainingSeconds.value = if (_timerMode.value == TimerMode.FOCUS) preset.focusMinutes * 60 else preset.breakMinutes * 60
    }

    fun startPauseTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_remainingSeconds.value <= 0) return
        _isRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value && _remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0) {
                _isRunning.value = false
                triggerCompletionAlert()
                handleTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        val preset = _selectedPreset.value
        _remainingSeconds.value = if (_timerMode.value == TimerMode.FOCUS) {
            preset.focusMinutes * 60
        } else {
            preset.breakMinutes * 60
        }
    }

    fun skipMode() {
        pauseTimer()
        val preset = _selectedPreset.value
        if (_timerMode.value == TimerMode.FOCUS) {
            _timerMode.value = TimerMode.BREAK
            _remainingSeconds.value = preset.breakMinutes * 60
            _snackbarMessage.value = "Switched to Break"
        } else {
            _timerMode.value = TimerMode.FOCUS
            _remainingSeconds.value = preset.focusMinutes * 60
            _snackbarMessage.value = "Switched to Focus Sprint"
        }
    }

    private fun handleTimerFinished() {
        val preset = _selectedPreset.value
        if (_timerMode.value == TimerMode.FOCUS) {
            _totalFocusMinutesCompletedToday.value += preset.focusMinutes
            _snackbarMessage.value = "Focus sprint completed! Great job."
            _timerMode.value = TimerMode.BREAK
            _remainingSeconds.value = preset.breakMinutes * 60
        } else {
            _snackbarMessage.value = "Break finished. Ready for the next sprint?"
            _timerMode.value = TimerMode.FOCUS
            _remainingSeconds.value = preset.focusMinutes * 60
        }
    }

    private fun triggerCompletionAlert() {
        val context = getApplication<Application>()
        if (_soundEnabled.value) {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(context, notification)
                r.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (_vibrationEnabled.value) {
            try {
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addBrainNote(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            val note = BrainNote(text = trimmed)
            _brainNotes.value = listOf(note) + _brainNotes.value
        }
    }

    fun toggleBrainNote(id: String) {
        _brainNotes.value = _brainNotes.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
    }

    fun deleteBrainNote(id: String) {
        _brainNotes.value = _brainNotes.value.filterNot { it.id == id }
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, LocalDate.now())
        }
    }

    fun confirmPassiveLog(habitId: Long) {
        viewModelScope.launch {
            repository.confirmPassiveLog(habitId, LocalDate.now())
            _snackbarMessage.value = "Activity confirmed"
        }
    }

    fun logMood(score: Int) {
        viewModelScope.launch {
            repository.logMood(LocalDate.now(), score, 8)
            _snackbarMessage.value = "Mood logged"
        }
    }

    fun submitDailyMetrics(mood: Float, energy: Float) {
        viewModelScope.launch {
            repository.logDailyMetrics(LocalDate.now(), mood.toInt(), energy.toInt())
            _snackbarMessage.value = "Check-in saved"
        }
    }

    fun addHabit(title: String) {
        viewModelScope.launch {
            val trimmed = title.trim()
            if (trimmed.isNotEmpty()) {
                repository.addHabit(Habit(name = trimmed, sourceType = SourceType.MANUAL))
                _snackbarMessage.value = "Habit added"
            }
        }
    }

    fun addCustomHabit(
        name: String,
        iconKey: String,
        colorHex: String,
        sourceType: SourceType,
        reminderHour: Int?,
        reminderMinute: Int?
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                sourceType = sourceType,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
            repository.addHabit(habit)
            _snackbarMessage.value = "Habit created"
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId)
            _snackbarMessage.value = "Habit archived"
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _snackbarMessage.value = "Habit removed"
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}
