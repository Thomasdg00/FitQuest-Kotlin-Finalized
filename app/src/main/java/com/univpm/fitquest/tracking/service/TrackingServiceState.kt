package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.domain.model.Sport

enum class TrackingLifecycleState {
    Idle,
    Running,
    Paused,
    Stopping,
}

enum class LocationQuality {
    Unknown,
    Approximate,
    Precise
}

data class TrackingServiceState(
    val lifecycleState: TrackingLifecycleState = TrackingLifecycleState.Idle,
    val sport: Sport? = null,
    val elapsedMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val latestLatitude: Double? = null,
    val latestLongitude: Double? = null,
    val currentSpeedMetersPerSecond: Float? = null,
    val currentAltitudeMeters: Double? = null,
    val routePoints: List<InMemoryRoutePoint> = emptyList(),
    val estimatedCaloriesKcal: Double = 0.0,
    val averageCadenceStepsPerMinute: Int? = null,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val stepCounterAvailable: Boolean = false,
    val weatherStatus: WeatherCaptureStatus = WeatherCaptureStatus.NotStarted,
    val currentLocationQuality: LocationQuality = LocationQuality.Unknown,
    val errorMessage: String? = null,
)

enum class WeatherCaptureStatus {
    NotStarted,
    WaitingForLocation,
    Loading,
    Saved,
    Failed,
}

data class InMemoryRoutePoint(
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
    val speedMetersPerSecond: Float?,
)
