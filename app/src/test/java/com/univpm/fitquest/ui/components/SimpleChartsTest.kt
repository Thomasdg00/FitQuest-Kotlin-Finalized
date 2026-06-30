package com.univpm.fitquest.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleChartsTest {
    @Test
    fun chartAxisTicksUseReadableRoundedStepsAcrossDataRange() {
        assertEquals(
            listOf(0.0, 5.0, 10.0),
            buildChartAxisTicks(minValue = 1.2, maxValue = 9.8, preferredTickCount = 4),
        )
    }

    @Test
    fun chartAxisTicksExpandFlatDataAroundSingleValue() {
        assertEquals(
            listOf(10.0, 11.0, 12.0),
            buildChartAxisTicks(minValue = 11.0, maxValue = 11.0, preferredTickCount = 3),
        )
    }

    @Test
    fun chartLabelsFormatWorkoutUnits() {
        assertEquals("4.5 min", formatChartMinutes(4.5))
        assertEquals("5:30 min/km", formatChartPace(5.5))
        assertEquals("123 m", formatChartMeters(123.4))
    }
}
