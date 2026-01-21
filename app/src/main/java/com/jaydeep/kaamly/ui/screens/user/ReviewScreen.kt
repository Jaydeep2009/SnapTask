package com.jaydeep.kaamly.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.ui.viewmodel.ProfileViewModel
import com.jaydeep.kaamly.ui.viewmodel.ReviewViewModel
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for submitting a review for a worker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    taskId: String,
    workerId: String,
    onReviewSubmitted: () -> Unit,
    onNavigateBack: () -> Unit,
    reviewViewModel: ReviewViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var starRating by remember { mutableIntStateOf(0) }
    var textReview by remember { mutableStateOf("") }
    var punctualityRating by remember { mutableIntStateOf(0) }
    var qualityRating by remember { mutableIntStateOf(0) }
    var professionalismRating by remember { mutableIntStateOf(0) }
    
    // Validation errors
    var ratingError by remember { mutableStateOf<String?>(null) }
    var textError by remember { mutableStateOf<String?>(null) }
    
    val isLoading by reviewViewModel.isLoading.collectAsState()
    val error by reviewViewModel.error.collectAsState()
    val reviewSubmitted by reviewViewModel.reviewSubmitted.collectAsState()
    val selectedTask by taskViewModel.selectedTask.collectAsState()
    val workerProfile by profileViewModel.workerProfile.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Load task and worker details
    LaunchedEffect(taskId, workerId) {
        taskViewModel.loadTask(taskId)
        profileViewModel.loadWorkerProfile(workerId)
    }
    
    // Handle review submission success
    LaunchedEffect(reviewSubmitted) {
        if (reviewSubmitted) {
            onReviewSubmitted()
            reviewViewModel.clearReviewSubmitted()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it.message)
            reviewViewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Task and Worker Info Card
            selectedTask?.let { task ->
                workerProfile?.let { worker ->
                    TaskWorkerInfoCard(task, worker)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Overall Rating
            Text(
                text = "Overall Rating *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StarRatingSelector(
                rating = starRating,
                onRatingChange = {
                    starRating = it
                    ratingError = null
                }
            )
            
            if (ratingError != null) {
                Text(
                    text = ratingError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text Review
            OutlinedTextField(
                value = textReview,
                onValueChange = {
                    textReview = it
                    textError = null
                },
                label = { Text("Your Review *") },
                placeholder = { Text("Share your experience with this worker...") },
                isError = textError != null,
                supportingText = textError?.let { { Text(it) } },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Task-Specific Ratings
            Text(
                text = "Task-Specific Ratings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Punctuality
            TaskSpecificRatingRow(
                label = "Punctuality",
                rating = punctualityRating,
                onRatingChange = { punctualityRating = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quality
            TaskSpecificRatingRow(
                label = "Quality of Work",
                rating = qualityRating,
                onRatingChange = { qualityRating = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Professionalism
            TaskSpecificRatingRow(
                label = "Professionalism",
                rating = professionalismRating,
                onRatingChange = { professionalismRating = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit Button
            Button(
                onClick = {
                    // Validate inputs
                    var hasError = false
                    
                    if (starRating == 0) {
                        ratingError = "Please select a star rating"
                        hasError = true
                    }
                    
                    if (textReview.isBlank()) {
                        textError = "Please write a review"
                        hasError = true
                    } else if (textReview.length < 10) {
                        textError = "Review must be at least 10 characters"
                        hasError = true
                    }
                    
                    if (!hasError && currentUser != null) {
                        val taskSpecificRating = mutableMapOf<String, Int>()
                        if (punctualityRating > 0) {
                            taskSpecificRating["punctuality"] = punctualityRating
                        }
                        if (qualityRating > 0) {
                            taskSpecificRating["quality"] = qualityRating
                        }
                        if (professionalismRating > 0) {
                            taskSpecificRating["professionalism"] = professionalismRating
                        }
                        
                        reviewViewModel.submitReview(
                            taskId = taskId,
                            workerId = workerId,
                            userId = currentUser.uid,
                            starRating = starRating,
                            textReview = textReview,
                            taskSpecificRating = taskSpecificRating
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Review")
                }
            }
        }
    }
}

/**
 * Card displaying task and worker information
 */
@Composable
private fun TaskWorkerInfoCard(task: Task, worker: WorkerProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Task Completed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Task Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Worker Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Worker:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Completed:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(task.updatedAt),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Star rating selector component
 */
@Composable
private fun StarRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            IconButton(
                onClick = { onRatingChange(i) }
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * Task-specific rating row component
 */
@Composable
private fun TaskSpecificRatingRow(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                IconButton(
                    onClick = { onRatingChange(i) }
                ) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "$label Star $i",
                        tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Format date
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
