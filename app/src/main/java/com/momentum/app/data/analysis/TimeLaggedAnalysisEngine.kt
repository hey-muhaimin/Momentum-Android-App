package com.momentum.app.data.analysis

import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.LagWindow
import com.momentum.app.data.model.MoodLog
import com.momentum.app.data.model.TimeBin
import com.momentum.app.data.model.TimeLaggedImpact
import java.time.LocalDate

object TimeLaggedAnalysisEngine {

    /**
     * Calculates time-lagged causal analysis for habits across T-0, T-1, T-2, and Time-of-Day bins.
     * Enforces strict honest gating (>= 5 samples per bucket required).
     */
    fun analyze(
        habits: List<Habit>,
        moodLogs: List<MoodLog>,
        habitLogs: List<HabitLog>,
        selectedLag: LagWindow = LagWindow.T1_PREVIOUS_DAY
    ): List<TimeLaggedImpact> {
        val moodMap = moodLogs.associateBy { it.date }
        val logsByHabit = habitLogs.groupBy { it.habitId }

        return habits.mapNotNull { habit ->
            val logs = logsByHabit[habit.id] ?: emptyList()
            val logMap = logs.associateBy { it.date }

            when (selectedLag) {
                LagWindow.T0_SAME_DAY -> computeLagImpact(habit, LagWindow.T0_SAME_DAY, 0, moodMap, logMap)
                LagWindow.T1_PREVIOUS_DAY -> computeLagImpact(habit, LagWindow.T1_PREVIOUS_DAY, 1, moodMap, logMap)
                LagWindow.T2_TWO_DAYS_PRIOR -> computeLagImpact(habit, LagWindow.T2_TWO_DAYS_PRIOR, 2, moodMap, logMap)
                LagWindow.TIME_OF_DAY -> computeTimeOfDayImpact(habit, moodMap, logMap)
            }
        }.sortedByDescending { it.correlationScore }
    }

    private fun computeLagImpact(
        habit: Habit,
        lagWindow: LagWindow,
        daysLag: Int,
        moodMap: Map<LocalDate, MoodLog>,
        logMap: Map<LocalDate, HabitLog>
    ): TimeLaggedImpact {
        val withScores = mutableListOf<Int>()
        val withoutScores = mutableListOf<Int>()

        moodMap.forEach { (date, moodLog) ->
            val habitDate = date.minusDays(daysLag.toLong())
            val habitLog = logMap[habitDate]

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

        val isEnoughData = withScores.size >= 5 && withoutScores.size >= 5

        val naturalText = when {
            !isEnoughData -> "Gathering data (${withScores.size}/5 days logged)"
            correlation > 0.3 -> "Doing '${habit.name}' ${lagWindow.description.lowercase()} raises your mood by +${String.format("%.1f", correlation)} pts."
            correlation < -0.3 -> "Skipping '${habit.name}' correlates with higher mood (+${String.format("%.1f", -correlation)} pts)."
            else -> "'${habit.name}' shows a stable mood pattern with minimal variance."
        }

        return TimeLaggedImpact(
            habit = habit,
            lagWindow = lagWindow,
            correlationScore = correlation,
            avgMoodWith = avgWith,
            avgMoodWithout = avgWithout,
            sampleCountWith = withScores.size,
            sampleCountWithout = withoutScores.size,
            isEnoughData = isEnoughData,
            naturalLanguageInsight = naturalText
        )
    }

    private fun computeTimeOfDayImpact(
        habit: Habit,
        moodMap: Map<LocalDate, MoodLog>,
        logMap: Map<LocalDate, HabitLog>
    ): TimeLaggedImpact {
        val morningWith = mutableListOf<Int>()
        val nonMorningWith = mutableListOf<Int>()

        moodMap.forEach { (date, moodLog) ->
            val habitLog = logMap[date]
            if (habitLog?.completed == true) {
                if (habitLog.timeBin == TimeBin.MORNING) {
                    morningWith.add(moodLog.score)
                } else {
                    nonMorningWith.add(moodLog.score)
                }
            }
        }

        val avgMorning = if (morningWith.isNotEmpty()) morningWith.average() else 0.0
        val avgOther = if (nonMorningWith.isNotEmpty()) nonMorningWith.average() else 0.0
        val correlation = if (morningWith.isNotEmpty() && nonMorningWith.isNotEmpty()) {
            avgMorning - avgOther
        } else {
            0.0
        }

        val isEnoughData = morningWith.size >= 5 && nonMorningWith.size >= 5

        val naturalText = when {
            !isEnoughData -> "Gathering time-of-day data (${morningWith.size}/5 morning logs)"
            correlation > 0.3 -> "Doing '${habit.name}' in the Morning yields +${String.format("%.1f", correlation)} pts higher energy than later in the day."
            else -> "'${habit.name}' yields consistent benefits regardless of time of day."
        }

        return TimeLaggedImpact(
            habit = habit,
            lagWindow = LagWindow.TIME_OF_DAY,
            targetTimeBin = TimeBin.MORNING,
            correlationScore = correlation,
            avgMoodWith = avgMorning,
            avgMoodWithout = avgOther,
            sampleCountWith = morningWith.size,
            sampleCountWithout = nonMorningWith.size,
            isEnoughData = isEnoughData,
            naturalLanguageInsight = naturalText
        )
    }
}
