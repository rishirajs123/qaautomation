package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qaautomation.data.model.NetworkLog
import com.example.qaautomation.data.repository.NetworkLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class NetworkLogViewModel @Inject constructor(
    private val repository: NetworkLogRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkLogUiState>(NetworkLogUiState.Initial)
    val uiState: StateFlow<NetworkLogUiState> = _uiState.asStateFlow()
    
    val logs = repository.logs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    private val _lastCallTimestamp = MutableStateFlow<Long?>(null)
    val lastCallTimestamp: StateFlow<Long?> = _lastCallTimestamp.asStateFlow()
    
    fun clearLogs() {
        repository.clearLogs()
    }
    
    fun addNetworkLog(networkLog: NetworkLog) {
        repository.addLog(networkLog)
        _lastCallTimestamp.value = System.currentTimeMillis()
    }
    
    fun testGetRequest(url: String) {
        viewModelScope.launch {
            _uiState.value = NetworkLogUiState.Loading
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    _uiState.value = NetworkLogUiState.Success
                } else {
                    _uiState.value = NetworkLogUiState.Error("Request failed with code: ${response.code}")
                }
            } catch (e: Exception) {
                _uiState.value = NetworkLogUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun testPostRequest(url: String, body: String) {
        viewModelScope.launch {
            _uiState.value = NetworkLogUiState.Loading
            try {
                // Simulate a test POST request
                val currentTime = System.currentTimeMillis()
                _lastCallTimestamp.value = currentTime
                
                // Create a sample network log entry
                val log = NetworkLog(
                    method = "POST",
                    url = "https://httpbin.org/post",
                    requestHeaders = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
                    requestBody = """{"test":"data"}""",
                    responseTime = 250, // simulated response time (ms)
                    statusCode = 200,
                    responseHeaders = mapOf("Content-Type" to "application/json"),
                    responseBody = """{"args":{},"data":"{\\"test\\":\\"data\\"}","headers":{"Accept":"application/json","Content-Type":"application/json","Host":"httpbin.org"},"json":{"test":"data"},"origin":"127.0.0.1","url":"https://httpbin.org/post"}""",
                    timestamp = currentTime
                )
                
                repository.addLog(log)
                _uiState.value = NetworkLogUiState.Success
            } catch (e: Exception) {
                _uiState.value = NetworkLogUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class NetworkLogUiState {
    object Initial : NetworkLogUiState()
    object Loading : NetworkLogUiState()
    object Success : NetworkLogUiState()
    data class Error(val message: String) : NetworkLogUiState()
} 