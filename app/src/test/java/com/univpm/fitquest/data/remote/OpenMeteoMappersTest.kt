package com.univpm.fitquest.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenMeteoMappersTest {
    @Test
    fun mapsCurrentWeatherToSnapshotDraft() {
        val response = OpenMeteoCurrentResponseDto(
            current = OpenMeteoCurrentDto(
                temperatureCelsius = 21.5,
                relativeHumidityPercent = 64.0,
                windSpeedKmh = 8.4,
                weatherCode = 3,
            ),
        )

        val draft = response.toWeatherSnapshotDraft(recordedAtMillis = 123L)

        assertEquals(123L, draft?.recordedAtMillis)
        assertEquals(21.5, draft?.temperatureCelsius ?: 0.0, 0.0)
        assertEquals(64.0, draft?.relativeHumidityPercent ?: 0.0, 0.0)
        assertEquals(8.4, draft?.windSpeedKmh ?: 0.0, 0.0)
        assertEquals(3, draft?.weatherCode)
    }

    @Test
    fun missingCurrentWeatherReturnsNullSnapshotDraft() {
        assertNull(OpenMeteoCurrentResponseDto(current = null).toWeatherSnapshotDraft(recordedAtMillis = 123L))
    }

    @Test
    fun mapsDailyForecastToAtMostThreeRows() {
        val response = OpenMeteoDailyResponseDto(
            daily = OpenMeteoDailyDto(
                dates = listOf("2026-06-10", "2026-06-11", "2026-06-12", "2026-06-13"),
                weatherCodes = listOf(0, 3, 61, 95),
                maxTemperatures = listOf(25.2, 23.6, 21.4, 20.0),
                minTemperatures = listOf(15.1, 14.8, 13.2, 12.0),
            ),
        )

        val forecast = response.toDailyForecasts()

        assertEquals(3, forecast.size)
        assertEquals("2026-06-10", forecast[0].date)
        assertEquals(0, forecast[0].weatherCode)
        assertEquals(25.2, forecast[0].maxTemperatureCelsius ?: 0.0, 0.0)
        assertEquals(15.1, forecast[0].minTemperatureCelsius ?: 0.0, 0.0)
        assertEquals("2026-06-12", forecast[2].date)
        assertEquals(61, forecast[2].weatherCode)
    }

    @Test
    fun missingDailyForecastReturnsEmptyList() {
        assertEquals(emptyList<Any>(), OpenMeteoDailyResponseDto(daily = null).toDailyForecasts())
    }
}