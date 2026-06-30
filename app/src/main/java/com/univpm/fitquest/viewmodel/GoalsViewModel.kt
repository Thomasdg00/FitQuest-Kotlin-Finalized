package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.GoalEntity
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalProgressUi(
    val sport: Sport,
    val targetKm: Double,
    val progressKm: Double,
) {
    val progressFraction: Float
        get() = if (targetKm <= 0.0) 0f else (progressKm / targetKm).coerceIn(0.0, 1.0).toFloat()
}

data class GoalsUiState(
    val goals: List<GoalProgressUi> = Sport.entries.map {
        GoalProgressUi(sport = it, targetKm = 0.0, progressKm = 0.0)
    }
)

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    workoutRepository: WorkoutRepository
) : ViewModel() {
    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeGoals(),
        workoutRepository.observeWorkouts()
    ) { goals, workouts ->
        val weeklyProgressMeters = weeklyDistanceMetersBySport(workouts)
        GoalsUiState(
            goals = Sport.entries.map { sport ->
                val goal = goals.firstOrNull { it.sport == sport.routeValue }
                GoalProgressUi(
                    sport = sport,
                    targetKm = metersToKm(goal?.weeklyDistanceGoalMeters ?: defaultWeeklyGoalMeters(sport)),
                    progressKm = metersToKm(weeklyProgressMeters.getValue(sport))
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState()
    )

    init {
        viewModelScope.launch {
            Sport.entries.forEach { sport ->
                if (goalRepository.getGoalForSport(sport.routeValue) == null) {
                    goalRepository.saveGoal(
                        GoalEntity(
                            sport = sport.routeValue,
                            weeklyDistanceGoalMeters = defaultWeeklyGoalMeters(sport),
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun saveGoal(sport: Sport, targetKm: Double) {
        viewModelScope.launch {
            val existing = goalRepository.getGoalForSport(sport.routeValue)
            goalRepository.saveGoal(
                GoalEntity(
                    id = existing?.id ?: 0,
                    sport = sport.routeValue,
                    weeklyDistanceGoalMeters = kmToMeters(targetKm.coerceAtLeast(0.0)),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    class Factory(
        private val goalRepository: GoalRepository,
        private val workoutRepository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoalsViewModel(goalRepository, workoutRepository) as T
        }
    }
}
