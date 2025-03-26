package com.example.qaautomation.ui.screens.gps

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.data.model.GpsLocation
import com.example.qaautomation.ui.viewmodel.GpsLocationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsLocationScreen(
    onBackClick: () -> Unit,
    viewModel: GpsLocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    
    var showMockDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Location") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getCurrentLocation() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Get current location")
                    }
                    IconButton(onClick = { showMockDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Set mock location")
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
                // Current location card
                currentLocation?.let { location ->
                    LocationInfoCard(
                        location = location,
                        isCurrent = true
                    )
                    
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(locations) { location ->
                                LocationInfoCard(
                                    location = location,
                                    isCurrent = location.id == currentLocation?.id
                                )
                            }
                        }
                    }
                }
                
                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.startLocationUpdates() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text("Start Updates")
                    }
                    
                    Button(
                        onClick = { viewModel.stopLocationUpdates() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        Text("Stop Updates")
                    }
                    
                    Button(
                        onClick = { viewModel.clearLocations() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Clear")
                    }
                }
            }
            
            // Loading or error state
            when (uiState) {
                is GpsLocationViewModel.UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is GpsLocationViewModel.UiState.Error -> {
                    val message = (uiState as GpsLocationViewModel.UiState.Error).message
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Error") },
                        text = { Text(message) },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.getCurrentLocation() }
                            ) {
                                Text("Retry")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { 
                                    // Reset state
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    )
                }
                else -> { /* No-op for other states */ }
            }
            
            // Mock location dialog
            if (showMockDialog) {
                MockLocationDialog(
                    onDismiss = { showMockDialog = false },
                    onConfirm = { lat, lng, alt, acc, spd, brg ->
                        showMockDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInfoCard(
    location: GpsLocation,
    isCurrent: Boolean
) {
    val context = LocalContext.current
    val locationString = "Lat: ${location.latitude}, Long: ${location.longitude}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isCurrent) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isCurrent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        if (location.isMocked) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Mocked") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Location", locationString)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Location copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Copy")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = locationString,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (location.altitude != null) {
                Text(
                    text = "Altitude: ${location.altitude}m",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (location.accuracy != null) {
                Text(
                    text = "Accuracy: ±${location.accuracy}m",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (location.speed != null) {
                Text(
                    text = "Speed: ${location.speed}m/s",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (location.bearing != null) {
                Text(
                    text = "Bearing: ${location.bearing}°",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Add timestamp information
            if (location.timestamp > 0) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) 
                val formattedDate = dateFormat.format(Date(location.timestamp))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Last Updated: $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, Float?, Float?, Float?, Float?) -> Unit
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var altitude by remember { mutableStateOf("") }
    var accuracy by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    var bearing by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Mock Location") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = altitude,
                    onValueChange = { altitude = it },
                    label = { Text("Altitude (optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = accuracy,
                    onValueChange = { accuracy = it },
                    label = { Text("Accuracy (optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = speed,
                    onValueChange = { speed = it },
                    label = { Text("Speed (optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bearing,
                    onValueChange = { bearing = it },
                    label = { Text("Bearing (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latitude.toFloatOrNull() ?: 0f
                    val lng = longitude.toFloatOrNull() ?: 0f
                    val alt = altitude.toFloatOrNull()
                    val acc = accuracy.toFloatOrNull()
                    val spd = speed.toFloatOrNull()
                    val brg = bearing.toFloatOrNull()
                    onConfirm(lat, lng, alt, acc, spd, brg)
                }
            ) {
                Text("Set Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
} 