package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.GoalEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val weekly: PeriodStatsUi = PeriodStatsUi(),
    val monthly: PeriodStatsUi = PeriodStatsUi(),
    val sportBreakdown: List<SportBreakdownUi> = Sport.entries.map { SportBreakdownUi(sport = it) },
    val goalProgress: List<GoalProgressUi> = Sport.entries.map { GoalProgressUi(sport = it, targetKm = 0.0, progressKm = 0.0) },
) {
    val hasWorkouts: Boolean
        get() = monthly.workoutCount > 0 || weekly.workoutCount > 0
}

data class PeriodStatsUi(
    val totalDistanceKm: Double = 0.0,
    val totalDurationMinutes: Long = 0L,
    val workoutCount: Int = 0,
    val totalCaloriesKcal: Double = 0.0,
)

data class SportBreakdownUi(
    val sport: Sport,
    val weeklyDistanceKm: Double = 0.0,
    val monthlyDistanceKm: Double = 0.0,
    val workoutCount: Int = 0,
)

class StatsViewModel(
    workoutRepository: WorkoutRepository,
    goalRepository: GoalRepository,
) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = combine(
        workoutRepository.observeWorkouts(),
        goalRepository.observeGoals(),
    ) { workouts, goals ->
        workouts.toStatsUiState(goals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val goalRepository: GoalRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(workoutRepository, goalRepository) as T
        }
    }
}

private fun List<WorkoutEntity>.toStatsUiState(goals: List<GoalEntity>): StatsUiState {
    val completed = filter { it.isCompleted }
    val now = System.currentTimeMillis()
    val weekStart = startOfWeekMillis(now)
    val monthStart = startOfMonthMillis(now)
    val weeklyWorkouts = completed.filter { it.startedAtMillis >= weekStart }
    val monthlyWorkouts = completed.filter { it.startedAtMillis >= monthStart }

    return StatsUiState(
        weekly = weeklyWorkouts.toPeriodStats(),
        monthly = monthlyWorkouts.toPeriodStats(),
        sportBreakdown = Sport.entries.map { sport ->
            SportBreakdownUi(
                sport = sport,
                weeklyDistanceKm = weeklyWorkouts.distanceKmForSport(sport),
                monthlyDistanceKm = monthlyWorkouts.distanceKmForSport(sport),
                workoutCount = monthlyWorkouts.count { it.sport == sport.routeValue },
            )
        },
        goalProgress = Sport.entries.map { sport ->
            val goal = goals.firstOrNull { it.sport == sport.routeValue }
            val targetMeters = goal?.weeklyDistanceGoalMeters ?: defaultWeeklyGoalMeters(sport)
            GoalProgressUi(
                sport = sport,
                targetKm = targetMeters / 1_000.0,
                progressKm = weeklyWorkouts
                    .filter { it.sport == sport.routeValue }
                    .sumOf { it.distanceMeters } / 1_000.0,
            )
        },
    )
}

private fun List<WorkoutEntity>.toPeriodStats(): PeriodStatsUi {
    return PeriodStatsUi(
        totalDistanceKm = sumOf { it.distanceMeters } / 1_000.0,
        totalDurationMinutes = sumOf { it.durationMillis } / 60_000L,
        workoutCount = size,
        totalCaloriesKcal = sumOf { it.caloriesKcal },
    )
}

private fun List<WorkoutEntity>.distanceKmForSport(sport: Sport): Double {
    return filter { it.sport == sport.routeValue }.sumOf { it.distanceMeters } / 1_000.0
}

private fun startOfWeekMillis(nowMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfMonthMillis(nowMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
