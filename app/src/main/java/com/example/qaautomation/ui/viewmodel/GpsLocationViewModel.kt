package com.example.qaautomation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qaautomation.data.model.GpsLocation
import com.example.qaautomation.data.repository.GpsLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GpsLocationViewModel @Inject constructor(
    private val repository: GpsLocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    val currentLocation = repository.currentLocation
    val locations = repository.locations
    
    private var locationUpdatesJob: Flow<GpsLocation>? = null
    
    init {
        // Try to get location on init
        if (_uiState.value == UiState.Initial) {
            getCurrentLocation()
        }
    }
    
    fun checkLocationPermission() {
        if (_uiState.value is UiState.Error) {
            // Only retry if in error state
            getCurrentLocation()
        }
    }
    
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.getCurrentLocation()
                if (result.isSuccess) {
                    _uiState.value = UiState.Success(result.getOrNull()!!)
                } else {
                    val error = result.exceptionOrNull()
                    when (error) {
                        is SecurityException -> _uiState.value = UiState.Error("Location permission required")
                        is IllegalStateException -> _uiState.value = UiState.Error("GPS is disabled")
                        else -> _uiState.value = UiState.Error(error?.message ?: "Error getting location")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun startLocationUpdates() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                locationUpdatesJob = repository.startLocationUpdates()
                locationUpdatesJob?.collect { location ->
                    _uiState.value = UiState.Success(location)
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is SecurityException -> "Location permission required"
                    is IllegalStateException -> "GPS is disabled"
                    else -> e.message ?: "Error getting location updates"
                }
                _uiState.value = UiState.Error(message)
            }
        }
    }
    
    fun stopLocationUpdates() {
        repository.stopLocationUpdates()
    }
    
    fun clearLocations() {
        repository.clearLocations()
    }
    
    fun setError(message: String) {
        _uiState.value = UiState.Error(message)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
    
    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        data class Success(val location: GpsLocation) : UiState()
        data class Error(val message: String) : UiState()
    }
} 