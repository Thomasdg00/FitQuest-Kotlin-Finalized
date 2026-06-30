package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long): Int

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun observeById(workoutId: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE sport = :sport ORDER BY startedAtMillis DESC")
    fun observeBySport(sport: String): Flow<List<WorkoutEntity>>
}
