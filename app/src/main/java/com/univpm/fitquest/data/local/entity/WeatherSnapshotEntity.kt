package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Weather captured near workout start. Values follow OpenMeteo units.
 */
@Entity(
    tableName = "weather_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"], unique = true)
    ]
)
data class WeatherSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val recordedAtMillis: Long,
    val temperatureCelsius: Double? = null,
    val relativeHumidityPercent: Double? = null,
    val windSpeedKmh: Double? = null,
    val precipitationMm: Double? = null,
    val weatherCode: Int? = null
)
