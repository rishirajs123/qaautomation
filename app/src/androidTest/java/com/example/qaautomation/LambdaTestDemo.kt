package com.example.qaautomation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.lambdatest.LTApp
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Demo test for LambdaTest screenshot functionality
 */
@RunWith(AndroidJUnit4::class)
class LambdaTestDemo {

    private val smartUIApp = LTApp()

    /**
     * Test: Demonstrate taking screenshots using LambdaTest
     */
    @Test
    fun takeScreenshots() {
        // Get package name for logging
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = appContext.packageName
        Log.i("LambdaTest", "Testing app: $packageName")

        // Take first screenshot
        val response1 = smartUIApp.screenshot("LT-Espresso-Test-1")
        Log.i("LambdaTest", "Screenshot 1 result: $response1")
        
        // Wait a moment
        TimeUnit.SECONDS.sleep(2)
        
        // Take second screenshot
        val response2 = smartUIApp.screenshot("LT-Espresso-Test-2")
        Log.i("LambdaTest", "Screenshot 2 result: $response2")
    }
} 