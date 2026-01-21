package com.jaydeep.kaamly.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskState
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel

/**
 * Screen displaying task progress for users
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskProgressScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToReview: (String, String) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val selectedTask by viewModel.selectedTask.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load task when screen opens
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }
    
    // Navigate to review screen when task is completed
    LaunchedEffect(selectedTask?.state) {
        if (selectedTask?.state == TaskState.COMPLETED) {
            selectedTask?.assignedWorkerId?.let { workerId ->
                // Could navigate to review screen here if desired
                // onNavigateToReview(taskId, workerId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Progress") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                selectedTask != null -> {
                    TaskProgressContent(
                        task = selectedTask!!,
                        onApproveCompletion = { viewModel.approveCompletion(taskId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Text(
                        text = "Task not found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Show error message
            error?.let { errorState ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorState.message)
                }
            }
        }
    }
}

@Composable
fun TaskProgressContent(
    task: Task,
    onApproveCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Task details card
        TaskDetailsCard(task)
        
        // Progress timeline
        ProgressTimeline(task)
        
        // Completion photo if uploaded
        if (task.completionPhotoUrl != null) {
            CompletionPhotoCard(task.completionPhotoUrl!!)
        }
        
        // Completion request
        if (task.completionRequested && task.state != TaskState.COMPLETED) {
            CompletionRequestCard(onApproveCompletion)
        }
        
        // Escrow status
        EscrowStatusCard(task)
        
        // Task completed message
        if (task.state == TaskState.COMPLETED) {
            TaskCompletedCard()
        }
    }
}

@Composable
fun TaskDetailsCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget: ₹${task.budget}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = task.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.location.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProgressTimeline(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Progress Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Worker assigned
            TimelineStep(
                label = "Worker assigned",
                isCompleted = task.assignedWorkerId != null,
                icon = Icons.Default.Person
            )
            
            // Worker arrived
            TimelineStep(
                label = "Worker arrived at location",
                isCompleted = task.workerArrived,
                icon = Icons.Default.LocationOn
            )
            
            // Photo uploaded
            TimelineStep(
                label = "Completion photo uploaded",
                isCompleted = task.completionPhotoUrl != null,
                icon = Icons.Default.Photo
            )
            
            // Completion requested
            TimelineStep(
                label = "Completion requested",
                isCompleted = task.completionRequested,
                icon = Icons.Default.CheckCircle
            )
            
            // Task completed
            TimelineStep(
                label = "Task completed",
                isCompleted = task.state == TaskState.COMPLETED,
                icon = Icons.Default.Done
            )
        }
    }
}

@Composable
fun TimelineStep(
    label: String,
    isCompleted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun CompletionPhotoCard(photoUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Completion Photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            AsyncImage(
                model = photoUrl,
                contentDescription = "Task completion photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun CompletionRequestCard(onApproveCompletion: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Completion Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = "The worker has marked this task as completed. Please review the work and approve if satisfied.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Button(
                onClick = onApproveCompletion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Approve Task Completion")
            }
        }
    }
}

@Composable
fun EscrowStatusCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.state == TaskState.COMPLETED) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (task.state == TaskState.COMPLETED) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (task.state == TaskState.COMPLETED) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Escrow Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (task.state == TaskState.COMPLETED) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Text(
                text = if (task.state == TaskState.COMPLETED) 
                    "✅ Escrow released to worker (mock)" 
                else 
                    "🔒 Amount locked in escrow (mock): ₹${task.budget}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.state == TaskState.COMPLETED) 
                    MaterialTheme.colorScheme.onTertiaryContainer 
                else 
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = "Note: This is a demo. No real payments are processed.",
                style = MaterialTheme.typography.bodySmall,
                color = if (task.state == TaskState.COMPLETED) 
                    MaterialTheme.colorScheme.onTertiaryContainer 
                else 
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun TaskCompletedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Task Completed!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Thank you for using Kaamly. Don't forget to leave a review for the worker!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
