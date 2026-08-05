package com.momentum.app.data.model

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

enum class TimeBin(val displayName: String, val hourRangeDisplay: String) {
    MORNING("Morning", "4am - 12pm"),
    AFTERNOON("Afternoon", "12pm - 5pm"),
    EVENING("Evening", "5pm - 10pm"),
    NIGHT("Night", "10pm - 4am");

    companion object {
        fun fromHour(hour: Int): TimeBin {
            return when (hour) {
                in 4..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..21 -> EVENING
                else -> NIGHT
            }
        }

        fun fromTimestamp(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): TimeBin {
            val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
            return fromHour(zdt.hour)
        }
    }
}
