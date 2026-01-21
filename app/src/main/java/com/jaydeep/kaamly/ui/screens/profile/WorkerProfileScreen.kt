package com.jaydeep.kaamly.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.jaydeep.kaamly.data.model.Review
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.ui.viewmodel.ProfileViewModel
import com.jaydeep.kaamly.ui.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for displaying and editing worker profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToVerification: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val workerProfile by viewModel.workerProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val photoUploadProgress by viewModel.photoUploadProgress.collectAsState()
    val profileUpdateSuccess by viewModel.profileUpdateSuccess.collectAsState()
    val workerRating by reviewViewModel.workerRating.collectAsState()
    val workerReviews by reviewViewModel.workerReviews.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skillsText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadWorkerPhoto(workerId, it)
        }
    }
    
    // Load profile on first composition
    LaunchedEffect(workerId) {
        viewModel.loadWorkerProfile(workerId)
        reviewViewModel.loadWorkerRating(workerId)
        reviewViewModel.loadWorkerReviews(workerId)
    }
    
    // Update form fields when profile loads
    LaunchedEffect(workerProfile) {
        workerProfile?.let { profile ->
            name = profile.name
            city = profile.city
            phoneNumber = profile.phoneNumber ?: ""
            bio = profile.bio
            skillsText = profile.skills.joinToString(", ")
        }
    }
    
    // Show success message
    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) {
            viewModel.resetProfileUpdateSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Profile") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo Section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (workerProfile?.profilePhotoUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(workerProfile?.profilePhotoUrl),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    if (photoUploadProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tap to change photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Verification Badge
                if (workerProfile?.aadhaarVerified == true) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aadhaar Verified",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(
                        onClick = onNavigateToVerification,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Verify Aadhaar")
                    }
                }
                
                // Rating Display
                if (workerProfile != null && workerProfile!!.totalReviews > 0) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", workerProfile!!.overallRating),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${workerProfile!!.totalReviews} reviews)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // City Field
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone Number Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Skills Field
                OutlinedTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = { Text("Skills (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Plumbing, Electrical, Carpentry") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bio Field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Tell users about your experience and expertise...") }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save Button
                Button(
                    onClick = {
                        val skills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val profile = WorkerProfile(
                            workerId = workerId,
                            name = name,
                            city = city,
                            profilePhotoUrl = workerProfile?.profilePhotoUrl,
                            skills = skills,
                            bio = bio,
                            overallRating = workerProfile?.overallRating ?: 0.0,
                            totalReviews = workerProfile?.totalReviews ?: 0,
                            taskTypeRatings = workerProfile?.taskTypeRatings ?: emptyMap(),
                            aadhaarVerified = workerProfile?.aadhaarVerified ?: false,
                            phoneNumber = phoneNumber.ifBlank { null },
                            createdAt = workerProfile?.createdAt ?: System.currentTimeMillis()
                        )
                        viewModel.updateWorkerProfile(profile)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && city.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Profile")
                    }
                }
                
                // Error Message
                error?.let { errorState ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Success Message
                if (profileUpdateSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Profile updated successfully!",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Reviews Section
                if (workerRating != null && workerRating!!.totalReviews > 0) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Reviews Header
                    Text(
                        text = "Reviews & Ratings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Overall Rating Card
                    OverallRatingCard(workerRating!!)
                    
                    // Task-Type Ratings
                    if (workerRating!!.taskTypeRatings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TaskTypeRatingsCard(workerRating!!.taskTypeRatings)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Recent Reviews
                    Text(
                        text = "Recent Reviews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    workerReviews.take(10).forEach { review ->
                        ReviewCard(review)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * Card displaying overall rating
 */
@Composable
private fun OverallRatingCard(rating: com.jaydeep.kaamly.data.model.WorkerRating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", rating.overallRating),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating.overallRating.toInt()) 
                                Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${rating.totalReviews} reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Card displaying task-type specific ratings
 */
@Composable
private fun TaskTypeRatingsCard(taskTypeRatings: Map<TaskCategory, Double>) {
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
                text = "Ratings by Task Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            taskTypeRatings.forEach { (category, rating) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a single review
 */
@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Rating and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.starRating) 
                                Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = formatDate(review.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Review Text
            Text(
                text = review.textReview,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Task-Specific Ratings
            if (review.taskSpecificRating.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    review.taskSpecificRating.forEach { (key, value) ->
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = "${key.replaceFirstChar { it.uppercase() }}: $value★",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            enabled = false
                        )
                    }
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
