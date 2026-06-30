package com.univpm.fitquest.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.remote.DailyForecast
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.resources.weatherCodeToLabelRes
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.history.WorkoutRouteMap
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.HomeUiState
import com.univpm.fitquest.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onOpenGoals: () -> Unit = {},
    onOpenStats: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    val currentDate = remember(locale) { FormatUtils.formatCurrentDate(locale) }

    HomeContent(
        uiState = uiState,
        currentDate = currentDate,
        modifier = modifier,
        onOpenGoals = onOpenGoals,
        onOpenStats = onOpenStats,
    )
}

@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    currentDate: String,
    modifier: Modifier = Modifier,
    onOpenGoals: () -> Unit = {},
    onOpenStats: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = currentDate.ifBlank { stringResource(R.string.today) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        WeatherWidget(
            forecast = uiState.weatherForecast,
            locationName = uiState.locationName,
        )

        ActivityCard(uiState = uiState)

        HomeQuickActions(
            onOpenGoals = onOpenGoals,
            onOpenStats = onOpenStats,
        )

        LastWorkoutCard(
            workout = uiState.lastWorkout,
            routePoints = uiState.lastWorkoutRoutePoints,
        )
    }
}

@Composable
private fun HomeQuickActions(
    onOpenGoals: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeQuickActionCard(
            title = stringResource(R.string.goals_title),
            subtitle = stringResource(R.string.goals_subtitle),
            icon = Icons.Outlined.Flag,
            onClick = onOpenGoals,
            modifier = Modifier.weight(1f),
        )
        HomeQuickActionCard(
            title = stringResource(R.string.home_statistics),
            subtitle = stringResource(R.string.stats_subtitle),
            icon = Icons.Outlined.BarChart,
            onClick = onOpenStats,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeatherWidget(
    forecast: List<DailyForecast>,
    locationName: String?,
    modifier: Modifier = Modifier,
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.surface,
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = locationName ?: stringResource(R.string.weather),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = forecast.firstOrNull()?.let { getWeatherIcon(it.weatherCode) }
                            ?: Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }

                if (forecast.isEmpty()) {
                    Text(
                        text = stringResource(R.string.weather_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    )
                } else {
                    forecast.take(3).forEachIndexed { index, day ->
                        WeatherForecastRow(
                            label = stringResource(forecastDayLabelRes(index)),
                            forecast = day,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherForecastRow(
    label: String,
    forecast: DailyForecast,
    modifier: Modifier = Modifier,
) {
    val condition = stringResource(weatherCodeToLabelRes(forecast.weatherCode))
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = condition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
        }
        Text(
            text = forecastTemperatureText(forecast),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LastWorkoutCard(
    workout: WorkoutEntity?,
    routePoints: List<RoutePointEntity>,
    modifier: Modifier = Modifier,
) {
    val cardShape = MaterialTheme.shapes.large
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.last_workout),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (workout == null) {
                Text(
                    text = stringResource(R.string.no_workouts_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LastWorkoutMetric(
                        label = stringResource(R.string.distance),
                        value = FormatUtils.formatDistance(workout.distanceMeters),
                        modifier = Modifier.weight(1f),
                    )
                    LastWorkoutMetric(
                        label = stringResource(R.string.duration),
                        value = FormatUtils.formatDuration(workout.durationMillis),
                        modifier = Modifier.weight(1f),
                    )
                    LastWorkoutMetric(
                        label = stringResource(R.string.calories),
                        value = FormatUtils.formatCalories(workout.caloriesKcal),
                        modifier = Modifier.weight(1f),
                    )
                }
                WorkoutRouteMap(
                    routePoints = routePoints,
                    mapHeight = 150.dp,
                    showContainer = false,
                    shape = cardShape,
                )
            }
        }
    }
}

@Composable
private fun LastWorkoutMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivityCard(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.weekly_goal_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            WeeklySportProgressCards(
                weeklyDistances = uiState.weeklyDistances,
                weeklyGoals = uiState.weeklyGoals,
            )
        }
    }
}

@Composable
private fun WeeklySportProgressCards(
    weeklyDistances: Map<Sport, Double>,
    weeklyGoals: Map<Sport, Double>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Sport.entries.forEach { sport ->
            val distance = weeklyDistances[sport] ?: 0.0
            val goal = weeklyGoals[sport] ?: 1.0
            val progressFraction = if (goal <= 0.0) 0f else (distance / goal).toFloat().coerceIn(0f, 1f)

            SportProgressCard(
                sport = sport,
                distance = distance,
                goal = goal,
                progressFraction = progressFraction,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SportProgressCard(
    sport: Sport,
    distance: Double,
    goal: Double,
    progressFraction: Float,
    modifier: Modifier = Modifier,
) {
    val sportColor = when (sport) {
        Sport.Walking -> MaterialTheme.colorScheme.primary
        Sport.Running -> MaterialTheme.colorScheme.secondary
        Sport.Cycling -> MaterialTheme.colorScheme.tertiary
    }
    val sportName = sport.localizedName()

    val sportIcon = when (sport) {
        Sport.Walking -> Icons.AutoMirrored.Outlined.DirectionsWalk
        Sport.Running -> Icons.AutoMirrored.Outlined.DirectionsRun
        Sport.Cycling -> Icons.AutoMirrored.Outlined.DirectionsBike
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = sportIcon,
                contentDescription = sportName,
                tint = sportColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = sportName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = sportColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                strokeCap = StrokeCap.Round,
            )

            Text(
                text = stringResource(
                    R.string.weekly_distance_progress,
                    FormatUtils.formatDecimalKm(distance),
                    FormatUtils.formatDecimalKm(goal),
                ),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun getWeatherIconAndDesc(weatherCode: Int?): Pair<ImageVector, Int> {
    if (weatherCode == null) return Pair(Icons.Outlined.CloudOff, R.string.weather_cloudy)
    return when (weatherCode) {
        0 -> Pair(Icons.Outlined.WbSunny, R.string.weather_sunny)
        1, 2, 3 -> Pair(Icons.Outlined.Cloud, R.string.weather_partly_cloudy)
        45, 48 -> Pair(Icons.Outlined.Cloud, R.string.weather_foggy)
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Pair(Icons.Outlined.Umbrella, R.string.weather_rainy)
        71, 73, 75, 85, 86 -> Pair(Icons.Outlined.AcUnit, R.string.weather_snowy)
        95, 96, 99 -> Pair(Icons.Outlined.Thunderstorm, R.string.weather_stormy)
        else -> Pair(Icons.Outlined.Cloud, R.string.weather_cloudy)
    }
}

private fun getWeatherIcon(weatherCode: Int?): ImageVector = getWeatherIconAndDesc(weatherCode).first

private fun forecastDayLabelRes(index: Int): Int = when (index) {
    0 -> R.string.today
    1 -> R.string.tomorrow
    else -> R.string.day_after_tomorrow
}

private fun forecastTemperatureText(forecast: DailyForecast): String {
    val max = forecast.maxTemperatureCelsius
    val min = forecast.minTemperatureCelsius
    return when {
        max != null && min != null -> "${FormatUtils.formatOneDecimal(min)}° / ${FormatUtils.formatOneDecimal(max)}°"
        max != null -> "${FormatUtils.formatOneDecimal(max)}°"
        min != null -> "${FormatUtils.formatOneDecimal(min)}°"
        else -> "--"
    }
}
