package com.univpm.fitquest.data.local.entity

import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomEntityDefaultsTest {
    @Test
    fun newWorkoutStartsWithoutDatabaseId() {
        val workout = WorkoutEntity(
            sport = Sport.Running.routeValue,
            startedAtMillis = 1_000L
        )

        assertEquals(0L, workout.id)
        assertEquals(0L, workout.durationMillis)
        assertEquals(false, workout.isCompleted)
    }

    @Test
    fun defaultUserSettingsUseMetricUnitsAndWalkingSport() {
        val settings = UserSettingsEntity()

        assertEquals(UserSettingsEntity.DEFAULT_ID, settings.id)
        assertEquals(true, settings.useMetricUnits)
        assertEquals(Sport.Walking.routeValue, settings.preferredSport)
        assertEquals("", settings.name)
        assertEquals("", settings.surname)
        assertEquals(0, settings.heightCm)
        assertEquals("en", settings.languageCode)
        assertEquals(ThemeMode.System.storageValue, settings.themeMode)
    }
}
