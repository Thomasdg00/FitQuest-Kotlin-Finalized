package com.univpm.fitquest.ui.screens.goals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoalsScreenValidationTest {
    @Test
    fun goalTargetInputAcceptsZeroAndDecimalComma() {
        assertEquals(0.0, parseGoalTargetKmInput("0") ?: -1.0, 0.0)
        assertEquals(2.5, parseGoalTargetKmInput("2,5") ?: -1.0, 0.0)
    }

    @Test
    fun goalTargetInputRejectsInvalidValues() {
        assertNull(parseGoalTargetKmInput("abc"))
        assertNull(parseGoalTargetKmInput("-1"))
    }

    @Test
    fun goalTargetInputTreatsBlankAsNoValue() {
        assertNull(parseGoalTargetKmInput(" "))
    }
}
