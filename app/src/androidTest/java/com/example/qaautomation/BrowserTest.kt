package com.example.qaautomation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.qaautomation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.lambdatest.LTApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import android.util.Log

/**
 * Tests related to the browser functionality.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrowserTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var device: UiDevice
    private val smartUIApp = LTApp()
    
    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Handle all permission dialogs that might appear on app startup
        TestUtils.handleInitialPermissions()
    }
    
    /**
     * Test: Verify that the browser button exists and clicking it navigates to browser screen.
     */
    @Test
    fun testBrowserNavigation() {
        // Wait for UI to load - ensure we see the main screen
        TimeUnit.SECONDS.sleep(2)
        
        // Take screenshot of main screen and log the response
        val mainScreenResponse = smartUIApp.screenshot("Main-Screen")
        Log.i("LambdaTest", "Main Screen Screenshot Response: $mainScreenResponse")
        
        // Verify and click on Browser section
        composeTestRule.onNodeWithText("Browser")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        
        // Short wait for browser screen transition
        TimeUnit.SECONDS.sleep(2)
        
        // Verify browser URL field exists
        composeTestRule.onNodeWithTag("browser_url_field")
            .assertIsDisplayed()
            
        // Wait longer for browser content to fully load (ifconfig.me)
        TimeUnit.SECONDS.sleep(8)
        
        // Take screenshot of browser page and log the response
        val browserPageResponse = smartUIApp.screenshot("Browser-Page")
        Log.i("LambdaTest", "Browser Page Screenshot Response: $browserPageResponse")
        
        // Wait a bit to observe the loaded browser content
        TimeUnit.SECONDS.sleep(3)
        
        // Go back to main screen
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .performClick()
            
        // Brief wait to ensure we're back at main screen
        TimeUnit.SECONDS.sleep(2)
    }
}