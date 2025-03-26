package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qaautomation.data.model.BrowserGeolocation
import com.example.qaautomation.data.repository.BrowserGeolocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserGeolocationViewModel @Inject constructor(
    private val repository: BrowserGeolocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowserGeolocationUiState>(BrowserGeolocationUiState.Initial)
    val uiState: StateFlow<BrowserGeolocationUiState> = _uiState.asStateFlow()
    
    val currentLocation = repository.currentLocation
    val locations = repository.locations
    val mockEnabled = repository.mockEnabled
    
    private val _jsCode = MutableStateFlow("")
    val jsCode: StateFlow<String> = _jsCode.asStateFlow()
    
    fun setMockLocation(
        latitude: Double, 
        longitude: Double, 
        accuracy: Double? = 10.0,
        altitude: Double? = null,
        altitudeAccuracy: Double? = null,
        heading: Double? = null,
        speed: Double? = null
    ) {
        repository.setMockLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
            heading = heading,
            speed = speed
        )
        _uiState.value = BrowserGeolocationUiState.Success(repository.currentLocation.value!!)
        generateJsCode()
    }
    
    fun toggleMockEnabled(enabled: Boolean) {
        repository.toggleMockEnabled(enabled)
        generateJsCode()
    }
    
    fun clearLocations() {
        repository.clearLocations()
        _uiState.value = BrowserGeolocationUiState.Initial
    }
    
    fun generateJsCode() {
        val jsOverride = repository.generateJsOverride()
        _jsCode.value = jsOverride
    }
}

sealed class BrowserGeolocationUiState {
    object Initial : BrowserGeolocationUiState()
    object Loading : BrowserGeolocationUiState()
    data class Success(val location: BrowserGeolocation) : BrowserGeolocationUiState()
    data class Error(val message: String) : BrowserGeolocationUiState()
} 