package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.univpm.fitquest.data.local.entity.WeatherSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherSnapshotDao {
    @Upsert
    suspend fun upsert(weatherSnapshot: WeatherSnapshotEntity)

    @Query("SELECT * FROM weather_snapshots WHERE workoutId = :workoutId")
    fun observeForWorkout(workoutId: Long): Flow<WeatherSnapshotEntity?>

    @Query("SELECT * FROM weather_snapshots WHERE workoutId = :workoutId")
    suspend fun getForWorkout(workoutId: Long): WeatherSnapshotEntity?
}
