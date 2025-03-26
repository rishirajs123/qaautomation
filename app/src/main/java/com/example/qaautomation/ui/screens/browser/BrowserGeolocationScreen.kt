package com.example.qaautomation.ui.screens.browser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.qaautomation.data.model.BrowserGeolocation
import com.example.qaautomation.ui.viewmodel.BrowserGeolocationUiState
import com.example.qaautomation.ui.viewmodel.BrowserGeolocationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserGeolocationScreen(
    navController: NavController,
    viewModel: BrowserGeolocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val mockEnabled by viewModel.mockEnabled.collectAsState()
    val jsCode by viewModel.jsCode.collectAsState()

    var showMockDialog by remember { mutableStateOf(false) }
    var showJsCodeDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browser Geolocation") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMockDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Set mock location")
                    }
                    IconButton(onClick = { showJsCodeDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Show JS code")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mock toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mock Browser Geolocation", 
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = mockEnabled,
                        onCheckedChange = { 
                            viewModel.toggleMockEnabled(it)
                        }
                    )
                }
                
                // Current mock location
                currentLocation?.let { location ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Mock Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Mocked") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Lat: ${location.latitude}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Lng: ${location.longitude}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            // Additional details
                            Text(
                                text = "Accuracy: ${location.accuracy ?: "N/A"} meters",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            location.altitude?.let {
                                Text(
                                    text = "Altitude: $it meters",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            location.altitudeAccuracy?.let {
                                Text(
                                    text = "Altitude Accuracy: $it meters",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            location.heading?.let {
                                Text(
                                    text = "Heading: $it degrees",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            location.speed?.let {
                                Text(
                                    text = "Speed: $it m/s",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Timestamp
                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            Text(
                                text = "Time: ${formatter.format(Date(location.timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                } ?: run {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No mock location set",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showMockDialog = true }
                            ) {
                                Text("Set Mock Location")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Location history
                Text(
                    text = "Location History",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (locations.isEmpty()) {
                    Text(
                        text = "No location history yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locations) { location ->
                            if (location.id != currentLocation?.id) {
                                BrowserLocationCard(location = location)
                            }
                        }
                    }
                }
                
                // Bottom buttons
                Button(
                    onClick = { viewModel.clearLocations() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Clear History")
                }
            }
            
            // Mock location dialog
            if (showMockDialog) {
                BrowserMockLocationDialog(
                    onDismiss = { showMockDialog = false },
                    onConfirm = { lat, lng, acc, alt, altAcc, head, spd ->
                        viewModel.setMockLocation(
                            latitude = lat,
                            longitude = lng,
                            accuracy = acc,
                            altitude = alt,
                            altitudeAccuracy = altAcc,
                            heading = head,
                            speed = spd
                        )
                        showMockDialog = false
                    }
                )
            }
            
            // JS Code dialog
            if (showJsCodeDialog) {
                AlertDialog(
                    onDismissRequest = { showJsCodeDialog = false },
                    title = { Text("JavaScript Code") },
                    text = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Copy this code to your browser console:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = jsCode,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showJsCodeDialog = false }
                        ) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BrowserLocationCard(location: BrowserGeolocation) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            // Additional details in a more compact format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Accuracy: ${location.accuracy ?: "N/A"}m",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (location.isMocked) {
                    Text(
                        text = "Mocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Timestamp
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            Text(
                text = "Time: ${formatter.format(Date(location.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMockLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (lat: Double, lng: Double, acc: Double?, alt: Double?, altAcc: Double?, head: Double?, spd: Double?) -> Unit
) {
    var latitude by remember { mutableStateOf("37.4220") }
    var longitude by remember { mutableStateOf("-122.0841") }
    var accuracy by remember { mutableStateOf("10") }
    var altitude by remember { mutableStateOf("") }
    var altitudeAccuracy by remember { mutableStateOf("") }
    var heading by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Browser Mock Location") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = accuracy,
                    onValueChange = { accuracy = it },
                    label = { Text("Accuracy (meters)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = altitude,
                    onValueChange = { altitude = it },
                    label = { Text("Altitude (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = altitudeAccuracy,
                    onValueChange = { altitudeAccuracy = it },
                    label = { Text("Altitude Accuracy (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = heading,
                    onValueChange = { heading = it },
                    label = { Text("Heading (degrees, optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                TextField(
                    value = speed,
                    onValueChange = { speed = it },
                    label = { Text("Speed (m/s, optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        onConfirm(
                            latitude.toDouble(),
                            longitude.toDouble(),
                            accuracy.takeIf { it.isNotEmpty() }?.toDouble(),
                            altitude.takeIf { it.isNotEmpty() }?.toDouble(),
                            altitudeAccuracy.takeIf { it.isNotEmpty() }?.toDouble(),
                            heading.takeIf { it.isNotEmpty() }?.toDouble(),
                            speed.takeIf { it.isNotEmpty() }?.toDouble()
                        )
                    } catch (e: Exception) {
                        // Handle parsing errors
                    }
                }
            ) {
                Text("Set Location")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 