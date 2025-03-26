package com.example.qaautomation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.R
import com.example.qaautomation.ui.components.FeatureCard
import com.example.qaautomation.ui.components.FeatureStatus
import com.example.qaautomation.ui.components.NetworkSpeedIndicator
import com.example.qaautomation.ui.viewmodel.*
import com.example.qaautomation.data.model.GpsLocation
import com.example.qaautomation.ui.viewmodel.GpsLocationViewModel.UiState
import com.example.qaautomation.util.RequestLocationPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onBrowserClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onIpGeolocationClick: () -> Unit,
    ipGeolocationViewModel: IpGeolocationViewModel = hiltViewModel(),
    networkLogViewModel: NetworkLogViewModel = hiltViewModel(),
    gpsLocationViewModel: GpsLocationViewModel = hiltViewModel(),
    browserViewModel: BrowserViewModel = hiltViewModel(),
    networkSpeedViewModel: NetworkSpeedViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val ipGeolocationState = ipGeolocationViewModel.uiState.collectAsState()
    val networkLogState = networkLogViewModel.uiState.collectAsState()
    val gpsLocationState = gpsLocationViewModel.uiState.collectAsState()
    val browserState = browserViewModel.uiState.collectAsState()
    
    val ipGeolocationStatus = when(ipGeolocationState.value) {
        is IpGeolocationUiState.Initial -> FeatureStatus.INFO
        is IpGeolocationUiState.Loading -> FeatureStatus.INFO
        is IpGeolocationUiState.Success -> FeatureStatus.SUCCESS
        is IpGeolocationUiState.Error -> FeatureStatus.ERROR
    }
    
    val networkLogStatus = when(networkLogState.value) {
        is NetworkLogUiState.Initial -> FeatureStatus.INFO
        is NetworkLogUiState.Loading -> FeatureStatus.INFO
        is NetworkLogUiState.Success -> FeatureStatus.SUCCESS
        is NetworkLogUiState.Error -> FeatureStatus.ERROR
    }
    
    val gpsLocationStatus = when(gpsLocationState.value) {
        is GpsLocationViewModel.UiState.Initial -> FeatureStatus.INFO
        is GpsLocationViewModel.UiState.Loading -> FeatureStatus.INFO
        is GpsLocationViewModel.UiState.Success -> FeatureStatus.SUCCESS
        is GpsLocationViewModel.UiState.Error -> FeatureStatus.ERROR
    }
    
    val browserStatus = when(browserState.value) {
        is BrowserUiState.Initial -> FeatureStatus.INFO
        is BrowserUiState.Loading -> FeatureStatus.INFO
        is BrowserUiState.Success -> FeatureStatus.SUCCESS
        is BrowserUiState.Error -> FeatureStatus.ERROR
    }
    
    // Permission request for location
    RequestLocationPermission(
        onPermissionGranted = {
            // Get location when permission is granted
            gpsLocationViewModel.getCurrentLocation()
        },
        onPermissionDenied = {
            // Show error state when permission is denied
            gpsLocationViewModel.setError("Location permission required")
        }
    )
    
    // Call APIs on launch
    LaunchedEffect(key1 = true) {
        // Initialize data only once on startup
        if (ipGeolocationViewModel.uiState.value is IpGeolocationUiState.Initial) {
            ipGeolocationViewModel.refreshIpGeolocation()
        }
        
        // Test network calls if needed
        if (networkLogViewModel.logs.value.isEmpty()) {
            networkLogViewModel.testGetRequest("https://httpbin.org/get")
        }
    }
    
    val networkSpeed by networkSpeedViewModel.networkSpeed.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("QA Automation") }
            )
        },
        bottomBar = {
            // Add bottom navigation bar for Network Logs and Browser
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = onNetworkClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "Network Logs"
                            )
                            Text(
                                "Network Logs",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onBrowserClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Web,
                                contentDescription = "Browser"
                            )
                            Text(
                                "Browser",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Add network speed indicator
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                item {
                    FeatureCard(
                        title = stringResource(R.string.feature_gps_geolocation),
                        infoText = when (val state = gpsLocationState.value) {
                            is GpsLocationViewModel.UiState.Initial -> "Waiting for location..."
                            is GpsLocationViewModel.UiState.Loading -> "Getting location..."
                            is GpsLocationViewModel.UiState.Success -> {
                                val gpsLocation = state.location
                                String.format(
                                    "Lat: %.6f, Long: %.6f",
                                    gpsLocation.latitude,
                                    gpsLocation.longitude
                                )
                            }
                            is GpsLocationViewModel.UiState.Error -> state.message
                        },
                        status = gpsLocationStatus,
                        onRefresh = { gpsLocationViewModel.checkLocationPermission() },
                        showCopyButton = gpsLocationState.value is GpsLocationViewModel.UiState.Success,
                        onCopy = {
                            val state = gpsLocationState.value
                            if (state is GpsLocationViewModel.UiState.Success) {
                                clipboardManager.setText(AnnotatedString(
                                    String.format(
                                        "%.6f, %.6f",
                                        state.location.latitude,
                                        state.location.longitude
                                    )
                                ))
                            }
                        },
                        lastChecked = if (gpsLocationState.value is GpsLocationViewModel.UiState.Success) {
                            (gpsLocationState.value as GpsLocationViewModel.UiState.Success).location.timestamp
                        } else null
                    )
                }
                
                item {
                    FeatureCard(
                        title = stringResource(R.string.feature_ip_geolocation),
                        infoText = when (val state = ipGeolocationState.value) {
                            is IpGeolocationUiState.Initial -> "Waiting for IP geolocation..."
                            is IpGeolocationUiState.Loading -> "Getting IP geolocation..."
                            is IpGeolocationUiState.Success -> {
                                val location = state.ipGeolocation
                                val ipInfo = "IP: ${location.ip} (${location.vendor})"
                                
                                val locationInfo = if (location.city != "Unknown" || location.region != "Unknown") {
                                    "\nLocation: ${location.city}, ${location.region} (${location.cityVendor})"
                                } else ""
                                
                                val coordsInfo = if (location.latitude != 0.0 || location.longitude != 0.0) {
                                    "\nCoordinates: ${location.latitude}, ${location.longitude} (${location.coordinatesVendor})"
                                } else ""
                                
                                ipInfo + locationInfo + coordsInfo
                            }
                            is IpGeolocationUiState.Error -> state.message
                        },
                        status = ipGeolocationStatus,
                        onRefresh = { ipGeolocationViewModel.refreshIpGeolocation() },
                        onClick = onIpGeolocationClick,
                        onCopy = if (ipGeolocationState.value is IpGeolocationUiState.Success) {
                            {
                                val location = (ipGeolocationState.value as IpGeolocationUiState.Success).ipGeolocation
                                clipboardManager.setText(AnnotatedString(location.ip))
                            }
                        } else null,
                        showCopyButton = ipGeolocationState.value is IpGeolocationUiState.Success,
                        lastChecked = if (ipGeolocationState.value is IpGeolocationUiState.Success) {
                            (ipGeolocationState.value as IpGeolocationUiState.Success).ipGeolocation.timestamp
                        } else null
                    )
                }
            }
            
            // Network speed indicator overlay
            if (networkSpeed > 0) {
                NetworkSpeedIndicator(
                    speed = networkSpeed,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 16.dp)
                )
            }
        }
    }
} 