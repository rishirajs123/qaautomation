package com.example.qaautomation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.qaautomation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Basic Espresso tests for the app
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BasicAppTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var device: UiDevice
    
    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Handle all permission dialogs that might appear on app startup
        TestUtils.handleInitialPermissions()
    }
    
    /**
     * Test 1: Verify all main sections are displayed.
     */
    @Test
    fun basicAppLaunchTest() {
        // Wait for UI to load completely
        TimeUnit.SECONDS.sleep(3)
        
        // Verify all main sections are displayed
        composeTestRule.onNodeWithText("GPS Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("IP Geolocation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network Logs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browser").assertIsDisplayed()
        
        // Wait an extra moment to ensure the app is fully loaded and stable
        TimeUnit.SECONDS.sleep(2)
    }
} 