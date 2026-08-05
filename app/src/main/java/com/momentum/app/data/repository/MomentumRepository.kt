package com.momentum.app.data.repository

import com.momentum.app.data.local.HabitDao
import com.momentum.app.data.local.HabitLogDao
import com.momentum.app.data.local.MoodLogDao
import com.momentum.app.data.model.CorrelationRow
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitImpact
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.HabitStreak
import com.momentum.app.data.model.MoodLog
import com.momentum.app.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MomentumRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val moodLogDao: MoodLogDao
) {

    val activeHabits: Flow<List<Habit>> = habitDao.observeActiveHabits()
    val allHabits: Flow<List<Habit>> = habitDao.observeAllHabits()

    fun observeTodayMood(): Flow<MoodLog?> {
        return moodLogDao.observeMoodForDate(LocalDate.now())
    }

    fun observeMoodForDate(date: LocalDate): Flow<MoodLog?> {
        return moodLogDao.observeMoodForDate(date)
    }

    fun observeTodayHabitLogs(): Flow<List<HabitLog>> {
        return habitLogDao.observeLogsForDate(LocalDate.now())
    }

    fun observeLogsForDate(date: LocalDate): Flow<List<HabitLog>> {
        return habitLogDao.observeLogsForDate(date)
    }

    fun observeLogsForHabit(habitId: Long, days: Int = 35): Flow<List<HabitLog>> {
        val today = LocalDate.now()
        val from = today.minusDays((days - 1).toLong())
        return habitLogDao.observeLogsForHabit(habitId, from, today)
    }

    fun observeRecentLogs(days: Int = 365): Flow<List<HabitLog>> {
        val today = LocalDate.now()
        val from = today.minusDays((days - 1).toLong())
        return habitLogDao.observeAllLogsBetween(from, today)
    }

    fun observeMoodHistory(days: Int = 30): Flow<List<MoodLog>> {
        val today = LocalDate.now()
        val from = today.minusDays((days - 1).toLong())
        return moodLogDao.observeMoodLogsBetween(from, today)
    }

    suspend fun getHabitById(id: Long): Habit? {
        return habitDao.getHabitById(id)
    }

    suspend fun addHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun archiveHabit(habitId: Long) {
        habitDao.archiveHabit(habitId)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitLog(habitId: Long, date: LocalDate) {
        val logs = habitLogDao.getLogsForDateSync(date)
        val existing = logs.find { it.habitId == habitId }
        val now = System.currentTimeMillis()
        if (existing != null) {
            val newCompleted = !existing.completed
            habitLogDao.upsertHabitLog(
                existing.copy(
                    completed = newCompleted,
                    completedAt = if (newCompleted) now else null,
                    exactTimestamp = now,
                    timeBin = com.momentum.app.data.model.TimeBin.fromTimestamp(now),
                    isConfirmed = true
                )
            )
        } else {
            habitLogDao.upsertHabitLog(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    completed = true,
                    completedAt = now,
                    exactTimestamp = now,
                    timeBin = com.momentum.app.data.model.TimeBin.fromTimestamp(now),
                    isConfirmed = true
                )
            )
        }
    }

    suspend fun confirmPassiveLog(habitId: Long, date: LocalDate) {
        val logs = habitLogDao.getLogsForDateSync(date)
        val existing = logs.find { it.habitId == habitId }
        if (existing != null) {
            habitLogDao.upsertHabitLog(
                existing.copy(
                    isConfirmed = true
                )
            )
        }
    }

    /**
     * Observes time-lagged impacts (T-0, T-1, T-2, Time of Day) using TimeLaggedAnalysisEngine
     */
    fun observeTimeLaggedImpacts(
        days: Int = 90,
        selectedLag: com.momentum.app.data.model.LagWindow = com.momentum.app.data.model.LagWindow.T1_PREVIOUS_DAY
    ): Flow<List<com.momentum.app.data.model.TimeLaggedImpact>> {
        val today = LocalDate.now()
        val from = today.minusDays((days - 1).toLong())

        return combine(
            habitDao.observeActiveHabits(),
            moodLogDao.observeMoodLogsBetween(from, today),
            habitLogDao.observeAllLogsBetween(from, today)
        ) { habits, moodLogs, habitLogs ->
            com.momentum.app.data.analysis.TimeLaggedAnalysisEngine.analyze(
                habits = habits,
                moodLogs = moodLogs,
                habitLogs = habitLogs,
                selectedLag = selectedLag
            )
        }
    }

    suspend fun logMood(date: LocalDate, score: Int, energyScore: Int = 5) {
        moodLogDao.upsertMoodLog(
            MoodLog(
                date = date,
                score = score.coerceIn(1, 10),
                energyScore = energyScore.coerceIn(1, 10),
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun logDailyMetrics(date: LocalDate, mood: Int, energy: Int) {
        moodLogDao.upsertMoodLog(
            MoodLog(
                date = date,
                score = mood.coerceIn(1, 10),
                energyScore = energy.coerceIn(1, 10),
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Calculates streak count for a habit applying the 1-grace-day-per-week rule.
     */
    fun calculateStreak(logs: List<HabitLog>, today: LocalDate = LocalDate.now()): HabitStreak {
        return StreakCalculator.calculateStreak(logs, today)
    }

    /**
     * Computes correlation score and impact statistics for all active habits
     */
    fun observeHabitImpacts(days: Int = 30): Flow<List<HabitImpact>> {
        val today = LocalDate.now()
        val from = today.minusDays((days - 1).toLong())

        return combine(
            habitDao.observeActiveHabits(),
            moodLogDao.observeMoodLogsBetween(from, today),
            habitLogDao.observeAllLogsBetween(from, today)
        ) { habits, moodLogs, habitLogs ->
            val moodMap = moodLogs.associateBy { it.date }
            val logsByHabit = habitLogs.groupBy { it.habitId }

            habits.map { habit ->
                val logs = logsByHabit[habit.id] ?: emptyList()
                val logMap = logs.associateBy { it.date }

                val withScores = mutableListOf<Int>()
                val withoutScores = mutableListOf<Int>()

                moodMap.forEach { (date, moodLog) ->
                    val habitLog = logMap[date]
                    if (habitLog?.completed == true) {
                        withScores.add(moodLog.score)
                    } else {
                        withoutScores.add(moodLog.score)
                    }
                }

                val avgWith = if (withScores.isNotEmpty()) withScores.average() else 0.0
                val avgWithout = if (withoutScores.isNotEmpty()) withoutScores.average() else 0.0
                val correlation = if (withScores.isNotEmpty() && withoutScores.isNotEmpty()) {
                    avgWith - avgWithout
                } else {
                    0.0
                }

                HabitImpact(
                    habit = habit,
                    correlationScore = correlation,
                    avgMoodWith = avgWith,
                    avgMoodWithout = avgWithout,
                    totalLoggedDays = withScores.size + withoutScores.size,
                    daysWithBoth = withScores.size
                )
            }.sortedByDescending { it.correlationScore }
        }
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHabits()
        habitLogDao.deleteAllLogs()
        moodLogDao.deleteAllMoodLogs()
    }

    suspend fun seedInitialDataIfEmpty() {
        // No-op: Onboarding controls habit initialization based on user choice.
    }
}
