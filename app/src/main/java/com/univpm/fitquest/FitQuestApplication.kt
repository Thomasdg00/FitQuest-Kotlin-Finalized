package com.univpm.fitquest

import android.app.Application
import com.univpm.fitquest.di.AppContainer

class FitQuestApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)

        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                applicationContext,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) { renderer ->
                android.util.Log.d("FitQuestApp", "Maps initialized with renderer: ${renderer.name}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FitQuestApp", "Failed to initialize Google Maps", e)
        }
    }
}
