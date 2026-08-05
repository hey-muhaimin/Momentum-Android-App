package com.momentum.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import java.time.LocalDate

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitLog(
    val habitId: Long,
    val date: LocalDate,
    val completed: Boolean,
    val completedAt: Long? = null,
    val exactTimestamp: Long = completedAt ?: System.currentTimeMillis(),
    val timeBin: TimeBin = TimeBin.fromTimestamp(completedAt ?: System.currentTimeMillis()),
    val sourceType: SourceType = SourceType.MANUAL,
    val isConfirmed: Boolean = true,
    val detectedValue: Double? = null
)
