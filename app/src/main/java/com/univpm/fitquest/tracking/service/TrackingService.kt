package com.univpm.fitquest.tracking.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.univpm.fitquest.FitQuestApplication
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.entity.WeatherSnapshotEntity
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.WeatherSnapshotDraft
import com.univpm.fitquest.tracking.calories.CalorieProfile
import com.univpm.fitquest.tracking.calories.MetCalorieCalculator
import com.univpm.fitquest.tracking.calories.Sex
import com.univpm.fitquest.ui.resources.getSportName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackingService : Service(), SensorEventListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private val appContainer by lazy {
        (application as FitQuestApplication).appContainer
    }
    private val workoutRepository by lazy {
        appContainer.workoutRepository
    }
    private val userSettingsRepository by lazy {
        appContainer.userSettingsRepository
    }
    private val weatherRepository by lazy {
        appContainer.weatherRepository
    }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private val routePoints = mutableListOf<InMemoryRoutePoint>()

    private var currentState = TrackingServiceState()
    private var weatherSnapshotDraft: WeatherSnapshotDraft? = null
    private var weatherRequestStarted = false
    private var sessionStartedAtMillis: Long? = null
    private var lastAcceptedLocation: Location? = null
    private var activeStartedAtElapsedRealtime: Long? = null
    private var accumulatedElapsedMillis: Long = 0L
    private var elapsedTicker: Job? = null
    private var calorieProfile: CalorieProfile = CalorieProfile()
    private var stepSensor: Sensor? = null
    private var lastStepCounterValue: Float? = null
    private var totalActiveSteps: Int = 0
    private var elevationGainMeters: Double = 0.0
    private var elevationLossMeters: Double = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::handleLocation)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(SensorManager::class.java)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking(intent)
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        stopSensorUpdates()
        elapsedTicker?.cancel()
        serviceJob.cancel()
        if (currentState.errorMessage == null) {
            TrackingServiceController.updateState(TrackingServiceState())
        }
        super.onDestroy()
    }

    private fun startTracking(intent: Intent) {
        if (!canStartForegroundTracking()) {
            stopWithError(getString(R.string.tracking_error_permissions_required))
            return
        }

        routePoints.clear()
        weatherSnapshotDraft = null
        weatherRequestStarted = false
        sessionStartedAtMillis = System.currentTimeMillis()
        lastAcceptedLocation = null
        accumulatedElapsedMillis = 0L
        activeStartedAtElapsedRealtime = SystemClock.elapsedRealtime()
        calorieProfile = CalorieProfile()
        resetSensorMetrics()

        val sport = Sport.fromRouteValue(intent.getStringExtra(EXTRA_SPORT))
        currentState = TrackingServiceState(
            lifecycleState = TrackingLifecycleState.Running,
            sport = sport,
            stepCounterAvailable = stepSensor != null && hasActivityRecognitionPermission() &&
                sport.supportsCadence(),
            weatherStatus = WeatherCaptureStatus.WaitingForLocation,
        )
        TrackingServiceController.updateState(currentState)
        // Android requires a foreground service and ongoing notification for long-running location work.
        startForeground(NOTIFICATION_ID, buildNotification(currentState))
        loadCalorieProfile()
        startElapsedTicker()
        startSensorUpdates()
        startLocationUpdates()
    }

    private fun pauseTracking() {
        if (currentState.lifecycleState != TrackingLifecycleState.Running) return

        activeStartedAtElapsedRealtime?.let { startedAt ->
            accumulatedElapsedMillis += SystemClock.elapsedRealtime() - startedAt
        }
        activeStartedAtElapsedRealtime = null
        stopLocationUpdates()
        stopSensorUpdates()
        currentState = currentState.copy(
            lifecycleState = TrackingLifecycleState.Paused,
            elapsedMillis = accumulatedElapsedMillis,
        )
        publishState(updateNotification = true)
    }

    private fun resumeTracking() {
        if (currentState.lifecycleState != TrackingLifecycleState.Paused) return
        if (!hasForegroundLocationPermission()) {
            stopWithError(getString(R.string.tracking_error_location_missing))
            return
        }

        activeStartedAtElapsedRealtime = SystemClock.elapsedRealtime()
        currentState = currentState.copy(
            lifecycleState = TrackingLifecycleState.Running,
            errorMessage = null,
        )
        publishState(updateNotification = true)
        startElapsedTicker()
        startSensorUpdates()
        startLocationUpdates()
    }

    private fun stopTracking() {
        stopLocationUpdates()
        stopSensorUpdates()
        elapsedTicker?.cancel()

        val sport = currentState.sport
        val startedAtMillis = sessionStartedAtMillis
        val endedAtMillis = System.currentTimeMillis()
        val durationMillis = currentElapsedMillis()
        val distanceMeters = currentState.distanceMeters
        val routeSnapshot = routePoints.toList()
        val caloriesKcal = currentState.estimatedCaloriesKcal
        val cadenceStepsPerMinute = currentState.averageCadenceStepsPerMinute
        val gainMeters = currentState.elevationGainMeters
        val lossMeters = currentState.elevationLossMeters
        val weatherSnapshot = weatherSnapshotDraft

        if (currentState.lifecycleState == TrackingLifecycleState.Idle ||
            currentState.lifecycleState == TrackingLifecycleState.Stopping
        ) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        currentState = currentState.copy(
            lifecycleState = TrackingLifecycleState.Stopping,
            elapsedMillis = durationMillis,
        )
        publishState(updateNotification = true)

        serviceScope.launch {
            val errorMessage = saveStoppedSession(
                sport = sport,
                startedAtMillis = startedAtMillis,
                endedAtMillis = endedAtMillis,
                durationMillis = durationMillis,
                distanceMeters = distanceMeters,
                routeSnapshot = routeSnapshot,
                caloriesKcal = caloriesKcal,
                cadenceStepsPerMinute = cadenceStepsPerMinute,
                elevationGainMeters = gainMeters,
                elevationLossMeters = lossMeters,
                weatherSnapshotDraft = weatherSnapshot,
            )
            if (errorMessage == null) {
                clearSession()
                currentState = TrackingServiceState()
                TrackingServiceController.updateState(currentState)
            } else {
                clearSession()
                currentState = TrackingServiceState(errorMessage = errorMessage)
                TrackingServiceController.updateState(currentState)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopWithError(message: String) {
        stopLocationUpdates()
        stopSensorUpdates()
        elapsedTicker?.cancel()
        clearSession()
        currentState = TrackingServiceState(errorMessage = message)
        TrackingServiceController.updateState(currentState)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun saveStoppedSession(
        sport: Sport?,
        startedAtMillis: Long?,
        endedAtMillis: Long,
        durationMillis: Long,
        distanceMeters: Double,
        routeSnapshot: List<InMemoryRoutePoint>,
        caloriesKcal: Double,
        cadenceStepsPerMinute: Int?,
        elevationGainMeters: Double,
        elevationLossMeters: Double,
        weatherSnapshotDraft: WeatherSnapshotDraft?,
    ): String? {
        if (sport == null || startedAtMillis == null) {
            return getString(R.string.tracking_error_missing_start_data)
        }
        if (durationMillis <= 0L) {
            return getString(R.string.tracking_error_too_short)
        }

        return runCatching {
            withContext(Dispatchers.IO) {
                val workout = buildTrackedWorkoutEntity(
                    sport = sport,
                    startedAtMillis = startedAtMillis,
                    endedAtMillis = endedAtMillis,
                    durationMillis = durationMillis,
                    distanceMeters = distanceMeters,
                    caloriesKcal = caloriesKcal,
                    elevationGainMeters = elevationGainMeters,
                    elevationLossMeters = elevationLossMeters,
                    averageCadenceStepsPerMinute = cadenceStepsPerMinute,
                )
                val workoutId = workoutRepository.addWorkout(workout)
                val routeEntities = routeSnapshot.toRoutePointEntities(workoutId)
                if (routeEntities.isNotEmpty()) {
                    workoutRepository.addRoutePoints(routeEntities)
                }
                weatherSnapshotDraft?.let { weather ->
                    workoutRepository.saveWeather(weather.toEntity(workoutId))
                }
            }
        }.exceptionOrNull()?.let { throwable ->
            getString(
                R.string.tracking_error_save_failed,
                throwable.message ?: throwable::class.java.simpleName,
            )
        }
    }

    private fun clearSession() {
        routePoints.clear()
        weatherSnapshotDraft = null
        weatherRequestStarted = false
        sessionStartedAtMillis = null
        lastAcceptedLocation = null
        activeStartedAtElapsedRealtime = null
        accumulatedElapsedMillis = 0L
        resetSensorMetrics()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasForegroundLocationPermission()) {
            stopWithError(getString(R.string.tracking_error_location_missing))
            return
        }

        fetchInitialLocation()

        fusedLocationClient.requestLocationUpdates(
            trackingLocationRequest(),
            locationCallback,
            Looper.getMainLooper(),
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        val cts = CancellationTokenSource()

        serviceScope.launch {
            delay(5_000)
            cts.cancel()
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    handleLocation(location)
                } else {
                    fetchNetworkLocationFallback()
                }
            }
            .addOnFailureListener {
                fetchNetworkLocationFallback()
            }
            .addOnCanceledListener {
                fetchNetworkLocationFallback()
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchNetworkLocationFallback() {
        val cts = CancellationTokenSource()

        serviceScope.launch {
            delay(5_000)
            cts.cancel()
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    handleLocation(location)
                } else {
                    fallbackLastLocation()
                }
            }
            .addOnFailureListener {
                fallbackLastLocation()
            }
            .addOnCanceledListener {
                fallbackLastLocation()
            }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                handleLocation(location)
            }
        }
    }

    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun handleLocation(location: Location) {
        if (currentState.lifecycleState != TrackingLifecycleState.Running) return
        if (!isUsableLocation(location)) return

        val lastLocation = lastAcceptedLocation
        val distanceDelta = lastLocation?.distanceTo(location)?.toDouble() ?: 0.0
        val altitudeMeters = if (location.hasAltitude()) location.altitude else null
        val speed = when {
            location.hasSpeed() -> location.speed
            lastLocation != null -> calculatedSpeed(lastLocation, location, distanceDelta)
            else -> null
        }

        if (lastLocation != null && isImplausibleJump(lastLocation, location, distanceDelta)) return

        lastAcceptedLocation = location
        routePoints += InMemoryRoutePoint(
            latitude = location.latitude,
            longitude = location.longitude,
            recordedAtMillis = location.time,
            altitudeMeters = altitudeMeters,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            speedMetersPerSecond = speed,
        )

        if (!weatherRequestStarted) {
            startWeatherFetch(location.latitude, location.longitude)
        }

        val prevAlt = currentState.currentAltitudeMeters
        if (altitudeMeters != null && prevAlt != null) {
            val delta = altitudeMeters - prevAlt
            if (kotlin.math.abs(delta) >= MIN_ALTITUDE_DELTA_METERS) {
                if (delta > 0.0) elevationGainMeters += delta
                else elevationLossMeters += -delta
            }
        }

        val quality = determineLocationQuality(if (location.hasAccuracy()) location.accuracy else null)

        val updatedDistanceMeters = currentState.distanceMeters + distanceDelta
        val updatedElapsedMillis = currentElapsedMillis()
        currentState = currentState.copy(
            elapsedMillis = updatedElapsedMillis,
            distanceMeters = updatedDistanceMeters,
            latestLatitude = location.latitude,
            latestLongitude = location.longitude,
            currentSpeedMetersPerSecond = speed,
            currentAltitudeMeters = altitudeMeters,
            routePoints = routePoints.toList(),
            elevationGainMeters = elevationGainMeters,
            elevationLossMeters = elevationLossMeters,
            currentLocationQuality = quality,
            estimatedCaloriesKcal = estimateCalories(
                durationMillis = updatedElapsedMillis,
                distanceMeters = updatedDistanceMeters,
                elevationGainMeters = elevationGainMeters,
            ),
            errorMessage = null,
        )
        publishState(updateNotification = false)
    }

    private fun isUsableLocation(location: Location): Boolean {
        return isUsableLocationSample(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
            isInitialFix = lastAcceptedLocation == null,
        )
    }

    private fun isImplausibleJump(
        previous: Location,
        current: Location,
        distanceDelta: Double,
    ): Boolean {
        val elapsedSeconds = (current.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000.0
        if (elapsedSeconds <= 0.0) return false
        return distanceDelta / elapsedSeconds > MAX_REASONABLE_SPEED_METERS_PER_SECOND
    }

    private fun calculatedSpeed(
        previous: Location,
        current: Location,
        distanceDelta: Double,
    ): Float? {
        val elapsedSeconds = (current.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000.0
        if (elapsedSeconds <= 0.0) return null
        return (distanceDelta / elapsedSeconds).toFloat()
    }

    private fun loadCalorieProfile() {
        serviceScope.launch {
            calorieProfile = runCatching {
                withContext(Dispatchers.IO) {
                    val settings = userSettingsRepository.getSettings()
                    CalorieProfile(
                        weightKg = settings.bodyWeightKg,
                        heightCm = settings.heightCm,
                        ageYears = settings.ageYears,
                        sex = Sex.fromStorageValue(settings.sex),
                    )
                }
            }.getOrDefault(CalorieProfile())
        }
    }

    private fun startWeatherFetch(latitude: Double, longitude: Double) {
        weatherRequestStarted = true
        currentState = currentState.copy(weatherStatus = WeatherCaptureStatus.Loading)
        publishState(updateNotification = false)

        serviceScope.launch {
            val weatherResult = runCatching {
                weatherRepository.fetchCurrentWeather(latitude, longitude)
            }
            weatherResult.onSuccess { weather ->
                weatherSnapshotDraft = weather
                currentState = currentState.copy(weatherStatus = WeatherCaptureStatus.Saved)
                publishState(updateNotification = false)
            }.onFailure {
                // Weather is useful context, not required data. Failed requests leave the workout saveable.
                currentState = currentState.copy(weatherStatus = WeatherCaptureStatus.Failed)
                publishState(updateNotification = false)
            }
        }
    }

    private fun startSensorUpdates() {
        lastStepCounterValue = null
        // Step Counter is optional hardware sensor, so tracking continues without it.
        if (hasActivityRecognitionPermission() && currentState.sport?.supportsCadence() == true) {
            stepSensor?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun stopSensorUpdates() {
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
        lastStepCounterValue = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (currentState.lifecycleState != TrackingLifecycleState.Running) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounter(event.values.firstOrNull() ?: return)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun handleStepCounter(stepCounterValue: Float) {
        val previous = lastStepCounterValue
        lastStepCounterValue = stepCounterValue
        if (previous == null) return

        val delta = (stepCounterValue - previous).toInt().coerceAtLeast(0)
        totalActiveSteps += delta
        currentState = currentState.copy(
            averageCadenceStepsPerMinute = averageCadenceStepsPerMinute(currentElapsedMillis()),
        )
        publishState(updateNotification = false)
    }



    private fun resetSensorMetrics() {
        lastStepCounterValue = null
        totalActiveSteps = 0
        elevationGainMeters = 0.0
        elevationLossMeters = 0.0
    }

    private fun estimateCalories(
        durationMillis: Long,
        distanceMeters: Double,
        elevationGainMeters: Double = currentState.elevationGainMeters,
    ): Double {
        val sport = currentState.sport ?: return 0.0
        return MetCalorieCalculator.estimateKcal(
            sport = sport,
            durationMillis = durationMillis,
            distanceMeters = distanceMeters,
            profile = calorieProfile,
            elevationGainMeters = elevationGainMeters,
        )
    }

    private fun averageCadenceStepsPerMinute(durationMillis: Long): Int? {
        if (stepSensor == null || durationMillis <= 0L || totalActiveSteps <= 0) return null
        val minutes = durationMillis / 60_000.0
        if (minutes <= 0.0) return null
        return (totalActiveSteps / minutes).toInt().coerceAtLeast(0)
    }

    private fun startElapsedTicker() {
        elapsedTicker?.cancel()
        elapsedTicker = serviceScope.launch {
            while (isActive && currentState.lifecycleState == TrackingLifecycleState.Running) {
                val elapsedMillis = currentElapsedMillis()
                currentState = currentState.copy(
                    elapsedMillis = elapsedMillis,
                    estimatedCaloriesKcal = estimateCalories(
                        durationMillis = elapsedMillis,
                        distanceMeters = currentState.distanceMeters,
                    ),
                    averageCadenceStepsPerMinute = averageCadenceStepsPerMinute(elapsedMillis),
                )
                publishState(updateNotification = false)
                delay(1_000)
            }
        }
    }

    private fun currentElapsedMillis(): Long {
        val activeMillis = activeStartedAtElapsedRealtime?.let { startedAt ->
            SystemClock.elapsedRealtime() - startedAt
        } ?: 0L
        return accumulatedElapsedMillis + activeMillis
    }

    private fun publishState(updateNotification: Boolean) {
        TrackingServiceController.updateState(currentState)
        if (updateNotification && currentState.lifecycleState != TrackingLifecycleState.Idle) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, buildNotification(currentState))
        }
    }

    private fun canStartForegroundTracking(): Boolean {
        val hasNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        return hasForegroundLocationPermission() && hasNotifications
    }

    private fun hasForegroundLocationPermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun trackingLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()
    }

    private fun buildNotification(state: TrackingServiceState) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(getString(R.string.notification_tracking_active))
            .setContentText(notificationText(state))
            .setOngoing(state.lifecycleState != TrackingLifecycleState.Idle)
            .setOnlyAlertOnce(true)
            .addAction(notificationAction(ACTION_PAUSE, getString(R.string.pause), REQUEST_PAUSE, android.R.drawable.ic_media_pause))
            .addAction(notificationAction(ACTION_RESUME, getString(R.string.resume), REQUEST_RESUME, android.R.drawable.ic_media_play))
            .addAction(notificationAction(ACTION_STOP, getString(R.string.stop), REQUEST_STOP, android.R.drawable.ic_menu_close_clear_cancel))
            .build()

    private fun notificationText(state: TrackingServiceState): String {
        val sportText = state.sport?.let(::getSportName) ?: getString(R.string.notification_workout)
        return when (state.lifecycleState) {
            TrackingLifecycleState.Running -> getString(R.string.notification_sport_active, sportText)
            TrackingLifecycleState.Paused -> getString(R.string.notification_sport_paused, sportText)
            TrackingLifecycleState.Stopping -> getString(R.string.notification_sport_saving, sportText)
            TrackingLifecycleState.Idle -> sportText
        }
    }

    private fun notificationAction(
        action: String,
        title: String,
        requestCode: Int,
        icon: Int,
    ): NotificationCompat.Action {
        val intent = Intent(this, TrackingService::class.java).setAction(action)
        val pendingIntent = PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_tracking),
            NotificationManager.IMPORTANCE_LOW,
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun Context.hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun Sport.supportsCadence(): Boolean {
        return this == Sport.Walking || this == Sport.Running
    }

    private fun WeatherSnapshotDraft.toEntity(workoutId: Long): WeatherSnapshotEntity {
        return WeatherSnapshotEntity(
            workoutId = workoutId,
            recordedAtMillis = recordedAtMillis,
            temperatureCelsius = temperatureCelsius,
            relativeHumidityPercent = relativeHumidityPercent,
            windSpeedKmh = windSpeedKmh,
            weatherCode = weatherCode,
        )
    }

    companion object {
        const val ACTION_START = "com.univpm.fitquest.tracking.action.START"
        const val ACTION_PAUSE = "com.univpm.fitquest.tracking.action.PAUSE"
        const val ACTION_RESUME = "com.univpm.fitquest.tracking.action.RESUME"
        const val ACTION_STOP = "com.univpm.fitquest.tracking.action.STOP"
        const val EXTRA_SPORT = "sport"

        private const val CHANNEL_ID = "tracking"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_PAUSE = 2001
        private const val REQUEST_RESUME = 2002
        private const val REQUEST_STOP = 2003
        private const val LOCATION_INTERVAL_MILLIS = 5_000L
        private const val LOCATION_FASTEST_INTERVAL_MILLIS = 2_000L
        private const val MIN_DISTANCE_METERS = 3f
        private const val MAX_REASONABLE_SPEED_METERS_PER_SECOND = 80.0
        private const val MIN_ALTITUDE_DELTA_METERS = 1.0
    }
}
