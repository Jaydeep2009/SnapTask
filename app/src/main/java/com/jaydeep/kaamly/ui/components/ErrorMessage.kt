package com.jaydeep.kaamly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jaydeep.kaamly.ui.viewmodel.ErrorState

/**
 * Composable for displaying error messages with retry option
 */
@Composable
fun ErrorMessage(
    errorState: ErrorState,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getErrorIcon(errorState),
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getErrorTitle(errorState),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = errorState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onRetry != null && errorState.isRetryable()) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

/**
 * Snackbar-style error message
 */
@Composable
fun ErrorSnackbar(
    errorState: ErrorState,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        action = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onRetry != null && errorState.isRetryable()) {
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getErrorIcon(errorState),
                contentDescription = "Error",
                modifier = Modifier.size(20.dp)
            )
            Text(text = errorState.message)
        }
    }
}

/**
 * Get appropriate icon for error type
 */
private fun getErrorIcon(errorState: ErrorState): ImageVector {
    return when (errorState) {
        is ErrorState.NetworkError -> Icons.Default.WifiOff
        is ErrorState.AuthenticationError -> Icons.Default.Lock
        is ErrorState.PermissionError -> Icons.Default.Block
        is ErrorState.NotFoundError -> Icons.Default.SearchOff
        is ErrorState.ValidationError -> Icons.Default.Warning
        is ErrorState.GenericError -> Icons.Default.Error
    }
}

/**
 * Get appropriate title for error type
 */
private fun getErrorTitle(errorState: ErrorState): String {
    return when (errorState) {
        is ErrorState.NetworkError -> "Connection Error"
        is ErrorState.AuthenticationError -> "Authentication Error"
        is ErrorState.PermissionError -> "Permission Denied"
        is ErrorState.NotFoundError -> "Not Found"
        is ErrorState.ValidationError -> "Validation Error"
        is ErrorState.GenericError -> "Error"
    }
}
