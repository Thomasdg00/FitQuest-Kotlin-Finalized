package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.domain.model.DailyForecast
import com.univpm.fitquest.domain.model.WeatherForecastResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelWeatherTest {

    @Test
    fun weatherStateFromResultKeepsErrorSeparateFromEmptyForecast() {
        val state = weatherStateFromResult(WeatherForecastResult.Error)

        assertEquals(HomeWeatherForecastState.Error, state)
    }

    @Test
    fun weatherStateFromResultKeepsEmptySeparateFromSuccess() {
        val state = weatherStateFromResult(WeatherForecastResult.Empty)

        assertEquals(HomeWeatherForecastState.Empty, state)
    }

    @Test
    fun weatherStateFromResultExposesSuccessfulForecasts() {
        val forecast = DailyForecast(
            date = "2026-06-10",
            weatherCode = 0,
            maxTemperatureCelsius = 24.0,
            minTemperatureCelsius = 14.0,
        )

        val state = weatherStateFromResult(WeatherForecastResult.Success(listOf(forecast)))

        assertTrue(state is HomeWeatherForecastState.Available)
        assertEquals(listOf(forecast), (state as HomeWeatherForecastState.Available).forecasts)
    }
}
