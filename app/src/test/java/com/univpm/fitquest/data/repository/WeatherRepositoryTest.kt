package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.remote.OpenMeteoApi
import com.univpm.fitquest.data.remote.OpenMeteoCurrentDto
import com.univpm.fitquest.data.remote.OpenMeteoCurrentResponseDto
import com.univpm.fitquest.data.remote.OpenMeteoDailyDto
import com.univpm.fitquest.data.remote.OpenMeteoDailyResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun fetchCurrentWeatherMapsSuccessfulResponse() = runBlocking {
        val repository = WeatherRepository(
            api = FakeOpenMeteoApi(
                currentResponse = OpenMeteoCurrentResponseDto(
                    current = OpenMeteoCurrentDto(
                        temperatureCelsius = 18.0,
                        relativeHumidityPercent = 72.0,
                        windSpeedKmh = 5.5,
                        weatherCode = 61,
                    ),
                ),
            ),
        )

        val weather = repository.fetchCurrentWeather(
            latitude = 43.0,
            longitude = 13.0,
            recordedAtMillis = 456L,
        )

        assertEquals(456L, weather?.recordedAtMillis)
        assertEquals(18.0, weather?.temperatureCelsius ?: 0.0, 0.0)
        assertEquals(61, weather?.weatherCode)
    }

    @Test
    fun fetchCurrentWeatherReturnsNullOnFailure() = runBlocking {
        val repository = WeatherRepository(api = FakeOpenMeteoApi(currentFailure = true))

        val weather = repository.fetchCurrentWeather(
            latitude = 43.0,
            longitude = 13.0,
            recordedAtMillis = 456L,
        )

        assertNull(weather)
    }

    @Test
    fun fetchDailyForecastReturnsEmptyListOnFailure() = runBlocking {
        val repository = WeatherRepository(api = FakeOpenMeteoApi(dailyFailure = true))

        val forecast = repository.fetchDailyForecast(latitude = 43.0, longitude = 13.0)

        assertEquals(emptyList<Any>(), forecast)
    }

    @Test
    fun fetchDailyForecastMapsSuccessfulResponse() = runBlocking {
        val repository = WeatherRepository(
            api = FakeOpenMeteoApi(
                dailyResponse = OpenMeteoDailyResponseDto(
                    daily = OpenMeteoDailyDto(
                        dates = listOf("2026-06-10"),
                        weatherCodes = listOf(0),
                        maxTemperatures = listOf(24.0),
                        minTemperatures = listOf(14.0),
                    ),
                ),
            ),
        )

        val forecast = repository.fetchDailyForecast(latitude = 43.0, longitude = 13.0)

        assertEquals(1, forecast.size)
        assertEquals("2026-06-10", forecast[0].date)
        assertEquals(0, forecast[0].weatherCode)
    }
}

private class FakeOpenMeteoApi(
    private val currentResponse: OpenMeteoCurrentResponseDto = OpenMeteoCurrentResponseDto(current = null),
    private val dailyResponse: OpenMeteoDailyResponseDto = OpenMeteoDailyResponseDto(daily = null),
    private val currentFailure: Boolean = false,
    private val dailyFailure: Boolean = false,
) : OpenMeteoApi {
    override suspend fun currentWeather(
        latitude: Double,
        longitude: Double,
        current: String,
        timezone: String,
    ): OpenMeteoCurrentResponseDto {
        if (currentFailure) error("current failed")
        return currentResponse
    }

    override suspend fun dailyForecast(
        latitude: Double,
        longitude: Double,
        daily: String,
        forecastDays: Int,
        timezone: String,
    ): OpenMeteoDailyResponseDto {
        if (dailyFailure) error("daily failed")
        return dailyResponse
    }
}