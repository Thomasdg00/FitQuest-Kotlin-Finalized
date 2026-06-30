package com.univpm.fitquest.di

import android.content.Context
import androidx.room.Room
import com.univpm.fitquest.data.local.database.FitQuestDatabase
import com.univpm.fitquest.data.remote.OpenMeteoClient
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.UserSettingsRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.tracking.location.FusedPreviewLocationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AppContainer(val context: Context) {
    private val database: FitQuestDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            FitQuestDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            workoutDao = database.workoutDao(),
            routePointDao = database.routePointDao(),
            weatherSnapshotDao = database.weatherSnapshotDao()
        )
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao())
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(database.userSettingsDao())
    }

    val openMeteoClient: OpenMeteoClient by lazy {
        OpenMeteoClient()
    }

    val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val previewLocationProvider: FusedPreviewLocationProvider by lazy {
        FusedPreviewLocationProvider(fusedLocationClient)
    }

    companion object {
        private const val DATABASE_NAME = "fitquest.db"
    }
}
