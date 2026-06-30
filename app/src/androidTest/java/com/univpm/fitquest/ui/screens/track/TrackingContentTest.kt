package com.univpm.fitquest.ui.screens.track

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.theme.FitQuestTheme
import org.junit.Rule
import org.junit.Test

class TrackingContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun idleTrackingShowsSportChoicesAndPermissionStartArea() {
        composeRule.setContent {
            FitQuestTheme {
                IdleTrackingView(
                    activeSport = Sport.Walking,
                    canStartTracking = false,
                    permissionState = TrackingPermissionState(
                        foregroundLocationGranted = false,
                        notificationPermissionRequired = true,
                        notificationPermissionGranted = false,
                    ),
                    onSportSelected = {},
                    onGrantLocation = {},
                    onGrantNotifications = {},
                    onStartTracking = {},
                )
            }
        }

        composeRule.onNodeWithText("Walking").assertIsDisplayed()
        composeRule.onNodeWithText("Running").assertIsDisplayed()
        composeRule.onNodeWithText("Cycling").assertIsDisplayed()
        composeRule.onNodeWithText("Permissions Required to Track Activity").assertIsDisplayed()
        composeRule.onNodeWithText("Grant Location").assertIsDisplayed()
        composeRule.onNodeWithText("Grant Notify").assertIsDisplayed()
    }
}