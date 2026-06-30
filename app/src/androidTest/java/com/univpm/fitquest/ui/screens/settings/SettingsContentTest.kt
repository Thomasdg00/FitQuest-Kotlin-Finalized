package com.univpm.fitquest.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.SettingsUiState
import org.junit.Rule
import org.junit.Test

class SettingsContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsShowsProfileGoalsAndPreferences() {
        composeRule.setContent {
            FitQuestTheme {
                SettingsContent(
                    uiState = SettingsUiState(),
                    onSaveProfile = { _, _, _, _, _, _ -> },
                    onSaveWeeklyGoals = { _ -> },
                    onLanguageSelected = {},
                    onThemeSelected = {},
                )
            }
        }

        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Name").assertIsDisplayed()
        composeRule.onNodeWithText("Surname").assertIsDisplayed()
        composeRule.onNodeWithText("Weekly goals").assertIsDisplayed()
        composeRule.onNodeWithText("Customize").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Current preferences").performScrollTo().assertIsDisplayed()
    }
}