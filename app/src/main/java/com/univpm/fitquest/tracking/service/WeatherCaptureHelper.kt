package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.data.repository.WeatherRepository
import com.univpm.fitquest.domain.model.WeatherSnapshotDraft

internal class WeatherCaptureHelper(
    private val weatherRepository: WeatherRepository,
) {
    suspend fun captureCurrentWeather(
        latitude: Double,
        longitude: Double,
    ): WeatherCaptureResult {
        val snapshotDraft = runCatching {
            weatherRepository.fetchCurrentWeather(latitude, longitude)
        }.getOrNull()

        return WeatherCaptureResult(
            snapshotDraft = snapshotDraft,
            status = if (snapshotDraft != null) WeatherCaptureStatus.Saved else WeatherCaptureStatus.Failed,
        )
    }
}

internal data class WeatherCaptureResult(
    val snapshotDraft: WeatherSnapshotDraft?,
    val status: WeatherCaptureStatus,
)
