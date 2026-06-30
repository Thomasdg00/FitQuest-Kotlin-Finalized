package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.RoutePointDao
import com.univpm.fitquest.data.local.dao.WorkoutDao
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val routePointDao: RoutePointDao,
) {
    fun observeWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.observeAll()

    fun observeWorkout(workoutId: Long): Flow<WorkoutEntity?> =
        workoutDao.observeById(workoutId)

    fun observeRoutePoints(workoutId: Long): Flow<List<RoutePointEntity>> =
        routePointDao.observeForWorkout(workoutId)


    suspend fun addWorkout(workout: WorkoutEntity): Long =
        workoutDao.insert(workout)

    suspend fun deleteWorkout(workoutId: Long): Boolean =
        workoutDao.deleteById(workoutId) > 0

    suspend fun addRoutePoints(points: List<RoutePointEntity>) =
        routePointDao.insertAll(points)

}
