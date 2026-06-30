package com.univpm.fitquest.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatUtils {
    fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis.coerceAtLeast(0L) / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationMinutes(durationMillis: Long): Long = durationMillis.coerceAtLeast(0L) / 60_000L

    fun formatDistance(distanceMeters: Double): String {
        val safeDistance = distanceMeters.coerceAtLeast(0.0)
        return if (safeDistance < 1_000.0) {
            "${String.format(Locale.US, "%.0f", safeDistance)} m"
        } else {
            "${String.format(Locale.US, "%.2f", safeDistance / 1_000.0)} km"
        }
    }

    fun formatDecimalKm(value: Double): String = String.format(Locale.US, "%.1f", value)

    fun formatOneDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)

    fun formatKm(value: Double): String = "${formatDecimalKm(value)} km"

    fun formatPace(speedMetersPerSecond: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond <= 0.1f) return "--"
        val totalSecondsPerKm = (1_000 / speedMetersPerSecond).toInt()
        val paceMinutes = totalSecondsPerKm / 60
        val paceSeconds = totalSecondsPerKm % 60
        return String.format(Locale.US, "%d:%02d /km", paceMinutes, paceSeconds)
    }

    fun formatSpeed(speedMetersPerSecond: Double): String {
        return "${String.format(Locale.US, "%.1f", speedMetersPerSecond.coerceAtLeast(0.0) * 3.6)} km/h"
    }

    fun formatSpeed(speedMetersPerSecond: Float): String = formatSpeed(speedMetersPerSecond.toDouble())

    fun formatCalories(value: Double): String {
        return "${String.format(Locale.US, "%.0f", value.coerceAtLeast(0.0))} kcal"
    }

    fun formatElevation(value: Double): String {
        return "${String.format(Locale.US, "%.0f", value.coerceAtLeast(0.0))} m"
    }

    fun formatWholeNumber(value: Double): String = String.format(Locale.US, "%.0f", value.coerceAtLeast(0.0))

    fun formatTemperatureCelsius(value: Double): String = "${String.format(Locale.US, "%.1f", value)} C"

    fun formatCurrentDate(locale: Locale): String {
        return runCatching {
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", locale))
        }.getOrDefault("")
    }
}
