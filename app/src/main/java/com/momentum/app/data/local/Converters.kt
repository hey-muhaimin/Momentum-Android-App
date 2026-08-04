package com.momentum.app.data.local

import androidx.room.TypeConverter
import com.momentum.app.data.model.Cadence
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromCadence(value: String?): Cadence {
        return value?.let { runCatching { Cadence.valueOf(it) }.getOrDefault(Cadence.DAILY) } ?: Cadence.DAILY
    }

    @TypeConverter
    fun toCadence(cadence: Cadence?): String {
        return (cadence ?: Cadence.DAILY).name
    }
}
