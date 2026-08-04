package com.momentum.app.util

import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.HabitStreak
import java.time.LocalDate

/**
 * Helper object to calculate habit streaks from HabitLog data,
 * enforcing the 'one-grace-day-per-week' rule.
 */
object StreakCalculator {

    /**
     * Calculates the current and maximum streak for a list of habit logs.
     *
     * Rules:
     * - A streak counts consecutive days of completion up to today (or yesterday if today isn't logged yet).
     * - One grace day is permitted per 7-day rolling period ONLY IF it bridges prior completed days.
     * - A missed day cannot grant a grace day unless there are completed days before it in the streak.
     */
    fun calculateStreak(
        logs: List<HabitLog>,
        today: LocalDate = LocalDate.now()
    ): HabitStreak {
        // Map of completed status by date
        val completedMap = logs.filter { it.completed }.associateBy { it.date }
        if (completedMap.isEmpty()) {
            return HabitStreak(currentStreak = 0, maxStreak = 0)
        }

        val todayCompleted = completedMap.containsKey(today)
        val yesterdayCompleted = completedMap.containsKey(today.minusDays(1))

        // Determine starting date for backward streak walk
        val startDate: LocalDate? = when {
            todayCompleted -> today
            yesterdayCompleted -> today.minusDays(1)
            else -> {
                // If yesterday was missed, check if yesterday was a grace day bridging to a streak ending 2 days ago
                val twoDaysAgoCompleted = completedMap.containsKey(today.minusDays(2))
                if (twoDaysAgoCompleted && hasCompletedDayBefore(today.minusDays(2), completedMap)) {
                    today.minusDays(1)
                } else if (twoDaysAgoCompleted) {
                    today.minusDays(2)
                } else {
                    null
                }
            }
        }

        var currentStreak = 0
        if (startDate != null) {
            var checkDate: LocalDate = startDate
            var consecutiveMisses = 0
            var lastGraceIndex = -100

            for (dayIndex in 0..365) {
                val isDone = completedMap.containsKey(checkDate)
                if (isDone) {
                    currentStreak++
                    consecutiveMisses = 0
                } else {
                    consecutiveMisses++
                    val canUseGrace = consecutiveMisses < 2 &&
                            (dayIndex - lastGraceIndex) >= 7 &&
                            hasCompletedDayBefore(checkDate, completedMap)

                    if (canUseGrace) {
                        lastGraceIndex = dayIndex
                        currentStreak++
                    } else {
                        break
                    }
                }
                checkDate = checkDate.minusDays(1)
            }
        }

        // Calculate max streak chronologically
        var maxStreak = currentStreak
        val minDateNonNull = completedMap.keys.minOrNull()
        if (minDateNonNull != null) {
            var tempStreak = 0
            var tempMisses = 0
            var tempGraceEpoch = -100000

            var scanDate: LocalDate = minDateNonNull
            while (!scanDate.isAfter(today)) {
                val isDone = completedMap.containsKey(scanDate)
                val epoch = scanDate.toEpochDay().toInt()

                if (isDone) {
                    tempStreak++
                    tempMisses = 0
                } else {
                    tempMisses++
                    val canUseGrace = tempMisses < 2 &&
                            (epoch - tempGraceEpoch) >= 7 &&
                            hasCompletedDayBefore(scanDate, completedMap)

                    if (canUseGrace) {
                        tempGraceEpoch = epoch
                        tempStreak++
                    } else {
                        tempStreak = 0
                    }
                }

                if (tempStreak > maxStreak) {
                    maxStreak = tempStreak
                }
                scanDate = scanDate.plusDays(1)
            }
        }

        return HabitStreak(
            currentStreak = currentStreak,
            maxStreak = maxStreak
        )
    }

    private fun hasCompletedDayBefore(date: LocalDate, completedMap: Map<LocalDate, HabitLog>): Boolean {
        return completedMap.keys.any { it.isBefore(date) }
    }

    /**
     * Calculates streaks for multiple habits grouped by habit ID.
     */
    fun calculateStreaksForHabits(
        habitsWithLogs: Map<Long, List<HabitLog>>,
        today: LocalDate = LocalDate.now()
    ): Map<Long, HabitStreak> {
        return habitsWithLogs.mapValues { (_, logs) ->
            calculateStreak(logs, today)
        }
    }
}
