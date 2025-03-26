package com.example.qaautomation.data.model

import java.util.*

data class NetworkLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val method: String,
    val url: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val responseTime: Long,
    val statusCode: Int,
    val responseHeaders: Map<String, String>,
    val responseBody: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "App"  // App, Browser, or Service
) 