package com.jaydeep.kaamly.ui.screens

import androidx.compose.foundation.layout.*
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
import com.jaydeep.kaamly.ui.components.DemoFlowProgress
import com.jaydeep.kaamly.ui.components.DemoQuickActionCard
import com.jaydeep.kaamly.ui.components.SeedDemoDataButton
import com.jaydeep.kaamly.ui.viewmodel.DemoViewModel

/**
 * Demo settings and quick actions screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToWorkerDashboard: () -> Unit,
    viewModel: DemoViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val demoDataSeeded by viewModel.demoDataSeeded.collectAsState()
    val seedingProgress by viewModel.seedingProgress.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demo Mode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Demo mode header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Demo Mode",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Quick setup for hackathon demo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Seed demo data section
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Demo Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seed the database with sample users, workers, tasks, bids, and reviews for demonstration purposes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Seeding progress
                    seedingProgress?.let { progress ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = progress,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isLoading) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                    
                    // Error message
                    error?.let { errorState ->
                        Text(
                            text = errorState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeedDemoDataButton(
                            onClick = { viewModel.seedDemoData() },
                            isLoading = isLoading,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (demoDataSeeded) {
                            OutlinedButton(
                                onClick = { viewModel.clearDemoData() },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Data")
                            }
                        }
                    }
                }
            }
            
            // Demo flow progress
            DemoFlowProgress(
                currentStep = 0,
                totalSteps = 7,
                stepLabels = listOf(
                    "Sign up / Login",
                    "Create task",
                    "Switch to worker role",
                    "View and bid on task",
                    "Switch back to user",
                    "Accept bid and complete task",
                    "Leave review"
                )
            )
            
            // Quick actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            DemoQuickActionCard(
                title = "Create Task",
                description = "Post a new task as a user",
                icon = Icons.Default.Add,
                onClick = onNavigateToCreateTask
            )
            
            DemoQuickActionCard(
                title = "Worker Dashboard",
                description = "View available tasks as a worker",
                icon = Icons.Default.Work,
                onClick = onNavigateToWorkerDashboard
            )
            
            // Demo tips
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Demo Tips",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val tips = listOf(
                        "Use the 'Use Demo Data' button on forms to quickly fill in sample data",
                        "Use the role switcher FAB to quickly switch between User and Worker roles",
                        "The complete demo flow should take under 2 minutes",
                        "All payments and verifications are mock implementations for demo purposes"
                    )
                    
                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
