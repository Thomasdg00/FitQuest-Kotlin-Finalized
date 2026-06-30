package com.univpm.fitquest.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.AppLanguage
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode
import com.univpm.fitquest.tracking.calories.Sex
import com.univpm.fitquest.ui.resources.localizedName
import com.univpm.fitquest.ui.screens.common.ScreenScaffold
import com.univpm.fitquest.util.FormatUtils
import com.univpm.fitquest.viewmodel.SettingsUiState
import com.univpm.fitquest.viewmodel.SettingsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsContent(
        uiState = uiState,
        modifier = modifier,
        onSaveProfile = viewModel::saveProfile,
        onSaveWeeklyGoals = viewModel::saveWeeklyGoals,
        onLanguageSelected = { language -> viewModel.saveLanguageCode(language.code) },
        onThemeSelected = viewModel::saveThemeMode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    onSaveProfile: (String, String, Int, Double, Int, Sex?) -> Unit,
    onSaveWeeklyGoals: (Map<Sport, Double>) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var surnameInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var sexInput by remember { mutableStateOf<Sex?>(null) }
    var heightError by remember { mutableStateOf<SettingsInputError?>(null) }
    var weightError by remember { mutableStateOf<SettingsInputError?>(null) }
    var ageError by remember { mutableStateOf<SettingsInputError?>(null) }
    val weeklyGoalInputs = rememberWeeklyGoalInputs()
    val weeklyGoalErrors = remember { androidx.compose.runtime.mutableStateMapOf<String, SettingsInputError>() }

    LaunchedEffect(
        uiState.name,
        uiState.surname,
        uiState.heightCm,
        uiState.bodyWeightKg,
        uiState.ageYears,
        uiState.sex,
    ) {
        nameInput = uiState.name
        surnameInput = uiState.surname
        heightInput = if (uiState.heightCm > 0) uiState.heightCm.toString() else ""
        weightInput = String.format(Locale.US, "%.1f", uiState.bodyWeightKg)
        ageInput = if (uiState.ageYears > 0) uiState.ageYears.toString() else ""
        sexInput = uiState.sex
        heightError = null
        weightError = null
        ageError = null
    }

    LaunchedEffect(uiState.weeklyGoalKmBySport) {
        Sport.entries.forEach { sport ->
            weeklyGoalInputs[sport.routeValue] = FormatUtils.formatDecimalKm(
                uiState.weeklyGoalKmBySport[sport] ?: 0.0
            )
            weeklyGoalErrors.remove(sport.routeValue)
        }
    }

    ScreenScaffold(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            ProfileCard(
                nameInput = nameInput,
                onNameInputChange = { nameInput = it },
                surnameInput = surnameInput,
                onSurnameInputChange = { surnameInput = it },
                heightInput = heightInput,
                onHeightInputChange = {
                    heightInput = it
                    heightError = null
                },
                heightError = heightError,
                weightInput = weightInput,
                onWeightInputChange = {
                    weightInput = it
                    weightError = null
                },
                weightError = weightError,
                ageInput = ageInput,
                onAgeInputChange = {
                    ageInput = it
                    ageError = null
                },
                ageError = ageError,
                sexInput = sexInput,
                onSexInputChange = { sexInput = it },
                useMetricUnits = uiState.useMetricUnits,
                onSaveProfile = {
                    val height = parseHeightCmInput(heightInput, uiState.heightCm)
                    val weight = parseBodyWeightKgInput(weightInput)
                    val age = parseAgeYearsInput(ageInput, uiState.ageYears)
                    heightError = height.error
                    weightError = weight.error
                    ageError = age.error

                    if (height.error == null && weight.error == null && age.error == null) {
                        onSaveProfile(
                            nameInput,
                            surnameInput,
                            checkNotNull(height.value),
                            checkNotNull(weight.value),
                            checkNotNull(age.value),
                            sexInput,
                        )
                    }
                },
            )
            WeeklyGoalsCard(
                goalInputs = weeklyGoalInputs,
                goalErrors = weeklyGoalErrors,
                onGoalInputChange = { sport, value ->
                    weeklyGoalInputs[sport.routeValue] = value
                    weeklyGoalErrors.remove(sport.routeValue)
                },
                onSaveGoals = {
                    val parsedGoals = mutableMapOf<Sport, Double>()
                    weeklyGoalErrors.clear()

                    Sport.entries.forEach { sport ->
                        val result = parseWeeklyGoalKmInput(
                            input = weeklyGoalInputs[sport.routeValue].orEmpty(),
                            currentValue = uiState.weeklyGoalKmBySport[sport] ?: 0.0,
                        )
                        if (result.error != null) {
                            weeklyGoalErrors[sport.routeValue] = result.error
                        } else {
                            parsedGoals[sport] = checkNotNull(result.value)
                        }
                    }

                    if (weeklyGoalErrors.isEmpty()) {
                        onSaveWeeklyGoals(parsedGoals)
                    }
                },
            )
            PreferenceControlsCard(
                selectedLanguage = uiState.appLanguage,
                selectedTheme = uiState.themeMode,
                onLanguageSelected = onLanguageSelected,
                onThemeSelected = onThemeSelected,
            )
            CurrentPreferencesCard(
                language = stringResource(uiState.appLanguage.labelResId()),
                theme = stringResource(uiState.themeMode.labelResId()),
            )
        }
    }
}

@Composable
private fun rememberWeeklyGoalInputs(): SnapshotStateMap<String, String> {
    return remember { androidx.compose.runtime.mutableStateMapOf() }
}

@Composable
private fun ProfileCard(
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    surnameInput: String,
    onSurnameInputChange: (String) -> Unit,
    heightInput: String,
    onHeightInputChange: (String) -> Unit,
    heightError: SettingsInputError?,
    weightInput: String,
    onWeightInputChange: (String) -> Unit,
    weightError: SettingsInputError?,
    ageInput: String,
    onAgeInputChange: (String) -> Unit,
    ageError: SettingsInputError?,
    sexInput: Sex?,
    onSexInputChange: (Sex?) -> Unit,
    useMetricUnits: Boolean,
    onSaveProfile: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameInputChange,
                label = { Text(stringResource(R.string.profile_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = surnameInput,
                onValueChange = onSurnameInputChange,
                label = { Text(stringResource(R.string.profile_surname)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = heightInput,
                onValueChange = onHeightInputChange,
                label = { Text(stringResource(R.string.height_cm)) },
                isError = heightError != null,
                supportingText = heightError?.let { error ->
                    { Text(stringResource(error.messageResId())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = weightInput,
                onValueChange = onWeightInputChange,
                label = { Text(stringResource(R.string.weight_kg)) },
                isError = weightError != null,
                supportingText = weightError?.let { error ->
                    { Text(stringResource(error.messageResId())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ageInput,
                onValueChange = onAgeInputChange,
                label = { Text(stringResource(R.string.age_years)) },
                isError = ageError != null,
                supportingText = ageError?.let { error ->
                    { Text(stringResource(error.messageResId())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            SexDropdown(
                selectedSex = sexInput,
                onSexSelected = onSexInputChange,
            )
            Button(onClick = onSaveProfile) {
                Text(stringResource(R.string.save))
            }
            Text(
                text = if (useMetricUnits) {
                    stringResource(R.string.units_metric)
                } else {
                    stringResource(R.string.units_imperial)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyGoalsCard(
    goalInputs: Map<String, String>,
    goalErrors: Map<String, SettingsInputError>,
    onGoalInputChange: (Sport, String) -> Unit,
    onSaveGoals: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.weekly_goals),
                style = MaterialTheme.typography.titleMedium,
            )
            Sport.entries.forEach { sport ->
                val error = goalErrors[sport.routeValue]
                OutlinedTextField(
                    value = goalInputs[sport.routeValue].orEmpty(),
                    onValueChange = { onGoalInputChange(sport, it) },
                    label = { Text(sport.localizedName()) },
                    isError = error != null,
                    supportingText = {
                        Text(
                            stringResource(
                                error?.messageResId() ?: R.string.goal_km_per_week
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(onClick = onSaveGoals) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexDropdown(
    selectedSex: Sex?,
    onSexSelected: (Sex?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedSex?.label() ?: stringResource(R.string.sex_unspecified),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sex)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sex_unspecified)) },
                onClick = {
                    onSexSelected(null)
                    expanded = false
                },
            )
            Sex.entries.forEach { sex ->
                DropdownMenuItem(
                    text = { Text(sex.label()) },
                    onClick = {
                        onSexSelected(sex)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CurrentPreferencesCard(
    language: String,
    theme: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_current_preferences),
                style = MaterialTheme.typography.titleMedium,
            )
            PreferenceValueRow(
                label = stringResource(R.string.settings_current_language),
                value = language,
            )
            PreferenceValueRow(
                label = stringResource(R.string.settings_current_theme),
                value = theme,
            )
        }
    }
}

@Composable
private fun PreferenceValueRow(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferenceControlsCard(
    selectedLanguage: AppLanguage,
    selectedTheme: ThemeMode,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_customize),
                style = MaterialTheme.typography.titleMedium,
            )
            LanguageDropdown(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
            )
            ThemeSelector(
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = stringResource(selectedLanguage.labelResId()),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(language.labelResId())) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ThemeMode.entries.forEach { theme ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) }
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                )
                Text(
                    text = stringResource(theme.labelResId()),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

private fun AppLanguage.labelResId(): Int {
    return when (this) {
        AppLanguage.English -> R.string.language_english
        AppLanguage.Italian -> R.string.language_italian
    }
}

private fun ThemeMode.labelResId(): Int {
    return when (this) {
        ThemeMode.System -> R.string.theme_system
        ThemeMode.Light -> R.string.theme_light
        ThemeMode.Dark -> R.string.theme_dark
    }
}

@Composable
private fun Sex.label(): String {
    return when (this) {
        Sex.Female -> stringResource(R.string.sex_female)
        Sex.Male -> stringResource(R.string.sex_male)
    }
}

internal enum class SettingsInputError {
    Required,
    HeightCm,
    WeightKg,
    AgeYears,
    WeeklyGoalKm,
}

internal data class SettingsInputResult<T>(
    val value: T? = null,
    val error: SettingsInputError? = null,
)

internal fun parseHeightCmInput(
    input: String,
    currentValue: Int,
): SettingsInputResult<Int> {
    return parseOptionalWholeNumberInRange(
        input = input,
        currentValue = currentValue,
        minValue = 1,
        maxValue = 260,
        error = SettingsInputError.HeightCm,
    )
}

internal fun parseAgeYearsInput(
    input: String,
    currentValue: Int,
): SettingsInputResult<Int> {
    return parseOptionalWholeNumberInRange(
        input = input,
        currentValue = currentValue,
        minValue = 1,
        maxValue = 120,
        error = SettingsInputError.AgeYears,
    )
}

internal fun parseBodyWeightKgInput(input: String): SettingsInputResult<Double> {
    if (input.isBlank()) {
        return SettingsInputResult(error = SettingsInputError.Required)
    }

    val value = input.toFiniteDecimalOrNull()
    return if (value != null && value in 20.0..300.0) {
        SettingsInputResult(value)
    } else {
        SettingsInputResult(error = SettingsInputError.WeightKg)
    }
}

internal fun parseWeeklyGoalKmInput(
    input: String,
    currentValue: Double,
): SettingsInputResult<Double> {
    if (input.isBlank()) return SettingsInputResult(currentValue)

    val value = input.toFiniteDecimalOrNull()
    return if (value != null && value >= 0.0) {
        SettingsInputResult(value)
    } else {
        SettingsInputResult(error = SettingsInputError.WeeklyGoalKm)
    }
}

private fun parseOptionalWholeNumberInRange(
    input: String,
    currentValue: Int,
    minValue: Int,
    maxValue: Int,
    error: SettingsInputError,
): SettingsInputResult<Int> {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return SettingsInputResult(currentValue)

    val value = trimmed.toIntOrNull()
    return if (value != null && value in minValue..maxValue) {
        SettingsInputResult(value)
    } else {
        SettingsInputResult(error = error)
    }
}

private fun String.toFiniteDecimalOrNull(): Double? {
    val value = trim().replace(',', '.').toDoubleOrNull() ?: return null
    return value.takeIf { !it.isNaN() && !it.isInfinite() }
}

private fun SettingsInputError.messageResId(): Int {
    return when (this) {
        SettingsInputError.Required -> R.string.settings_error_required
        SettingsInputError.HeightCm -> R.string.settings_error_height_cm
        SettingsInputError.WeightKg -> R.string.settings_error_weight_kg
        SettingsInputError.AgeYears -> R.string.settings_error_age_years
        SettingsInputError.WeeklyGoalKm -> R.string.settings_error_goal_km
    }
}
