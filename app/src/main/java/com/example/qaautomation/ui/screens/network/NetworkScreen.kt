package com.example.qaautomation.ui.screens.network

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.data.model.NetworkSpeedData
import com.example.qaautomation.ui.components.ErrorState
import com.example.qaautomation.ui.viewmodel.NetworkSpeedUiState
import com.example.qaautomation.ui.viewmodel.NetworkSpeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    onBackClick: () -> Unit,
    viewModel: NetworkSpeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Speed") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startSpeedTest() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                is NetworkSpeedUiState.Initial -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onStartTest = { viewModel.startSpeedTest() }
                    )
                }
                is NetworkSpeedUiState.Loading -> {
                    LoadingState(
                        progress = (uiState as NetworkSpeedUiState.Loading).progress,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is NetworkSpeedUiState.Success -> {
                    val data = (uiState as NetworkSpeedUiState.Success).networkSpeed
                    NetworkSpeedContent(data = data)
                }
                is NetworkSpeedUiState.Error -> {
                    val message = (uiState as NetworkSpeedUiState.Error).message
                    ErrorState(
                        message = message,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.startSpeedTest() }
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkSpeedContent(data: NetworkSpeedData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SpeedCard(
            title = "Download Speed",
            value = "${data.downloadSpeed} Mbps",
            unit = "Mbps"
        )
        SpeedCard(
            title = "Upload Speed",
            value = "${data.uploadSpeed} Mbps",
            unit = "Mbps"
        )
        SpeedCard(
            title = "Ping",
            value = "${data.ping} ms",
            unit = "ms"
        )
        SpeedCard(
            title = "Jitter",
            value = "${data.jitter} ms",
            unit = "ms"
        )
        SpeedCard(
            title = "Packet Loss",
            value = "${data.packetLoss}%",
            unit = "%"
        )
    }
}

@Composable
fun SpeedCard(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onStartTest: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No network speed data yet",
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = onStartTest) {
            Text("Start Speed Test")
        }
    }
}

@Composable
fun LoadingState(progress: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Testing network speed...",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 