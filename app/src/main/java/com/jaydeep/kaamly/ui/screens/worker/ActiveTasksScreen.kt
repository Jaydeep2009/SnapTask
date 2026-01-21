package com.jaydeep.kaamly.ui.screens.worker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel

/**
 * Screen displaying active tasks for workers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTasksScreen(
    onNavigateBack: () -> Unit,
    onTaskClick: (String) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val photoUploaded by viewModel.photoUploaded.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Load active tasks when screen opens
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { workerId ->
            viewModel.loadWorkerActiveTasks(workerId)
        }
    }
    
    // Show success message when photo is uploaded
    LaunchedEffect(photoUploaded) {
        if (photoUploaded != null) {
            viewModel.clearPhotoUploaded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Tasks") },
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
                activeTasks.isEmpty() -> {
                    EmptyActiveTasksState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeTasks) { task ->
                            ActiveTaskCard(
                                task = task,
                                onMarkArrived = { viewModel.markArrived(task.id) },
                                onUploadPhoto = { uri -> viewModel.uploadCompletionPhoto(task.id, uri) },
                                onMarkCompleted = { viewModel.markCompleted(task.id) },
                                onTaskClick = { onTaskClick(task.id) }
                            )
                        }
                    }
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
fun ActiveTaskCard(
    task: Task,
    onMarkArrived: () -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onMarkCompleted: () -> Unit,
    onTaskClick: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            onUploadPhoto(it)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTaskClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Task title and budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "₹${task.budget}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Task location
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
            
            Divider()
            
            // Task progress status
            TaskProgressIndicator(task)
            
            Divider()
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mark Arrived button
                if (!task.workerArrived) {
                    Button(
                        onClick = onMarkArrived,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Arrived")
                    }
                }
                
                // Upload Photo button
                if (task.workerArrived && task.completionPhotoUrl == null) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Completion Photo")
                    }
                }
                
                // Mark Completed button
                if (task.completionPhotoUrl != null && !task.completionRequested) {
                    Button(
                        onClick = onMarkCompleted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Task Completed")
                    }
                }
                
                // Waiting for approval
                if (task.completionRequested) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Waiting for user approval",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskProgressIndicator(task: Task) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Task Progress",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Arrived status
        ProgressStep(
            label = "Arrived at location",
            isCompleted = task.workerArrived,
            icon = Icons.Default.LocationOn
        )
        
        // Photo uploaded status
        ProgressStep(
            label = "Completion photo uploaded",
            isCompleted = task.completionPhotoUrl != null,
            icon = Icons.Default.Photo
        )
        
        // Completion requested status
        ProgressStep(
            label = "Completion requested",
            isCompleted = task.completionRequested,
            icon = Icons.Default.CheckCircle
        )
    }
}

@Composable
fun ProgressStep(
    label: String,
    isCompleted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyActiveTasksState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Work,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "No Active Tasks",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You don't have any active tasks at the moment. Browse available tasks and place bids to get started!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
