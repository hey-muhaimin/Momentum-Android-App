package com.momentum.app.data.model

enum class LagWindow(val displayName: String, val daysLag: Int, val description: String) {
    T0_SAME_DAY("Same Day (T-0)", 0, "Impact on mood on the day habit is completed"),
    T1_PREVIOUS_DAY("Previous Day (T-1)", 1, "Impact on mood the day after habit completion"),
    T2_TWO_DAYS_PRIOR("2 Days Prior (T-2)", 2, "Longer term residual impact 2 days after completion"),
    TIME_OF_DAY("Time of Day", 0, "Impact based on whether habit is done in Morning, Afternoon, or Evening")
}

data class TimeLaggedImpact(
    val habit: Habit,
    val lagWindow: LagWindow,
    val targetTimeBin: TimeBin? = null,
    val correlationScore: Double, // Mood difference: avgWith - avgWithout
    val avgMoodWith: Double,
    val avgMoodWithout: Double,
    val sampleCountWith: Int,
    val sampleCountWithout: Int,
    val isEnoughData: Boolean, // True if sampleCountWith >= 5 and sampleCountWithout >= 5
    val naturalLanguageInsight: String
)
