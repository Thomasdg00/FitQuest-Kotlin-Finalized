package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.remote.DailyForecast
import com.univpm.fitquest.data.remote.OpenMeteoClient
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import com.google.android.gms.location.FusedLocationProviderClient
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val weatherForecast: List<DailyForecast> = emptyList(),
    val locationName: String? = null,
    val lastWorkout: WorkoutEntity? = null,
    val lastWorkoutRoutePoints: List<RoutePointEntity> = emptyList(),
    val weeklyDistances: Map<Sport, Double> = Sport.entries.associateWith { 0.0 },
    val weeklyGoals: Map<Sport, Double> = Sport.entries.associateWith { 0.0 },
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val goalRepository: GoalRepository,
    private val openMeteoClient: OpenMeteoClient,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = workoutRepository.observeWorkouts()
        .flatMapLatest { workouts ->
            val latestWorkout = workouts.firstOrNull()
            val routeFlow: Flow<List<RoutePointEntity>> = if (latestWorkout != null) {
                workoutRepository.observeRoutePoints(latestWorkout.id)
            } else {
                flowOf(emptyList())
            }

            routeFlow.flatMapLatest { routePoints ->
                val forecastFlow = forecastForCurrentLocation()
                combine(
                    forecastFlow,
                    goalRepository.observeGoals(),
                ) { forecastData, goals ->
                    val forecast = forecastData.first
                    val locationName = forecastData.second
                    val weeklyProgressMeters = weeklyDistanceMetersBySport(workouts)

                    val weeklyDistances = Sport.entries.associateWith { sport ->
                        metersToKm(weeklyProgressMeters.getValue(sport))
                    }

                    val weeklyGoals = Sport.entries.associateWith { sport ->
                        val goal = goals.firstOrNull { it.sport == sport.routeValue }
                        metersToKm(goal?.weeklyDistanceGoalMeters ?: defaultWeeklyGoalMeters(sport))
                    }

                    HomeUiState(
                        weatherForecast = forecast,
                        locationName = locationName,
                        lastWorkout = latestWorkout,
                        lastWorkoutRoutePoints = routePoints,
                        weeklyDistances = weeklyDistances,
                        weeklyGoals = weeklyGoals,
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    @SuppressLint("MissingPermission")
    private fun forecastForCurrentLocation(): Flow<Pair<List<DailyForecast>, String?>> = callbackFlow {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    launch(Dispatchers.IO) {
                        val forecast = runCatching {
                            openMeteoClient.fetchDailyForecast(location.latitude, location.longitude)
                        }.getOrDefault(emptyList())
                        val locName = runCatching {
                            val address = Geocoder(context, java.util.Locale.getDefault())
                                .getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                            address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        }.getOrNull()
                        trySend(Pair(forecast, locName))
                        close()
                    }
                } else {
                    trySend(Pair(emptyList(), null))
                    close()
                }
            }
            .addOnFailureListener {
                trySend(Pair(emptyList(), null))
                close()
            }
        awaitClose { }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val goalRepository: GoalRepository,
        private val openMeteoClient: OpenMeteoClient,
        private val fusedLocationClient: FusedLocationProviderClient,
        private val context: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(workoutRepository, goalRepository, openMeteoClient, fusedLocationClient, context) as T
        }
    }
}
