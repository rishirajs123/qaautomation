package com.example.qaautomation

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt-enabled tests.
 * This replaces the standard application with HiltTestApplication
 * and additionally grants permissions needed for tests.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
    
    override fun onStart() {
        // Grant permissions before running tests
        try {
            // Attempt to grant location permissions
            val instruction = "pm grant ${targetContext.packageName} android.permission.ACCESS_FINE_LOCATION"
            executeShellCommand(instruction)
            
            val instruction2 = "pm grant ${targetContext.packageName} android.permission.ACCESS_COARSE_LOCATION"
            executeShellCommand(instruction2)
            
            Log.i("HiltTestRunner", "Location permissions granted")
        } catch (e: Exception) {
            Log.e("HiltTestRunner", "Failed to grant permissions: ${e.message}")
        }
        
        super.onStart()
    }
    
    private fun executeShellCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            process.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            Log.e("HiltTestRunner", "Shell command failed: $command", e)
            ""
        }
    }
} 