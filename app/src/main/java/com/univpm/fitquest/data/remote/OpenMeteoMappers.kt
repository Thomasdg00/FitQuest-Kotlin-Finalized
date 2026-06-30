package com.univpm.fitquest.data.remote

import com.univpm.fitquest.domain.model.DailyForecast
import com.univpm.fitquest.domain.model.WeatherSnapshotDraft

internal fun OpenMeteoCurrentResponseDto.toWeatherSnapshotDraft(
    recordedAtMillis: Long,
): WeatherSnapshotDraft? {
    val currentWeather = current ?: return null
    return WeatherSnapshotDraft(
        recordedAtMillis = recordedAtMillis,
        temperatureCelsius = currentWeather.temperatureCelsius,
        relativeHumidityPercent = currentWeather.relativeHumidityPercent,
        windSpeedKmh = currentWeather.windSpeedKmh,
        weatherCode = currentWeather.weatherCode,
    )
}

internal fun OpenMeteoDailyResponseDto.toDailyForecasts(): List<DailyForecast> {
    val dailyForecast = daily ?: return emptyList()
    val dates = dailyForecast.dates.orEmpty()
    return dates.take(OPEN_METEO_FORECAST_DAYS).mapIndexed { index, date ->
        DailyForecast(
            date = date,
            weatherCode = dailyForecast.weatherCodes?.getOrNull(index),
            maxTemperatureCelsius = dailyForecast.maxTemperatures?.getOrNull(index),
            minTemperatureCelsius = dailyForecast.minTemperatures?.getOrNull(index),
        )
    }.filter { it.date.isNotBlank() }
}
