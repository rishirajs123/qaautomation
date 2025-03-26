package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qaautomation.data.model.NetworkSpeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NetworkSpeedUiState {
    object Initial : NetworkSpeedUiState()
    data class Loading(val progress: Float) : NetworkSpeedUiState()
    data class Success(val networkSpeed: NetworkSpeedData) : NetworkSpeedUiState()
    data class Error(val message: String) : NetworkSpeedUiState()
}

@HiltViewModel
class NetworkSpeedViewModel @Inject constructor() : ViewModel() {
    private val _networkSpeed = MutableStateFlow<Float>(0f)
    val networkSpeed: StateFlow<Float> = _networkSpeed.asStateFlow()
    
    private val _uiState = MutableStateFlow<NetworkSpeedUiState>(NetworkSpeedUiState.Initial)
    val uiState: StateFlow<NetworkSpeedUiState> = _uiState.asStateFlow()
    
    fun updateNetworkSpeed(bytesPerSecond: Long) {
        val mbps = bytesPerSecond / 125000f // Convert bytes/s to Mbps (1 Mbps = 125000 bytes/s)
        _networkSpeed.value = mbps
    }
    
    fun resetNetworkSpeed() {
        _networkSpeed.value = 0f
    }
    
    fun startSpeedTest() {
        viewModelScope.launch {
            _uiState.value = NetworkSpeedUiState.Loading(0f)
            try {
                // Simulate network speed test
                var progress = 0f
                while (progress < 1f) {
                    progress += 0.1f
                    _uiState.value = NetworkSpeedUiState.Loading(progress)
                    kotlinx.coroutines.delay(500)
                }
                
                // Simulate test results
                val result = NetworkSpeedData(
                    downloadSpeed = 50f,
                    uploadSpeed = 25f,
                    ping = 20,
                    jitter = 5f,
                    packetLoss = 0.1f
                )
                _uiState.value = NetworkSpeedUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = NetworkSpeedUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
} 