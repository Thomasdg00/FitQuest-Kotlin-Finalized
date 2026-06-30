package com.univpm.fitquest.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.GoalProgressUi
import com.univpm.fitquest.viewmodel.GoalsViewModel

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val targetInputs = remember { mutableStateMapOf<String, String>() }
    val inputErrors = remember { mutableStateMapOf<String, Boolean>() }

    ScreenScaffold(
        title = stringResource(R.string.goals_title),
        subtitle = stringResource(R.string.goals_subtitle),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            uiState.goals.forEach { goal ->
                val sportKey = goal.sport.routeValue
                LaunchedEffect(goal.sport, goal.targetKm) {
                    targetInputs[sportKey] = FormatUtils.formatDecimalKm(goal.targetKm)
                }
                GoalCard(
                    goal = goal,
                    inputValue = targetInputs[sportKey].orEmpty(),
                    isInputError = inputErrors[sportKey] == true,
                    onInputChange = {
                        targetInputs[sportKey] = it
                        inputErrors.remove(sportKey)
                    },
                    onSave = {
                        val input = targetInputs[sportKey].orEmpty()
                        val targetKm = parseGoalTargetKmInput(input)
                        when {
                            input.isBlank() -> inputErrors.remove(sportKey)
                            targetKm == null -> inputErrors[sportKey] = true
                            else -> {
                                inputErrors.remove(sportKey)
                                viewModel.saveGoal(goal.sport, targetKm)
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: GoalProgressUi,
    inputValue: String,
    isInputError: Boolean,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = goal.sport.localizedName(), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${FormatUtils.formatDecimalKm(goal.progressKm)} / ${FormatUtils.formatDecimalKm(goal.targetKm)} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    label = { Text(stringResource(R.string.target_km)) },
                    isError = isInputError,
                    supportingText = if (isInputError) {
                        { Text(stringResource(R.string.goal_target_error)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onSave) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

internal fun parseGoalTargetKmInput(input: String): Double? {
    val targetKm = input.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return targetKm.takeIf { it >= 0.0 && !it.isNaN() && !it.isInfinite() }
}
