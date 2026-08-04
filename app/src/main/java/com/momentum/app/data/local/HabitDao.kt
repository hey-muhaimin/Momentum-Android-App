package com.momentum.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.momentum.app.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archivedAt IS NULL ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY archivedAt ASC, sortOrder ASC, createdAt ASC")
    fun observeAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE archivedAt IS NULL ORDER BY sortOrder ASC")
    suspend fun getActiveHabitsSync(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("UPDATE habits SET archivedAt = :archivedAt WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long, archivedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
