package com.momentum.app.data.model

data class HabitImpact(
    val habit: Habit,
    val correlationScore: Double,
    val avgMoodWith: Double,
    val avgMoodWithout: Double,
    val totalLoggedDays: Int,
    val daysWithBoth: Int = 0
)
