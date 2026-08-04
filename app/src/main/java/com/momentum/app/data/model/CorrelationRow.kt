package com.momentum.app.data.model

import java.time.LocalDate

data class CorrelationRow(
    val date: LocalDate,
    val score: Int?,
    val completed: Boolean?
)
