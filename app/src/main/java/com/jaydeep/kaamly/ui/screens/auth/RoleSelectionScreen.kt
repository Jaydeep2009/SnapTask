package com.jaydeep.kaamly.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.ui.viewmodel.AuthState
import com.jaydeep.kaamly.ui.viewmodel.AuthViewModel

/**
 * Role selection screen for choosing user role after signup
 */
@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var hasClickedContinue by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Only navigate after user clicks Continue and role update succeeds
    LaunchedEffect(authState, hasClickedContinue) {
        if (hasClickedContinue) {
            android.util.Log.d("RoleSelection", "AuthState changed after Continue clicked: $authState")
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    android.util.Log.d("RoleSelection", "✅ User authenticated with role: ${state.user.role}")
                    // Role has been successfully updated, navigate
                    onRoleSelected(state.user.role)
                }
                is AuthState.Error -> {
                    android.util.Log.e("RoleSelection", "❌ Error state: ${state.message}")
                    // Reset flag so user can try again
                    hasClickedContinue = false
                }
                else -> {
                    android.util.Log.d("RoleSelection", "Other auth state: $state")
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Choose Your Role",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "How would you like to use Kaamly?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // User role card
            RoleCard(
                title = "Post Tasks",
                description = "I need help with tasks and want to hire workers",
                icon = Icons.Default.Work,
                isSelected = selectedRole == UserRole.USER,
                onClick = { selectedRole = UserRole.USER }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Worker role card
            RoleCard(
                title = "Work",
                description = "I want to complete tasks and earn money",
                icon = Icons.Default.Build,
                isSelected = selectedRole == UserRole.WORKER,
                onClick = { selectedRole = UserRole.WORKER }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Both roles card
            RoleCard(
                title = "Both",
                description = "I want to post tasks and work on tasks",
                icon = Icons.Default.Work,
                isSelected = selectedRole == UserRole.BOTH,
                onClick = { selectedRole = UserRole.BOTH }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            error?.let { errorState ->
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue button
            Button(
                onClick = {
                    selectedRole?.let { role ->
                        android.util.Log.d("RoleSelection", "Continue button clicked with role: $role")
                        hasClickedContinue = true
                        viewModel.selectRole(role)
                    } ?: run {
                        android.util.Log.w("RoleSelection", "Continue clicked but no role selected")
                    }
                },
                enabled = selectedRole != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continue", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "You can change this later in settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Reusable role selection card
 */
@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
