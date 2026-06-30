package com.univpm.fitquest.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsInputValidationTest {
    @Test
    fun bodyWeightAcceptsDotAndCommaDecimals() {
        assertEquals(70.5, parseBodyWeightKgInput("70.5").value ?: -1.0, 0.0)
        assertEquals(70.5, parseBodyWeightKgInput("70,5").value ?: -1.0, 0.0)
    }

    @Test
    fun bodyWeightRejectsInvalidValues() {
        assertEquals(SettingsInputError.Required, parseBodyWeightKgInput(" ").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("abc").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("-1").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("0").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("19.9").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("300.1").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("NaN").error)
        assertEquals(SettingsInputError.WeightKg, parseBodyWeightKgInput("Infinity").error)
    }

    @Test
    fun heightKeepsCurrentForBlankAndAcceptsValidCentimeters() {
        assertEquals(180, parseHeightCmInput(" ", currentValue = 180).value)
        assertEquals(181, parseHeightCmInput("181", currentValue = 180).value)
    }

    @Test
    fun heightRejectsInvalidValues() {
        assertEquals(SettingsInputError.HeightCm, parseHeightCmInput("abc", 180).error)
        assertEquals(SettingsInputError.HeightCm, parseHeightCmInput("-1", 180).error)
        assertEquals(SettingsInputError.HeightCm, parseHeightCmInput("0", 180).error)
        assertEquals(SettingsInputError.HeightCm, parseHeightCmInput("261", 180).error)
    }

    @Test
    fun ageKeepsCurrentForBlankAndAcceptsValidYears() {
        assertEquals(30, parseAgeYearsInput(" ", currentValue = 30).value)
        assertEquals(31, parseAgeYearsInput("31", currentValue = 30).value)
    }

    @Test
    fun ageRejectsInvalidValues() {
        assertEquals(SettingsInputError.AgeYears, parseAgeYearsInput("abc", 30).error)
        assertEquals(SettingsInputError.AgeYears, parseAgeYearsInput("-1", 30).error)
        assertEquals(SettingsInputError.AgeYears, parseAgeYearsInput("0", 30).error)
        assertEquals(SettingsInputError.AgeYears, parseAgeYearsInput("121", 30).error)
    }

    @Test
    fun weeklyGoalKeepsCurrentForBlankAndAcceptsZeroAndCommaDecimal() {
        assertEquals(5.0, parseWeeklyGoalKmInput(" ", currentValue = 5.0).value ?: -1.0, 0.0)
        assertEquals(0.0, parseWeeklyGoalKmInput("0", currentValue = 5.0).value ?: -1.0, 0.0)
        assertEquals(2.5, parseWeeklyGoalKmInput("2,5", currentValue = 5.0).value ?: -1.0, 0.0)
    }

    @Test
    fun weeklyGoalRejectsInvalidValues() {
        assertEquals(SettingsInputError.WeeklyGoalKm, parseWeeklyGoalKmInput("abc", 5.0).error)
        assertEquals(SettingsInputError.WeeklyGoalKm, parseWeeklyGoalKmInput("-1", 5.0).error)
        assertEquals(SettingsInputError.WeeklyGoalKm, parseWeeklyGoalKmInput("NaN", 5.0).error)
        assertEquals(SettingsInputError.WeeklyGoalKm, parseWeeklyGoalKmInput("Infinity", 5.0).error)
    }
}