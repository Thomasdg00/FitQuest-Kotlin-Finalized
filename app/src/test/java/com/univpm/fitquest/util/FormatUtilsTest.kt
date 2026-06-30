package com.univpm.fitquest.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {
    @Test
    fun formatsDurationAsTimerText() {
        assertEquals("00:05", FormatUtils.formatDuration(5_000L))
        assertEquals("01:02", FormatUtils.formatDuration(62_000L))
        assertEquals("1:01:01", FormatUtils.formatDuration(3_661_000L))
        assertEquals(61L, FormatUtils.formatDurationMinutes(3_661_000L))
    }

    @Test
    fun formatsDistanceForMetersAndKilometers() {
        assertEquals("850 m", FormatUtils.formatDistance(850.0))
        assertEquals("1.25 km", FormatUtils.formatDistance(1_250.0))
    }

    @Test
    fun formatsPaceAndSpeed() {
        assertEquals("5:00 /km", FormatUtils.formatPace(1000f / 300f))
        assertEquals("--", FormatUtils.formatPace(0.0f))
        assertEquals("12.0 km/h", FormatUtils.formatSpeed(1000.0 / 300.0))
    }

    @Test
    fun formatsCaloriesElevationAndDecimalKm() {
        assertEquals("343 kcal", FormatUtils.formatCalories(342.6))
        assertEquals("12 m", FormatUtils.formatElevation(12.4))
        assertEquals("4.5", FormatUtils.formatDecimalKm(4.45))
    }
}
