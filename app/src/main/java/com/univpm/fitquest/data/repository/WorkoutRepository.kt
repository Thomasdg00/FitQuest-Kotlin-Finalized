package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.RoutePointDao
import com.univpm.fitquest.data.local.dao.WeatherSnapshotDao
import com.univpm.fitquest.data.local.dao.WorkoutDao
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WeatherSnapshotEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val routePointDao: RoutePointDao,
    private val weatherSnapshotDao: WeatherSnapshotDao
) {
    fun observeWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.observeAll()

    fun observeWorkoutsBySport(sport: String): Flow<List<WorkoutEntity>> =
        workoutDao.observeBySport(sport)

    fun observeWorkout(workoutId: Long): Flow<WorkoutEntity?> =
        workoutDao.observeById(workoutId)

    fun observeRoutePoints(workoutId: Long): Flow<List<RoutePointEntity>> =
        routePointDao.observeForWorkout(workoutId)

    fun observeWeather(workoutId: Long): Flow<WeatherSnapshotEntity?> =
        weatherSnapshotDao.observeForWorkout(workoutId)

    suspend fun getWorkout(workoutId: Long): WorkoutEntity? =
        workoutDao.getById(workoutId)

    suspend fun addWorkout(workout: WorkoutEntity): Long =
        workoutDao.insert(workout)

    suspend fun updateWorkout(workout: WorkoutEntity) =
        workoutDao.update(workout)

    suspend fun deleteWorkout(workout: WorkoutEntity) =
        workoutDao.delete(workout)

    suspend fun deleteWorkout(workoutId: Long): Boolean =
        workoutDao.deleteById(workoutId) > 0

    suspend fun addRoutePoint(point: RoutePointEntity): Long =
        routePointDao.insert(point)

    suspend fun addRoutePoints(points: List<RoutePointEntity>) =
        routePointDao.insertAll(points)

    suspend fun saveWeather(weatherSnapshot: WeatherSnapshotEntity) =
        weatherSnapshotDao.upsert(weatherSnapshot)
}
