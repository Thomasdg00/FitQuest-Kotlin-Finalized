package com.univpm.fitquest.tracking.service

const val MAX_ACCEPTED_ACCURACY_METERS_TRACKING = 30f
const val MAX_ACCEPTED_ACCURACY_METERS_INITIAL = 500f

fun isUsableLocationSample(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float?,
    isInitialFix: Boolean = false,
): Boolean {
    val validCoordinates = latitude in -90.0..90.0 && longitude in -180.0..180.0
    val maxAccuracy = if (isInitialFix) MAX_ACCEPTED_ACCURACY_METERS_INITIAL else MAX_ACCEPTED_ACCURACY_METERS_TRACKING
    val accurateEnough = accuracyMeters == null || accuracyMeters <= maxAccuracy
    return validCoordinates && accurateEnough
}

fun determineLocationQuality(accuracyMeters: Float?): LocationQuality {
    if (accuracyMeters == null) return LocationQuality.Unknown
    return if (accuracyMeters <= MAX_ACCEPTED_ACCURACY_METERS_TRACKING) {
        LocationQuality.Precise
    } else {
        LocationQuality.Approximate
    }
}

fun gpsElevationDelta(
    previousAltitudeMeters: Double?,
    currentAltitudeMeters: Double?,
): Double? {
    if (previousAltitudeMeters == null || currentAltitudeMeters == null) {
        return null
    }
    return currentAltitudeMeters - previousAltitudeMeters
}
