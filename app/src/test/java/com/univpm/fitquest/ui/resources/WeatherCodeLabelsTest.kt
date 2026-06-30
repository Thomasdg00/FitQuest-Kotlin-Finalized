package com.univpm.fitquest.ui.resources

import com.univpm.fitquest.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeLabelsTest {
    @Test
    fun clearSkyUsesDedicatedWeatherLabel() {
        assertEquals(R.string.weather_condition_clear_sky, weatherCodeToLabelRes(0))
    }

    @Test
    fun thunderstormWithHailUsesThunderstormLabel() {
        assertEquals(R.string.weather_condition_thunderstorm, weatherCodeToLabelRes(96))
        assertEquals(R.string.weather_condition_thunderstorm, weatherCodeToLabelRes(99))
    }

    @Test
    fun unknownWeatherCodeUsesFallbackLabel() {
        assertEquals(R.string.weather_condition_unknown, weatherCodeToLabelRes(999))
    }
}
