package com.momentum.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.momentum.app.data.model.MoodLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs WHERE date = :date LIMIT 1")
    fun observeMoodForDate(date: LocalDate): Flow<MoodLog?>

    @Query("SELECT * FROM mood_logs WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeMoodLogsBetween(from: LocalDate, to: LocalDate): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE date = :date LIMIT 1")
    suspend fun getMoodForDateSync(date: LocalDate): MoodLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMoodLog(moodLog: MoodLog)

    @Query("DELETE FROM mood_logs")
    suspend fun deleteAllMoodLogs()
}
