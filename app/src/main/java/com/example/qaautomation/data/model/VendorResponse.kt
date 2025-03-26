package com.example.qaautomation.data.model

data class VendorResponse(
    val vendor: String,
    val ipAddress: String,
    val isIpv4: Boolean,
    val responseTimeMs: Long,
    val timestamp: Long,
    val rawResponse: String,
    val requestUrl: String,
    val successful: Boolean,
    val status: Int = 0, // HTTP Status code
    val formattedResponse: String = "" // Formatted JSON if valid
) 