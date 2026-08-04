package com.momentum.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey val date: LocalDate,
    val score: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
