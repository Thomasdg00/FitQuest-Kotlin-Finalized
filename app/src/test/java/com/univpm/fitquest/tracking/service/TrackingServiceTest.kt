package com.univpm.fitquest.tracking.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceTest {

    @Test
    fun locationSamplesRejectInvalidCoordinates() {
        assertFalse(isUsableLocationSample(latitude = 91.0, longitude = 13.5, accuracyMeters = 5f))
        assertFalse(isUsableLocationSample(latitude = 43.6, longitude = -181.0, accuracyMeters = 5f))
        assertTrue(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 5f))
    }

    @Test
    fun gpsElevationDeltaReportsGainAndLoss() {
        val gain = gpsElevationDelta(
            previousAltitudeMeters = 100.0,
            currentAltitudeMeters = 150.0,
        )
        val loss = gpsElevationDelta(
            previousAltitudeMeters = 150.0,
            currentAltitudeMeters = 125.0,
        )

        assertEquals(50.0, gain ?: 0.0, 0.0)
        assertEquals(-25.0, loss ?: 0.0, 0.0)
    }

    @Test
    fun gpsElevationDeltaIgnoresMissingAltitude() {
        assertNull(
            gpsElevationDelta(
                previousAltitudeMeters = null,
                currentAltitudeMeters = 150.0,
            ),
        )
        assertNull(
            gpsElevationDelta(
                previousAltitudeMeters = 100.0,
                currentAltitudeMeters = null,
            ),
        )
    }
}
