package com.univpm.fitquest.tracking.calories

import com.univpm.fitquest.domain.model.Sport

enum class Sex(
    val storageValue: String,
    internal val mifflinStJeorConstant: Double,
) {
    Female("female", -161.0),
    Male("male", 5.0);

    companion object {
        fun fromStorageValue(value: String?): Sex? {
            return entries.firstOrNull { it.storageValue == value }
        }
    }
}

data class CalorieProfile(
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val ageYears: Int? = null,
    val sex: Sex? = null,
)

object MetCalorieCalculator {
    fun estimateKcal(
        sport: Sport,
        durationMillis: Long,
        distanceMeters: Double,
        profile: CalorieProfile?,
        elevationGainMeters: Double = 0.0,
    ): Double {
        if (durationMillis <= 0L) return 0.0

        val resolvedProfile = resolveProfile(profile)
        val durationHours = durationMillis / 3_600_000.0
        val averageSpeedMetersPerSecond = distanceMeters.coerceAtLeast(0.0) / (durationMillis / 1_000.0)
        val met = metFor(sport, averageSpeedMetersPerSecond)
        val restingKcalPerHour = mifflinStJeorRestingKcalPerDay(resolvedProfile) / 24.0
        val climbKcal = 0.9 * resolvedProfile.weightKg * elevationGainMeters.coerceAtLeast(0.0) / 1_000.0
        return (met * restingKcalPerHour * durationHours + climbKcal).coerceAtLeast(0.0)
    }

    /**
     * Simple classroom-friendly MET bands based on common Compendium-style values:
     * easy walking/riding, brisk/moderate, and faster effort.
     */
    fun metFor(sport: Sport, averageSpeedMetersPerSecond: Double): Double {
        if (averageSpeedMetersPerSecond < 0.3) return 1.0

        return when (sport) {
            Sport.Walking -> when {
                averageSpeedMetersPerSecond < 1.4 -> 3.0
                averageSpeedMetersPerSecond < 1.8 -> 3.8
                else -> 5.0
            }
            Sport.Running -> when {
                averageSpeedMetersPerSecond < 2.5 -> 7.0
                averageSpeedMetersPerSecond < 3.5 -> 9.8
                else -> 11.5
            }
            Sport.Cycling -> when {
                averageSpeedMetersPerSecond < 4.5 -> 4.0
                averageSpeedMetersPerSecond < 6.7 -> 6.8
                else -> 8.0
            }
        }
    }

    private fun resolveProfile(profile: CalorieProfile?): ResolvedCalorieProfile {
        return ResolvedCalorieProfile(
            weightKg = profile?.weightKg?.takeIf { it > 0.0 } ?: DEFAULT_WEIGHT_KG,
            heightCm = profile?.heightCm?.takeIf { it > 0 } ?: DEFAULT_HEIGHT_CM,
            ageYears = profile?.ageYears?.takeIf { it > 0 } ?: DEFAULT_AGE_YEARS,
            // Female is the explicit unknown-sex fallback to avoid overestimating calories.
            sex = profile?.sex ?: DEFAULT_SEX,
        )
    }

    private fun mifflinStJeorRestingKcalPerDay(profile: ResolvedCalorieProfile): Double {
        return 10.0 * profile.weightKg +
            6.25 * profile.heightCm -
            5.0 * profile.ageYears +
            profile.sex.mifflinStJeorConstant
    }

    private data class ResolvedCalorieProfile(
        val weightKg: Double,
        val heightCm: Int,
        val ageYears: Int,
        val sex: Sex,
    )

    private const val DEFAULT_WEIGHT_KG = 70.0
    private const val DEFAULT_HEIGHT_CM = 170
    private const val DEFAULT_AGE_YEARS = 30
    private val DEFAULT_SEX = Sex.Female
}
