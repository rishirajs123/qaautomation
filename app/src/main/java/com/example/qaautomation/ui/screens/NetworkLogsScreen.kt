package com.example.qaautomation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qaautomation.ui.viewmodel.NetworkLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogsScreen(
    onBackClick: () -> Unit,
    viewModel: NetworkLogViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Logs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Network logs UI will be implemented here
            Text("Network logs will be displayed here")
        }
    }
} 