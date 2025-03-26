package com.example.qaautomation.data.repository

import com.example.qaautomation.data.model.BrowserGeolocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserGeolocationRepository @Inject constructor() {
    private val _locations = MutableStateFlow<List<BrowserGeolocation>>(emptyList())
    val locations: StateFlow<List<BrowserGeolocation>> = _locations.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<BrowserGeolocation?>(null)
    val currentLocation: StateFlow<BrowserGeolocation?> = _currentLocation.asStateFlow()
    
    private val _mockEnabled = MutableStateFlow(false)
    val mockEnabled: StateFlow<Boolean> = _mockEnabled.asStateFlow()
    
    fun setMockLocation(
        latitude: Double, 
        longitude: Double, 
        accuracy: Double? = 10.0,
        altitude: Double? = null,
        altitudeAccuracy: Double? = null,
        heading: Double? = null,
        speed: Double? = null
    ) {
        val location = BrowserGeolocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
            heading = heading,
            speed = speed,
            isMocked = true
        )
        _currentLocation.value = location
        addLocation(location)
    }
    
    fun toggleMockEnabled(enabled: Boolean) {
        _mockEnabled.value = enabled
    }
    
    fun addLocation(location: BrowserGeolocation) {
        _locations.value = _locations.value + location
    }
    
    fun clearLocations() {
        _locations.value = emptyList()
    }
    
    fun generateJsOverride(): String {
        val location = _currentLocation.value ?: return ""
        
        return """
            (function() {
              const mockGeolocation = {
                getCurrentPosition: function(success, error) {
                  success({
                    coords: {
                      latitude: ${location.latitude},
                      longitude: ${location.longitude},
                      accuracy: ${location.accuracy ?: 10.0},
                      ${location.altitude?.let { "altitude: $it," } ?: ""}
                      ${location.altitudeAccuracy?.let { "altitudeAccuracy: $it," } ?: ""}
                      ${location.heading?.let { "heading: $it," } ?: ""}
                      ${location.speed?.let { "speed: $it," } ?: ""}
                    },
                    timestamp: ${location.timestamp}
                  });
                },
                watchPosition: function(success, error) {
                  const id = setInterval(() => {
                    success({
                      coords: {
                        latitude: ${location.latitude},
                        longitude: ${location.longitude},
                        accuracy: ${location.accuracy ?: 10.0},
                        ${location.altitude?.let { "altitude: $it," } ?: ""}
                        ${location.altitudeAccuracy?.let { "altitudeAccuracy: $it," } ?: ""}
                        ${location.heading?.let { "heading: $it," } ?: ""}
                        ${location.speed?.let { "speed: $it," } ?: ""}
                      },
                      timestamp: Date.now()
                    });
                  }, 1000);
                  return id;
                },
                clearWatch: function(id) {
                  clearInterval(id);
                }
              };
              
              if (${_mockEnabled.value}) {
                navigator.geolocation = mockGeolocation;
              }
            })();
        """.trimIndent()
    }
} 