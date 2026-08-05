package com.momentum.app.data.analysis

import com.momentum.app.data.model.HabitLog
import java.time.LocalDate

data class DisruptionState(
    val isDisruptionDetected: Boolean,
    val reasonMessage: String?,
    val isAutoPaused: Boolean
)

object DisruptionEngine {
    /**
     * Detects disruption signals (e.g., sudden drop in active logs or travel anomalies) over recent days.
     */
    fun evaluateDisruption(
        recentLogs: List<HabitLog>,
        today: LocalDate = LocalDate.now()
    ): DisruptionState {
        // Look at the past 3 days before today
        val last3Days = (1..3).map { today.minusDays(it.toLong()) }
        val logsInWindow = recentLogs.filter { it.date in last3Days }
        val completedCount = logsInWindow.count { it.completed }

        // If active logging drops significantly (e.g. 0 logs completed across 3 days when user usually logs)
        if (logsInWindow.size >= 3 && completedCount == 0) {
            return DisruptionState(
                isDisruptionDetected = true,
                reasonMessage = "Routine shift detected over the past 3 days (possible travel or rest). Streaks are gracefully paused.",
                isAutoPaused = true
            )
        }

        return DisruptionState(
            isDisruptionDetected = false,
            reasonMessage = null,
            isAutoPaused = false
        )
    }
}
