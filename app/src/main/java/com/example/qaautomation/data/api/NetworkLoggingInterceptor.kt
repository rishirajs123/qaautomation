package com.example.qaautomation.data.api

import com.example.qaautomation.data.model.NetworkLog
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLoggingInterceptor @Inject constructor() : Interceptor {
    
    // Callback that will be set by NetworkLogRepository
    var logCallback: ((NetworkLog) -> Unit)? = null
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        // Get request body if present
        val requestBody = request.body?.toString()

        // Execute request
        val response = chain.proceed(request)

        // Get response body
        val responseBodyString = response.body?.string() ?: ""
        
        // Create new response with the body
        val newResponse = response.newBuilder()
            .body(responseBodyString.toResponseBody(response.body?.contentType()))
            .build()

        // Calculate response time
        val responseTime = System.currentTimeMillis() - startTime

        // Create network log
        val log = NetworkLog(
            method = request.method,
            url = request.url.toString(),
            requestHeaders = request.headers.toMap(),
            requestBody = requestBody,
            responseTime = responseTime,
            statusCode = response.code,
            responseHeaders = response.headers.toMap(),
            responseBody = responseBodyString,
            timestamp = System.currentTimeMillis(),
            source = "App"
        )
        
        // Send log via callback if set
        logCallback?.invoke(log)

        return newResponse
    }
    
    private fun generateCurlCommand(request: okhttp3.Request, body: String? = null): String {
        val builder = StringBuilder("curl")
        
        // Add method
        builder.append(" -X ${request.method}")
        
        // Add headers
        request.headers.forEach { (name, value) ->
            builder.append(" -H '$name: $value'")
        }
        
        // Add body if present
        body?.let {
            builder.append(" -d '$it'")
        }
        
        // Add URL
        builder.append(" '${request.url}'")
        
        return builder.toString()
    }
    
    private fun Headers.toMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (i in 0 until size) {
            result[name(i)] = value(i)
        }
        return result
    }
} 