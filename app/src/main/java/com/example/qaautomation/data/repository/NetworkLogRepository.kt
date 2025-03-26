package com.example.qaautomation.data.repository

import com.example.qaautomation.data.api.NetworkLoggingInterceptor
import com.example.qaautomation.data.api.NetworkTestService
import com.example.qaautomation.data.model.NetworkLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLogRepository @Inject constructor(
    private val networkTestService: NetworkTestService,
    private val networkLoggingInterceptor: NetworkLoggingInterceptor
) {
    private val _logs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val logs: Flow<List<NetworkLog>> = _logs.asStateFlow()

    init {
        // Register for log callback
        networkLoggingInterceptor.logCallback = { log ->
            addLog(log)
        }
    }

    fun addLog(log: NetworkLog) {
        _logs.value = _logs.value + log
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun generateCurlCommand(request: Request, body: String? = null): String {
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

    fun createNetworkLog(
        request: Request,
        response: Response,
        requestBody: String?,
        responseBody: String?,
        responseTime: Long
    ): NetworkLog {
        val log = NetworkLog(
            method = request.method.uppercase(),
            url = request.url.toString(),
            requestHeaders = request.headers.toMap(),
            requestBody = requestBody,
            responseTime = responseTime,
            statusCode = response.code,
            responseHeaders = response.headers.toMap(),
            responseBody = responseBody,
            timestamp = System.currentTimeMillis(),
            source = "NetworkLogRepository"
        )
        return log
    }
} 