package com.momentum.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.momentum.app.data.model.CorrelationRow
import com.momentum.app.data.model.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitLogDao {
    @Query("""
        SELECT * FROM habit_logs 
        WHERE habitId = :habitId AND date BETWEEN :from AND :to 
        ORDER BY date ASC
    """)
    fun observeLogsForHabit(habitId: Long, from: LocalDate, to: LocalDate): Flow<List<HabitLog>>

    @Query("""
        SELECT * FROM habit_logs 
        WHERE date BETWEEN :from AND :to
    """)
    fun observeAllLogsBetween(from: LocalDate, to: LocalDate): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogsForDate(date: LocalDate): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDateSync(date: LocalDate): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabitLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitLog(habitId: Long, date: LocalDate)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Query("""
        SELECT m.date AS date, m.score AS score, h.completed AS completed 
        FROM mood_logs m
        LEFT JOIN habit_logs h ON h.date = m.date AND h.habitId = :habitId
        WHERE m.date BETWEEN :from AND :to
        ORDER BY m.date ASC
    """)
    fun observeCorrelationRows(habitId: Long, from: LocalDate, to: LocalDate): Flow<List<CorrelationRow>>

    @Query("""
        SELECT m.date AS date, m.score AS score, h.completed AS completed 
        FROM mood_logs m
        LEFT JOIN habit_logs h ON h.date = m.date AND h.habitId = :habitId
        WHERE m.date BETWEEN :from AND :to
        ORDER BY m.date ASC
    """)
    suspend fun getCorrelationRowsSync(habitId: Long, from: LocalDate, to: LocalDate): List<CorrelationRow>
}
