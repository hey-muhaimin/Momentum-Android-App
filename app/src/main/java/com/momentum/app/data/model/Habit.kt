package com.momentum.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#2F855A",
    val iconKey: String = "check",
    val cadence: Cadence = Cadence.DAILY,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val reminderTimes: String? = null, // Comma separated HH:mm strings, e.g. "08:00,14:00,20:30"
    val sortOrder: Int = 0,
    val archivedAt: Long? = null,
    val sourceType: SourceType = SourceType.MANUAL,
    val healthConnectTargetValue: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getReminderTimesList(): List<Pair<Int, Int>> {
        if (!reminderTimes.isNullOrBlank()) {
            val list = reminderTimes.split(",")
                .mapNotNull { timeStr ->
                    val parts = timeStr.trim().split(":")
                    if (parts.size == 2) {
                        val h = parts[0].toIntOrNull()
                        val m = parts[1].toIntOrNull()
                        if (h != null && m != null && h in 0..23 && m in 0..59) Pair(h, m) else null
                    } else null
                }
            if (list.isNotEmpty()) return list
        }
        if (reminderHour != null && reminderMinute != null) {
            return listOf(Pair(reminderHour, reminderMinute))
        }
        return emptyList()
    }

    companion object {
        fun formatTimePair(hour: Int, minute: Int): String {
            return String.format("%02d:%02d", hour, minute)
        }

        fun timesListToString(times: List<Pair<Int, Int>>): String? {
            if (times.isEmpty()) return null
            return times.joinToString(",") { (h, m) -> formatTimePair(h, m) }
        }

        fun formatTimeDisplay(hour: Int, minute: Int): String {
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format("%d:%02d %s", displayHour, minute, amPm)
        }
    }
}
