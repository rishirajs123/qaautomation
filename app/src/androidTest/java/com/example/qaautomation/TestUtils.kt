package com.example.qaautomation

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Utility functions for testing
 */
object TestUtils {
    
    /**
     * Grant location permissions via adb
     */
    fun grantLocationPermissions() {
        try {
            val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            
            // Grant fine location
            device.executeShellCommand("pm grant $packageName ${Manifest.permission.ACCESS_FINE_LOCATION}")
            
            // Grant coarse location
            device.executeShellCommand("pm grant $packageName ${Manifest.permission.ACCESS_COARSE_LOCATION}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * Clicks twice anywhere on the screen
     * Useful for dismissing popups or dialogs that don't have specific buttons
     */
    fun clickTwiceAnywhere() {
        try {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            
            // Get screen dimensions
            val screenWidth = device.displayWidth
            val screenHeight = device.displayHeight
            
            // Click near the center of the screen, but not exactly at the center
            // to avoid hitting buttons that might be there
            // val x = screenWidth / 2
            // val y = screenHeight / 3 * 2 // Lower third of the screen
            val x = screenWidth - 10
            val y = 10 
            
            // Click twice with a small delay
            device.click(x, y)
            TimeUnit.MILLISECONDS.sleep(300)
            device.click(x, y)
            

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Wait for permission dialog and accept it - enhanced version
     * This checks for multiple possible permission dialog variations
     */
    fun acceptPermissionDialog() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        try {
            // Common text patterns for permission dialog buttons
            val allowButton = device.wait(Until.findObject(By.text("Allow")), 1000)
            if (allowButton != null && allowButton.isClickable()) {
                allowButton.click()
                TimeUnit.SECONDS.sleep(1)
                return
            }
            
            // Try "While using the app" option
            val whileUsingButton = device.wait(Until.findObject(By.text("While using the app")), 2000)
            if (whileUsingButton != null && whileUsingButton.isClickable()) {
                whileUsingButton.click()
                TimeUnit.SECONDS.sleep(1)
                return
            }
            
            // Check for a permission dialog using UiSelector (more reliable in some cases)
            val permissionDialog = device.findObject(UiSelector().packageName("com.android.permissioncontroller"))
            if (permissionDialog.exists()) {
                // Look for various allow buttons
                val allow = device.findObject(UiSelector().text("Allow"))
                val allowForeground = device.findObject(UiSelector().text("Allow only while using the app"))
                val allowThisTime = device.findObject(UiSelector().text("Allow this time"))
                
                if (allow.exists() && allow.isClickable()) {
                    allow.click()
                } else if (allowForeground.exists() && allowForeground.isClickable()) {
                    allowForeground.click()
                } else if (allowThisTime.exists() && allowThisTime.isClickable()) {
                    allowThisTime.click()
                }
                
                TimeUnit.SECONDS.sleep(1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Handle all permission dialogs that might appear on app startup
     * This should be called at the beginning of each test
     */
    fun handleInitialPermissions() {
        // First grant via ADB (more reliable but might not trigger UI dialogs)
        grantLocationPermissions()

        // Give the app a moment to process the permissions
        TimeUnit.SECONDS.sleep(1)
    }
    

} 