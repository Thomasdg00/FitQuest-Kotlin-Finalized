package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.data.local.entity.WeatherSnapshotEntity
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.WeatherSnapshotDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class WorkoutSaveCoordinator(
    private val workoutRepository: WorkoutRepository,
) {
    suspend fun saveCompletedWorkout(request: CompletedWorkoutSaveRequest) {
        withContext(Dispatchers.IO) {
            val workout = buildTrackedWorkoutEntity(
                sport = request.sport,
                startedAtMillis = request.startedAtMillis,
                endedAtMillis = request.endedAtMillis,
                durationMillis = request.durationMillis,
                distanceMeters = request.distanceMeters,
                caloriesKcal = request.caloriesKcal,
                elevationGainMeters = request.elevationGainMeters,
                elevationLossMeters = request.elevationLossMeters,
                averageCadenceStepsPerMinute = request.cadenceStepsPerMinute,
            )
            val workoutId = workoutRepository.addWorkout(workout)
            val routeEntities = request.routeSnapshot.toRoutePointEntities(workoutId)
            if (routeEntities.isNotEmpty()) {
                workoutRepository.addRoutePoints(routeEntities)
            }
            request.weatherSnapshotDraft?.let { weather ->
                workoutRepository.saveWeather(weather.toEntity(workoutId))
            }
        }
    }
}

internal data class CompletedWorkoutSaveRequest(
    val sport: Sport,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationMillis: Long,
    val distanceMeters: Double,
    val routeSnapshot: List<InMemoryRoutePoint>,
    val caloriesKcal: Double,
    val cadenceStepsPerMinute: Int?,
    val elevationGainMeters: Double,
    val elevationLossMeters: Double,
    val weatherSnapshotDraft: WeatherSnapshotDraft?,
)

private fun WeatherSnapshotDraft.toEntity(workoutId: Long): WeatherSnapshotEntity {
    return WeatherSnapshotEntity(
        workoutId = workoutId,
        recordedAtMillis = recordedAtMillis,
        temperatureCelsius = temperatureCelsius,
        relativeHumidityPercent = relativeHumidityPercent,
        windSpeedKmh = windSpeedKmh,
        weatherCode = weatherCode,
    )
}
