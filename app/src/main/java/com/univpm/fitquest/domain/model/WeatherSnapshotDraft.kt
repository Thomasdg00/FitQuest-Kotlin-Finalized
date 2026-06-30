package com.univpm.fitquest.domain.model

data class WeatherSnapshotDraft(
    val recordedAtMillis: Long,
    val temperatureCelsius: Double?,
    val relativeHumidityPercent: Double?,
    val windSpeedKmh: Double?,
    val weatherCode: Int?,
)
