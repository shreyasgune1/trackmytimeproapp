package com.dreamdevelopersone.trackmytimepro

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppTitleDisplayed() {
        composeTestRule.onNodeWithText("TrackMyTime Pro").assertIsDisplayed()
    }

    @Test
    fun testStartButtonDisabledInitially() {
        // Find text field and clear it to be sure
        composeTestRule.onNodeWithText("What are you working on?").assertIsDisplayed()
        
        // Start button should be disabled when text is empty
        composeTestRule.onNodeWithText("Start").assertIsNotEnabled()
    }

    @Test
    fun testStartButtonEnabledWhenTextEntered() {
        composeTestRule.onNodeWithText("What are you working on?").performTextInput("Coding")
        
        composeTestRule.onNodeWithText("Start").assertIsEnabled()
    }

    @Test
    fun testTabSwitching() {
        // Start on Timer tab
        composeTestRule.onNodeWithText("Current Session").assertIsDisplayed()
        
        // Click Analytics tab
        composeTestRule.onNodeWithText("Analytics").performClick()
        
        // Verify Analytics content
        composeTestRule.onNodeWithText("Total Productivity (Today)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Allocation").assertIsDisplayed()
        
        // Navigate back
        composeTestRule.onNodeWithText("Timer").performClick()
        composeTestRule.onNodeWithText("Current Session").assertIsDisplayed()
    }

    @Test
    fun testStartStopFlow() {
        val input = composeTestRule.onNodeWithText("What are you working on?")
        input.performTextInput("Test Task")
        
        composeTestRule.onNodeWithText("Start").performClick()
        
        // After start, Stop button should be enabled
        composeTestRule.onNodeWithText("Stop").assertIsEnabled()
        
        composeTestRule.onNodeWithText("Stop").performClick()
        
        // Should appear in recent list
        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    }
}
