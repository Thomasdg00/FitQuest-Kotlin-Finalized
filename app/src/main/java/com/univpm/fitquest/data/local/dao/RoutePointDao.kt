package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<RoutePointEntity>)

    @Query("SELECT * FROM route_points WHERE workoutId = :workoutId ORDER BY sequenceIndex ASC, recordedAtMillis ASC")
    fun observeForWorkout(workoutId: Long): Flow<List<RoutePointEntity>>

    @Query("SELECT * FROM route_points WHERE workoutId = :workoutId ORDER BY sequenceIndex ASC, recordedAtMillis ASC")
    suspend fun getForWorkout(workoutId: Long): List<RoutePointEntity>

    @Query("DELETE FROM route_points WHERE workoutId = :workoutId")
    suspend fun deleteForWorkout(workoutId: Long)
}
