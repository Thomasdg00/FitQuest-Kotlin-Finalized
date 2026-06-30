package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.remote.OPEN_METEO_CURRENT_FIELDS
import com.univpm.fitquest.data.remote.OPEN_METEO_DAILY_FIELDS
import com.univpm.fitquest.data.remote.OPEN_METEO_FORECAST_DAYS
import com.univpm.fitquest.data.remote.OPEN_METEO_TIMEZONE
import com.univpm.fitquest.data.remote.OpenMeteoApi
import com.univpm.fitquest.data.remote.toDailyForecasts
import com.univpm.fitquest.data.remote.toWeatherSnapshotDraft
import com.univpm.fitquest.domain.model.DailyForecast
import com.univpm.fitquest.domain.model.WeatherSnapshotDraft

class WeatherRepository(
    private val api: OpenMeteoApi,
) {
    suspend fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
        recordedAtMillis: Long = System.currentTimeMillis(),
    ): WeatherSnapshotDraft? {
        return runCatching {
            api.currentWeather(
                latitude = latitude,
                longitude = longitude,
                current = OPEN_METEO_CURRENT_FIELDS,
                timezone = OPEN_METEO_TIMEZONE,
            ).toWeatherSnapshotDraft(recordedAtMillis)
        }.getOrNull()
    }

    suspend fun fetchDailyForecast(
        latitude: Double,
        longitude: Double,
    ): List<DailyForecast> {
        return runCatching {
            api.dailyForecast(
                latitude = latitude,
                longitude = longitude,
                daily = OPEN_METEO_DAILY_FIELDS,
                forecastDays = OPEN_METEO_FORECAST_DAYS,
                timezone = OPEN_METEO_TIMEZONE,
            ).toDailyForecasts()
        }.getOrDefault(emptyList())
    }
}
