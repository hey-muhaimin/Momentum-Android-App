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

    @TypeConverter
    fun fromSourceType(value: String?): com.momentum.app.data.model.SourceType {
        return value?.let { runCatching { com.momentum.app.data.model.SourceType.valueOf(it) }.getOrDefault(com.momentum.app.data.model.SourceType.MANUAL) } ?: com.momentum.app.data.model.SourceType.MANUAL
    }

    @TypeConverter
    fun toSourceType(sourceType: com.momentum.app.data.model.SourceType?): String {
        return (sourceType ?: com.momentum.app.data.model.SourceType.MANUAL).name
    }

    @TypeConverter
    fun fromTimeBin(value: String?): com.momentum.app.data.model.TimeBin {
        return value?.let { runCatching { com.momentum.app.data.model.TimeBin.valueOf(it) }.getOrDefault(com.momentum.app.data.model.TimeBin.MORNING) } ?: com.momentum.app.data.model.TimeBin.MORNING
    }

    @TypeConverter
    fun toTimeBin(timeBin: com.momentum.app.data.model.TimeBin?): String {
        return (timeBin ?: com.momentum.app.data.model.TimeBin.MORNING).name
    }
}
