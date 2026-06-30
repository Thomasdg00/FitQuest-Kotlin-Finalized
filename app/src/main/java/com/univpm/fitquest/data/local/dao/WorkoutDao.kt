package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long): Int

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun observeById(workoutId: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>
}
