package com.univpm.fitquest.ui.screens.track

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import com.univpm.fitquest.tracking.service.WeatherCaptureStatus
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.TrackPanelUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun IdleTrackingView(
    activeSport: Sport,
    canStartTracking: Boolean,
    permissionState: TrackingPermissionState,
    onSportSelected: (Sport) -> Unit,
    onGrantLocation: () -> Unit,
    onGrantNotifications: () -> Unit,
    onStartTracking: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SportSelector(
            activeSport = activeSport,
            onSportSelected = onSportSelected,
        )

        if (canStartTracking) {
            Button(
                onClick = onStartTracking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeSport.accentColor(),
                ),
            ) {
                Text(
                    text = stringResource(R.string.track_start_activity),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        } else {
            PermissionRequiredPrompt(
                permissionState = permissionState,
                onGrantLocation = onGrantLocation,
                onGrantNotifications = onGrantNotifications,
            )
        }
    }
}

@Composable
internal fun ActiveTrackingView(
    panelState: TrackPanelUiState,
    fallbackSport: Sport,
    elapsedMillis: StateFlow<Long>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    val currentSport = panelState.sport ?: fallbackSport
    val sportAccentColor = currentSport.accentColor()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = currentSport.icon(),
                    contentDescription = null,
                    tint = sportAccentColor,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = currentSport.localizedName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            TimerText(
                elapsedMillis = elapsedMillis,
                color = sportAccentColor,
            )
        }

        TrackingStatsPanel(
            panelState = panelState,
            currentSport = currentSport,
        )

        panelState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TrackingControls(
            lifecycleState = panelState.lifecycleState,
            accentColor = sportAccentColor,
            onPause = onPause,
            onResume = onResume,
            onStop = onStop,
        )
    }
}

@Composable
internal fun PausedTrackingView(
    panelState: TrackPanelUiState,
    fallbackSport: Sport,
    elapsedMillis: StateFlow<Long>,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    ActiveTrackingView(
        panelState = panelState,
        fallbackSport = fallbackSport,
        elapsedMillis = elapsedMillis,
        onPause = {},
        onResume = onResume,
        onStop = onStop,
    )
}

@Composable
internal fun TimerText(
    elapsedMillis: StateFlow<Long>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val value by elapsedMillis.collectAsState()
    Text(
        text = FormatUtils.formatDuration(value),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
    )
}

@Composable
internal fun SportSelector(
    activeSport: Sport,
    onSportSelected: (Sport) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Sport.entries.forEach { sport ->
            val sportName = sport.localizedName()
            val isSelected = activeSport == sport
            val accentColor = sport.accentColor()
            OutlinedButton(
                onClick = { onSportSelected(sport) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
                    contentColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline,
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = sport.icon(),
                        contentDescription = sportName,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = sportName,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TrackingStatsPanel(
    panelState: TrackPanelUiState,
    currentSport: Sport,
) {
    val speedMps = panelState.currentSpeedMetersPerSecond
    val isCycling = currentSport == Sport.Cycling
    val speedOrPaceLabel = if (isCycling) stringResource(R.string.speed) else stringResource(R.string.pace)
    val speedOrPaceValue = if (isCycling) {
        speedMps?.let { FormatUtils.formatSpeed(it) } ?: "--"
    } else {
        FormatUtils.formatPace(speedMps)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricItem(
                label = stringResource(R.string.distance),
                value = FormatUtils.formatDistance(panelState.distanceMeters),
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                label = speedOrPaceLabel,
                value = speedOrPaceValue,
                modifier = Modifier.weight(1f),
            )
            MetricItem(
                label = stringResource(R.string.calories),
                value = FormatUtils.formatCalories(panelState.estimatedCaloriesKcal),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (panelState.stepCounterAvailable) {
                val cadence = panelState.averageCadenceStepsPerMinute
                MetricItem(
                    label = stringResource(R.string.cadence),
                    value = if (cadence != null) "$cadence spm" else "--",
                    modifier = Modifier.weight(1f),
                )
            }
            MetricItem(
                label = stringResource(R.string.live_elevation),
                value = panelState.currentAltitudeMeters?.let(FormatUtils::formatElevation)
                    ?: stringResource(R.string.elevation_unavailable),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun TrackingControls(
    lifecycleState: TrackingLifecycleState,
    accentColor: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (lifecycleState == TrackingLifecycleState.Running) {
            val pauseText = stringResource(R.string.pause)
            Button(
                onClick = onPause,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Icon(imageVector = Icons.Default.Pause, contentDescription = pauseText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(pauseText)
            }
        } else if (lifecycleState == TrackingLifecycleState.Paused) {
            val resumeText = stringResource(R.string.resume)
            Button(
                onClick = onResume,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = resumeText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(resumeText)
            }
        }

        if (lifecycleState != TrackingLifecycleState.Stopping) {
            val stopText = stringResource(R.string.stop)
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = stopText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stopText)
            }
        } else {
            Button(
                enabled = false,
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.track_saving_activity))
            }
        }
    }
}

@Composable
internal fun WeatherStatusPill(
    status: WeatherCaptureStatus,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = androidx.compose.foundation.shape.CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = when (status) {
                    WeatherCaptureStatus.Saved -> Icons.Outlined.Cloud
                    WeatherCaptureStatus.Failed -> Icons.Outlined.CloudOff
                    WeatherCaptureStatus.Loading -> Icons.Outlined.DeviceThermostat
                    else -> Icons.Outlined.WbSunny
                },
                contentDescription = stringResource(R.string.weather),
                tint = when (status) {
                    WeatherCaptureStatus.Saved -> MaterialTheme.colorScheme.primary
                    WeatherCaptureStatus.Failed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
            )
            when (status) {
                WeatherCaptureStatus.Loading,
                WeatherCaptureStatus.Saved,
                -> Unit
                else -> Text(
                    text = when (status) {
                        WeatherCaptureStatus.NotStarted -> stringResource(R.string.weather_ready)
                        WeatherCaptureStatus.WaitingForLocation -> stringResource(R.string.weather_waiting_gps)
                        WeatherCaptureStatus.Failed -> stringResource(R.string.weather_unavailable)
                        WeatherCaptureStatus.Loading,
                        WeatherCaptureStatus.Saved,
                        -> ""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Sport.accentColor(): Color {
    return when (this) {
        Sport.Walking -> MaterialTheme.colorScheme.primary
        Sport.Running -> MaterialTheme.colorScheme.secondary
        Sport.Cycling -> MaterialTheme.colorScheme.tertiary
    }
}

private fun Sport.icon(): ImageVector {
    return when (this) {
        Sport.Walking -> Icons.AutoMirrored.Outlined.DirectionsWalk
        Sport.Running -> Icons.AutoMirrored.Outlined.DirectionsRun
        Sport.Cycling -> Icons.AutoMirrored.Outlined.DirectionsBike
    }
}
