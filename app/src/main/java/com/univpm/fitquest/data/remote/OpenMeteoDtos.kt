package com.univpm.fitquest.data.remote

import com.squareup.moshi.Json

data class OpenMeteoCurrentResponseDto(
    @param:Json(name = "current") val current: OpenMeteoCurrentDto?,
)

data class OpenMeteoCurrentDto(
    @param:Json(name = "temperature_2m") val temperatureCelsius: Double?,
    @param:Json(name = "relative_humidity_2m") val relativeHumidityPercent: Double?,
    @param:Json(name = "wind_speed_10m") val windSpeedKmh: Double?,
    @param:Json(name = "weather_code") val weatherCode: Int?,
)

data class OpenMeteoDailyResponseDto(
    @param:Json(name = "daily") val daily: OpenMeteoDailyDto?,
)

data class OpenMeteoDailyDto(
    @param:Json(name = "time") val dates: List<String>?,
    @param:Json(name = "weather_code") val weatherCodes: List<Int?>?,
    @param:Json(name = "temperature_2m_max") val maxTemperatures: List<Double?>?,
    @param:Json(name = "temperature_2m_min") val minTemperatures: List<Double?>?,
)
