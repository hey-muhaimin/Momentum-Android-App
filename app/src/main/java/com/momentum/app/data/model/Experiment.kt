package com.momentum.app.data.model

import java.time.LocalDate

data class Experiment(
    val id: String,
    val title: String,
    val hypothesis: String,
    val variantA: String, // Routine A
    val variantB: String, // Routine B
    val durationDays: Int = 14,
    val startDate: LocalDate = LocalDate.now(),
    val currentDay: Int = 5,
    val activeVariant: String = "Variant A", // "Variant A" or "Variant B"
    val variantAMoodLiftPct: Int = 24,
    val variantBMoodLiftPct: Int = 8,
    val isCompleted: Boolean = false
)
