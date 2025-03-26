package com.example.qaautomation.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import com.example.qaautomation.data.model.GpsLocation
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

@Singleton
class GpsLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private val _locations = MutableStateFlow<List<GpsLocation>>(emptyList())
    val locations: StateFlow<List<GpsLocation>> = _locations.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<GpsLocation?>(null)
    val currentLocation: StateFlow<GpsLocation?> = _currentLocation.asStateFlow()
    
    private val _mockEnabled = MutableStateFlow(false)
    val mockEnabled: StateFlow<Boolean> = _mockEnabled.asStateFlow()
    
    private var locationCallback: LocationCallback? = null
    
    init {
        checkMockLocationEnabled()
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<GpsLocation> {
        return try {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val gpsLocation = GpsLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = location.altitude,
                                accuracy = location.accuracy,
                                speed = location.speed,
                                bearing = location.bearing,
                                timestamp = location.time,
                                provider = location.provider ?: LocationManager.GPS_PROVIDER,
                                isMocked = location.isFromMockProvider
                            )
                            _currentLocation.value = gpsLocation
                            addLocation(gpsLocation)
                            continuation.resume(Result.success(gpsLocation), null)
                        } else {
                            continuation.resume(Result.failure(Exception("Unable to fetch geolocation: Location is null")), null)
                        }
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(Result.failure(e), null)
                    }

                continuation.invokeOnCancellation { throwable ->
                    // Handle cancellation if needed
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(): Flow<GpsLocation> = callbackFlow {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val gpsLocation = location.toGpsLocation()
                    _currentLocation.value = gpsLocation
                    addLocation(gpsLocation)
                    trySend(gpsLocation)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
        
        awaitClose {
            stopLocationUpdates()
        }
    }
    
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }
    
    fun setMockLocation(latitude: Double, longitude: Double, altitude: Double? = null, 
                        accuracy: Float = 10f, speed: Float? = null, bearing: Float? = null) {
        val gpsLocation = GpsLocation(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            provider = LocationManager.GPS_PROVIDER,
            isMocked = true
        )
        _currentLocation.value = gpsLocation
        addLocation(gpsLocation)
    }
    
    fun addLocation(location: GpsLocation) {
        _locations.value = _locations.value + location
    }
    
    fun clearLocations() {
        _locations.value = emptyList()
    }
    
    private fun checkMockLocationEnabled() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        _mockEnabled.value = try {
            val allowMock = android.provider.Settings.Secure.getInt(
                context.contentResolver,
                android.provider.Settings.Secure.ALLOW_MOCK_LOCATION, 0
            )
            allowMock != 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun Location.toGpsLocation(): GpsLocation {
        return GpsLocation(
            latitude = latitude,
            longitude = longitude,
            altitude = if (hasAltitude()) altitude else null,
            accuracy = accuracy,
            speed = if (hasSpeed()) speed else null,
            bearing = if (hasBearing()) bearing else null,
            provider = provider ?: LocationManager.GPS_PROVIDER,
            isMocked = isFromMockProvider
        )
    }
} 