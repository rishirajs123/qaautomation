package com.example.qaautomation.ui.screens.ip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.data.model.IpGeolocation
import com.example.qaautomation.data.model.VendorResponse
import com.example.qaautomation.ui.viewmodel.IpGeolocationUiState
import com.example.qaautomation.ui.viewmodel.IpGeolocationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpGeolocationScreen(
    onBackClick: () -> Unit,
    viewModel: IpGeolocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showVendorDetails by viewModel.showVendorDetails.collectAsState()
    val selectedVendorForDetails by viewModel.selectedVendorForDetails.collectAsState()
    val vendorResponses by viewModel.vendorResponses.collectAsState()
    val selectedVendor by viewModel.selectedVendor.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP Geolocation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshIpGeolocation() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    if (vendorResponses.isNotEmpty()) {
                        IconButton(onClick = { viewModel.showVendorDetails(null) }) {
                            Icon(Icons.Default.List, contentDescription = "View All Vendors")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (uiState) {
                is IpGeolocationUiState.Initial -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onRefresh = { viewModel.refreshIpGeolocation() }
                    )
                }
                is IpGeolocationUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is IpGeolocationUiState.Success -> {
                    val data = (uiState as IpGeolocationUiState.Success).ipGeolocation
                    IpGeolocationContent(
                        data = data,
                        vendorResponses = vendorResponses,
                        selectedVendor = selectedVendor,
                        onShowVendorDetails = { vendor -> viewModel.showVendorDetails(vendor) }
                    )
                }
                is IpGeolocationUiState.Error -> {
                    val message = (uiState as IpGeolocationUiState.Error).message
                    ErrorState(
                        message = message,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.refreshIpGeolocation() }
                    )
                }
            }
        }
    }
    
    // Show vendor details dialog
    if (showVendorDetails) {
        VendorDetailsDialog(
            vendorForDetails = selectedVendorForDetails,
            allVendors = vendorResponses,
            onDismiss = { viewModel.hideVendorDetails() },
            onVendorSelect = { vendor -> viewModel.showVendorDetails(vendor) },
            getFormattedTimestamp = { viewModel.getFormattedTimestamp(it) }
        )
    }
}

@Composable
fun IpGeolocationContent(
    data: IpGeolocation,
    vendorResponses: List<VendorResponse>,
    selectedVendor: VendorResponse?,
    onShowVendorDetails: (VendorResponse?) -> Unit
) {
    val context = LocalContext.current
    val formattedDate = remember(data.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(data.timestamp))
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // IP information with vendor and timestamp
        InfoItemWithCopy(
            title = "IP (from ${data.vendor} at $formattedDate)", 
            value = data.ip,
            onCopy = { 
                copyToClipboard(context, data.ip, "IPv4 address copied to clipboard")
            }
        )
        
        // Location with vendor and timestamp
        if (data.city != "Unknown" || data.region != "Unknown" || data.countryName != "Unknown") {
            val locationVendor = if (data.cityVendor.isNotEmpty()) data.cityVendor 
                else if (data.regionVendor.isNotEmpty()) data.regionVendor
                else if (data.countryVendor.isNotEmpty()) data.countryVendor
                else "Unknown"
                
            InfoItem(
                title = "Location (from $locationVendor at $formattedDate)", 
                value = "${data.city}, ${data.region}, ${data.countryName}"
            )
        }
        
        // Coordinates with vendor and timestamp
        if (data.latitude != 0.0 || data.longitude != 0.0) {
            InfoItem(
                title = "Coordinates (from ${data.coordinatesVendor} at $formattedDate)", 
                value = "Lat: ${data.latitude}, Long: ${data.longitude}"
            )
        }
        
        // ISP with vendor and timestamp
        if (data.isp != "Unknown") {
            InfoItem(
                title = "ISP (from ${data.ispVendor} at $formattedDate)", 
                value = data.isp
            )
        }
        
        // Organization with vendor and timestamp
        if (data.org != "Unknown") {
            InfoItem(
                title = "Organization (from ${data.orgVendor} at $formattedDate)", 
                value = data.org
            )
        }
        
        // Timezone with vendor and timestamp
        if (data.timezone != "Unknown") {
            InfoItem(
                title = "Timezone (from ${data.timezoneVendor} at $formattedDate)", 
                value = data.timezone
            )
        }
        
        // Country Code with vendor and timestamp
        if (data.countryCode != "Unknown") {
            InfoItem(
                title = "Country Code (from ${data.countryCodeVendor} at $formattedDate)", 
                value = data.countryCode
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // API Vendor Results
        if (vendorResponses.isNotEmpty()) {
            Text(
                text = "All Vendor Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            vendorResponses.forEach { vendor ->
                VendorResultCard(
                    vendor = vendor,
                    isSelected = vendor == selectedVendor,
                    onClick = { onShowVendorDetails(vendor) },
                    copyToClipboard = { context, text -> copyToClipboard(context, text, "IPv4 address copied to clipboard") }
                )
            }
        }
    }
}

@Composable
fun VendorResultCard(
    vendor: VendorResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    copyToClipboard: (Context, String) -> Unit
) {
    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val formattedDate = remember(vendor.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(vendor.timestamp))
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vendor.vendor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (vendor.successful && vendor.isIpv4) Color.Green else Color.Red,
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (vendor.successful) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "IP: ${vendor.ipAddress}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "IPv4: ${if (vendor.isIpv4) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status: ${vendor.status}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "Response: ${vendor.responseTimeMs}ms",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Text(
                    text = "API Call: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Failed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    if (vendor.status > 0) {
                        Text(
                            text = "Status: ${vendor.status}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Text(
                    text = "API Call: $formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Selected indicator
            if (isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        "Tap for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun VendorDetailsDialog(
    vendorForDetails: VendorResponse?,
    allVendors: List<VendorResponse>,
    onDismiss: () -> Unit,
    onVendorSelect: (VendorResponse?) -> Unit,
    getFormattedTimestamp: (Long) -> String
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Header
                Text(
                    text = if (vendorForDetails != null) {
                        "Vendor: ${vendorForDetails.vendor}"
                    } else {
                        "All Vendor Responses"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (vendorForDetails != null) {
                        // Show details for one vendor
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            DetailItem("Vendor", vendorForDetails.vendor)
                            DetailItem("IP Address", vendorForDetails.ipAddress)
                            DetailItem("Is IPv4", vendorForDetails.isIpv4.toString())
                            DetailItem("Response Time", "${vendorForDetails.responseTimeMs}ms")
                            DetailItem("Status Code", vendorForDetails.status.toString())
                            DetailItem("Timestamp", getFormattedTimestamp(vendorForDetails.timestamp))
                            DetailItem("Request URL", vendorForDetails.requestUrl)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "API Response:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = if (vendorForDetails.formattedResponse.isNotEmpty()) 
                                        vendorForDetails.formattedResponse
                                    else
                                        vendorForDetails.rawResponse,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(16.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        // Show list of all vendors to select
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            allVendors.forEach { vendor ->
                                @OptIn(ExperimentalMaterial3Api::class)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = { onVendorSelect(vendor) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = vendor.vendor,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "IP: ${vendor.ipAddress}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Text(
                                                text = "${vendor.responseTimeMs}ms",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Status: ${if (vendor.successful) "Success" else "Failed"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (vendor.successful) Color.Green else Color.Red
                                            )
                                            
                                            if (vendor.status > 0) {
                                                Text(
                                                    text = "Code: ${vendor.status}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (vendorForDetails != null) {
                        Button(
                            onClick = { onVendorSelect(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("View All Vendors")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun InfoItem(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun InfoItemWithCopy(title: String, value: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopy) {
                    Text(
                        text = "Copy",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// Helper function to copy text to clipboard
private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onRefresh: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No IP Geolocation data yet",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = onRefresh) {
            Text("Fetch IP Data")
        }
    }
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
} 