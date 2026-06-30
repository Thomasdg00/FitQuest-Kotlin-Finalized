package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.data.local.entity.RoutePointEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ChartPointUi(
    val x: Double,
    val y: Double,
)

data class WorkoutRouteChartsUi(
    val pacePoints: List<ChartPointUi> = emptyList(),
    val elevationPoints: List<ChartPointUi> = emptyList(),
)

fun buildWorkoutRouteCharts(routePoints: List<RoutePointEntity>): WorkoutRouteChartsUi {
    val sortedPoints = routePoints.sortedBy { it.sequenceIndex }
    if (sortedPoints.size < 2) return WorkoutRouteChartsUi()

    val firstTime = sortedPoints.first().recordedAtMillis
    val pacePoints = sortedPoints.mapNotNull { point ->
        val speed = point.speedMetersPerSecond?.toDouble() ?: return@mapNotNull null
        if (speed <= 0.1) return@mapNotNull null
        ChartPointUi(
            x = (point.recordedAtMillis - firstTime).coerceAtLeast(0L) / 60_000.0,
            y = 1_000.0 / speed / 60.0,
        )
    }

    var cumulativeDistanceMeters = 0.0
    val elevationPoints = mutableListOf<ChartPointUi>()
    sortedPoints.zipWithNext().forEach { (previous, current) ->
        cumulativeDistanceMeters += distanceMeters(
            previous.latitude,
            previous.longitude,
            current.latitude,
            current.longitude,
        )
        current.altitudeMeters?.let { altitude ->
            elevationPoints += ChartPointUi(
                x = cumulativeDistanceMeters / 1_000.0,
                y = altitude,
            )
        }
    }

    return WorkoutRouteChartsUi(
        pacePoints = pacePoints,
        elevationPoints = elevationPoints,
    )
}

private fun distanceMeters(
    startLatitude: Double,
    startLongitude: Double,
    endLatitude: Double,
    endLongitude: Double,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val startLatRad = Math.toRadians(startLatitude)
    val endLatRad = Math.toRadians(endLatitude)
    val deltaLat = Math.toRadians(endLatitude - startLatitude)
    val deltaLon = Math.toRadians(endLongitude - startLongitude)
    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(startLatRad) * cos(endLatRad) *
        sin(deltaLon / 2) * sin(deltaLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
}
