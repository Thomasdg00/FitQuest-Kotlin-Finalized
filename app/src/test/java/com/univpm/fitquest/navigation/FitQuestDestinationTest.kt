package com.univpm.fitquest.navigation

import com.univpm.fitquest.ui.navigation.FitQuestDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class FitQuestDestinationTest {
    @Test
    fun homeRouteIsHome() {
        assertEquals("home", FitQuestDestination.Home.route)
    }

    @Test
    fun trackRouteIsDirectRecordingRoute() {
        assertEquals("track", FitQuestDestination.Track.route)
    }

    @Test
    fun goalsRouteIsGoals() {
        assertEquals("goals", FitQuestDestination.Goals.route)
    }

    @Test
    fun statsRouteIsStats() {
        assertEquals("stats", FitQuestDestination.Stats.route)
    }

    @Test
    fun workoutDetailRouteIncludesWorkoutId() {
        assertEquals("workout-detail/42", FitQuestDestination.WorkoutDetail.routeFor(42L))
    }
}
