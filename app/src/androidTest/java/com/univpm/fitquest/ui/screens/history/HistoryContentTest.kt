package com.univpm.fitquest.ui.screens.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.HistoryUiState
import org.junit.Rule
import org.junit.Test

class HistoryContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyShowsTitleSubtitleAndEmptyState() {
        composeRule.setContent {
            FitQuestTheme {
                HistoryContent(
                    uiState = HistoryUiState(),
                    onOpenWorkout = {},
                )
            }
        }

        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText("Saved workouts from this device.").assertIsDisplayed()
        composeRule.onNodeWithText("No saved workouts yet.").assertIsDisplayed()
    }
}