package com.univpm.fitquest.tracking.calories

import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetCalorieCalculatorTest {
    @Test
    fun defaultValuesAreUsedWhenProfileIncomplete() {
        val calories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Running,
            durationMillis = 30 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(),
        )

        assertEquals(296.4, calories, 0.1)
    }

    @Test
    fun userValuesOverrideDefaults() {
        val defaultCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Running,
            durationMillis = 30 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(),
        )
        val customCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Running,
            durationMillis = 30 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(
                weightKg = 80.0,
                heightCm = 180,
                ageYears = 40,
                sex = Sex.Male,
            ),
        )

        assertEquals(353.2, customCalories, 0.1)
        assertTrue(customCalories > defaultCalories)
    }

    @Test
    fun maleAndFemaleSexConstantsChangeResult() {
        val femaleCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 60 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(
                weightKg = 70.0,
                heightCm = 170,
                ageYears = 30,
                sex = Sex.Female,
            ),
        )
        val maleCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 60 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(
                weightKg = 70.0,
                heightCm = 170,
                ageYears = 30,
                sex = Sex.Male,
            ),
        )

        assertEquals(181.4, femaleCalories, 0.1)
        assertEquals(202.2, maleCalories, 0.1)
        assertTrue(maleCalories > femaleCalories)
    }

    @Test
    fun zeroDurationReturnsZeroCalories() {
        val calories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 0L,
            distanceMeters = 0.0,
            profile = CalorieProfile(
                weightKg = 80.0,
                heightCm = 180,
                ageYears = 40,
                sex = Sex.Male,
            ),
        )

        assertEquals(0.0, calories, 0.0)
    }

    @Test
    fun invalidProfileValuesFallBackToDefaults() {
        val invalidCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Running,
            durationMillis = 30 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(
                weightKg = -5.0,
                heightCm = 0,
                ageYears = -1,
                sex = null,
            ),
        )
        val defaultCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Running,
            durationMillis = 30 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(),
        )

        assertEquals(defaultCalories, invalidCalories, 0.0)
    }

    @Test
    fun stationaryAverageSpeedUsesRestingMet() {
        val calories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 60 * 60_000L,
            distanceMeters = 100.0,
            profile = CalorieProfile(
                weightKg = 70.0,
                heightCm = 170,
                ageYears = 30,
                sex = Sex.Female,
            ),
        )

        assertEquals(60.4, calories, 0.1)
    }

    @Test
    fun positiveElevationGainAddsClimbEnergy() {
        val flatCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 60 * 60_000L,
            distanceMeters = 5_000.0,
            profile = CalorieProfile(weightKg = 80.0),
        )
        val climbingCalories = MetCalorieCalculator.estimateKcal(
            sport = Sport.Walking,
            durationMillis = 60 * 60_000L,
            distanceMeters = 5_000.0,
            elevationGainMeters = 100.0,
            profile = CalorieProfile(weightKg = 80.0),
        )

        assertEquals(7.2, climbingCalories - flatCalories, 0.1)
    }
}
