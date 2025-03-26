package com.example.qaautomation.ui.screens.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.data.model.NetworkLog
import com.example.qaautomation.ui.components.ErrorState
import com.example.qaautomation.ui.viewmodel.NetworkLogUiState
import com.example.qaautomation.ui.viewmodel.NetworkLogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogScreen(
    onBackClick: () -> Unit,
    viewModel: NetworkLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Logs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Logs")
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
            when (uiState) {
                is NetworkLogUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NetworkLogUiState.Success -> {
                    NetworkLogContent(logs = logs)
                }
                is NetworkLogUiState.Error -> {
                    val message = (uiState as NetworkLogUiState.Error).message
                    ErrorState(
                        message = message,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.testGetRequest("https://httpbin.org/get") }
                    )
                }
                is NetworkLogUiState.Initial -> {
                    if (logs.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        NetworkLogContent(logs = logs)
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkLogContent(logs: List<NetworkLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(logs) { log ->
            NetworkLogCard(log = log)
        }
    }
}

@Composable
fun NetworkLogCard(log: NetworkLog) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "${log.method} ${log.statusCode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            Text(
                text = log.url,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Text(
                text = "Response Time: ${log.responseTime}ms",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request Headers:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                log.requestHeaders.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Response Headers:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                log.responseHeaders.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (log.requestBody != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Request Body:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.requestBody,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (log.responseBody != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Response Body:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.responseBody,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No network logs available",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault())
    return formatter.format(Date(timestamp))
} 