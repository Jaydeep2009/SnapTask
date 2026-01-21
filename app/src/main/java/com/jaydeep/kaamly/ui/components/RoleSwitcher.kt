package com.jaydeep.kaamly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jaydeep.kaamly.data.model.UserRole

/**
 * Role switcher component for switching between User and Worker roles
 */
@Composable
fun RoleSwitcherFAB(
    currentRole: UserRole,
    onRoleSwitch: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Only show FAB if user has BOTH role or can switch
    if (currentRole == UserRole.BOTH || currentRole == UserRole.USER || currentRole == UserRole.WORKER) {
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Switch Role"
            )
        }
    }
    
    if (showDialog) {
        RoleSwitchDialog(
            currentRole = currentRole,
            onDismiss = { showDialog = false },
            onRoleSelected = { newRole ->
                onRoleSwitch(newRole)
                showDialog = false
            }
        )
    }
}

/**
 * Role switch dialog
 */
@Composable
fun RoleSwitchDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Switch Role")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current role: ${getRoleDisplayName(currentRole)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Switch to:",
                    style = MaterialTheme.typography.labelLarge
                )
                
                // Show available roles
                if (currentRole == UserRole.USER || currentRole == UserRole.BOTH) {
                    RoleOption(
                        title = "Worker",
                        description = "Complete tasks and earn money",
                        icon = Icons.Default.Build,
                        onClick = { onRoleSelected(UserRole.WORKER) }
                    )
                }
                
                if (currentRole == UserRole.WORKER || currentRole == UserRole.BOTH) {
                    RoleOption(
                        title = "User",
                        description = "Post tasks and hire workers",
                        icon = Icons.Default.Work,
                        onClick = { onRoleSelected(UserRole.USER) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Role option in dialog
 */
@Composable
fun RoleOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Role indicator chip for app bar
 */
@Composable
fun RoleIndicatorChip(
    currentRole: UserRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(getRoleDisplayName(currentRole)) },
        leadingIcon = {
            Icon(
                when (currentRole) {
                    UserRole.USER -> Icons.Default.Work
                    UserRole.WORKER -> Icons.Default.Build
                    UserRole.BOTH -> Icons.Default.SwapHoriz
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier
    )
}

/**
 * Get display name for role
 */
private fun getRoleDisplayName(role: UserRole): String {
    return when (role) {
        UserRole.USER -> "User"
        UserRole.WORKER -> "Worker"
        UserRole.BOTH -> "Both"
    }
}
