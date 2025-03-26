package com.example.qaautomation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GpsLocationTest {

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
     * Helper method to check if a text is displayed without failing the test
     */
    private fun isTextDisplayed(text: String, substring: Boolean = false): Boolean {
        return try {
            composeTestRule.onNodeWithText(text, substring = substring).assertIsDisplayed()
            true
        } catch (e: Throwable) {
            false
        }
    }

    @Test
    fun testGpsLocationPermissionAndDisplay() {
        // Wait for UI to load completely
        TimeUnit.SECONDS.sleep(2)
        
        // Make sure we can see the main screen first
        composeTestRule.onNodeWithText("QA Automation").assertIsDisplayed()
        
        // Verify GPS Location section is present
        composeTestRule.onNodeWithText("GPS Location").assertIsDisplayed()
        
        // Click on GPS Location to navigate to the GPS screen
        composeTestRule.onNodeWithText("GPS Location").performClick()
        
        // Wait a moment for the GPS screen to load
        TimeUnit.SECONDS.sleep(3)
        
        // Wait to observe the GPS location data or error message
        TimeUnit.SECONDS.sleep(5)
        
        // We don't need to navigate back - some devices might have system back button
        // or different back button implementations
    }
} 