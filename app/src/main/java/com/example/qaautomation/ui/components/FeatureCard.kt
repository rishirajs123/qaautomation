package com.example.qaautomation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    title: String,
    infoText: String,
    status: FeatureStatus,
    onRefresh: () -> Unit,
    onClick: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    showCopyButton: Boolean = false,
    lastChecked: Long? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    if (showCopyButton && onCopy != null) {
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy"
                            )
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    Icon(
                        imageVector = when (status) {
                            FeatureStatus.SUCCESS -> Icons.Default.CheckCircle
                            FeatureStatus.ERROR -> Icons.Default.Error
                            FeatureStatus.WARNING -> Icons.Default.Warning
                            FeatureStatus.INFO -> Icons.Default.Info
                        },
                        contentDescription = "Status",
                        tint = when (status) {
                            FeatureStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                            FeatureStatus.ERROR -> MaterialTheme.colorScheme.error
                            FeatureStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                            FeatureStatus.INFO -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (lastChecked != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last checked: ${formatTimestamp(lastChecked)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: FeatureStatus) {
    val color = when (status) {
        FeatureStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        FeatureStatus.ERROR -> MaterialTheme.colorScheme.error
        FeatureStatus.WARNING -> MaterialTheme.colorScheme.errorContainer
        FeatureStatus.INFO -> MaterialTheme.colorScheme.secondary
    }
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            color = color,
            shape = MaterialTheme.shapes.small
        ) { }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
} 