package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WorkoutStatsTest {
    private val zoneId = ZoneId.of("UTC")
    private val mondayNoon = LocalDate.of(2026, 6, 1)
        .atTime(12, 0)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

    @Test
    fun weeklyDistanceTotalsIncludeOnlyCompletedWorkoutsFromCurrentWeek() {
        val workouts = listOf(
            workout(Sport.Walking, "2026-06-01", 1_000.0, completed = true),
            workout(Sport.Walking, "2026-05-31", 9_000.0, completed = true),
            workout(Sport.Running, "2026-06-02", 2_500.0, completed = true),
            workout(Sport.Cycling, "2026-06-03", 8_000.0, completed = false),
        )

        val totals = weeklyDistanceMetersBySport(workouts, mondayNoon, zoneId)

        assertEquals(1_000.0, totals.getValue(Sport.Walking), 0.0)
        assertEquals(2_500.0, totals.getValue(Sport.Running), 0.0)
        assertEquals(0.0, totals.getValue(Sport.Cycling), 0.0)
    }

    private fun workout(
        sport: Sport,
        date: String,
        distanceMeters: Double,
        completed: Boolean,
    ) = WorkoutEntity(
        sport = sport.routeValue,
        startedAtMillis = LocalDate.parse(date)
            .atTime(9, 0)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli(),
        distanceMeters = distanceMeters,
        isCompleted = completed,
    )
}
