package com.univpm.fitquest.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test

class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsGoalsStatsWeeklyProgressAndEmptyLastWorkout() {
        composeRule.setContent {
            FitQuestTheme {
                HomeContent(
                    uiState = HomeUiState(),
                    currentDate = "Today",
                )
            }
        }

        composeRule.onNodeWithText("FitQuest").assertIsDisplayed()
        composeRule.onNodeWithText("Weekly Goal Progress").assertIsDisplayed()
        composeRule.onNodeWithText("Goals").assertIsDisplayed()
        composeRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeRule.onNodeWithText("No workouts yet").assertIsDisplayed()
    }
}