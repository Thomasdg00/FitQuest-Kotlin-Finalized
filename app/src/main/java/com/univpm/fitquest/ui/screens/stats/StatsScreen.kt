package com.univpm.fitquest.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.components.BarChartItem
import com.univpm.fitquest.ui.components.SimpleBarChart
import com.univpm.fitquest.ui.components.SimpleProgressBar
import com.univpm.fitquest.ui.components.formatChartKm
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.PeriodStatsUi
import com.univpm.fitquest.viewmodel.StatsUiState
import com.univpm.fitquest.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenScaffold(
        title = stringResource(R.string.stats_title),
        subtitle = stringResource(R.string.stats_subtitle),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            if (!uiState.hasWorkouts) {
                EmptyStatsCard()
            }
            PeriodSummaryCard(title = stringResource(R.string.this_week), stats = uiState.weekly)
            PeriodSummaryCard(title = stringResource(R.string.this_month), stats = uiState.monthly)
            WeeklyDistanceChart(uiState = uiState)
            GoalProgressCard(uiState = uiState)
        }
    }
}

@Composable
private fun EmptyStatsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.stats_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun PeriodSummaryCard(
    title: String,
    stats: PeriodStatsUi,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = stringResource(R.string.distance),
                    value = FormatUtils.formatKm(stats.totalDistanceKm),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.duration),
                    value = "${stats.totalDurationMinutes} min",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    label = stringResource(R.string.workouts),
                    value = stats.workoutCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.calories),
                    value = FormatUtils.formatCalories(stats.totalCaloriesKcal),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyDistanceChart(uiState: StatsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.weekly_distance_by_sport), style = MaterialTheme.typography.titleMedium)
            SimpleBarChart(
                items = uiState.sportBreakdown.map {
                    BarChartItem(
                        label = it.sport.localizedName(),
                        value = it.weeklyDistanceKm,
                    )
                },
                valueLabel = ::formatChartKm,
            )
        }
    }
}

@Composable
private fun GoalProgressCard(uiState: StatsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.goal_completion), style = MaterialTheme.typography.titleMedium)
            uiState.goalProgress.forEach { goal ->
                SimpleProgressBar(
                    label = goal.sport.localizedName(),
                    progressFraction = goal.progressFraction,
                    valueText = "${FormatUtils.formatKm(goal.progressKm)} / ${FormatUtils.formatKm(goal.targetKm)}",
                )
            }
        }
    }
}
