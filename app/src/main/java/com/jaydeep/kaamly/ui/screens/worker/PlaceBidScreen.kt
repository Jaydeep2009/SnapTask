package com.jaydeep.kaamly.ui.screens.worker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.ui.viewmodel.BidViewModel
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for placing a bid on a task
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceBidScreen(
    taskId: String,
    onBidPlaced: () -> Unit,
    onNavigateBack: () -> Unit,
    bidViewModel: BidViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    var bidAmount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    // Validation errors
    var amountError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    
    val isLoading by bidViewModel.isLoading.collectAsState()
    val error by bidViewModel.error.collectAsState()
    val bidPlaced by bidViewModel.bidPlaced.collectAsState()
    val selectedTask by taskViewModel.selectedTask.collectAsState()
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Load task details
    LaunchedEffect(taskId) {
        taskViewModel.loadTask(taskId)
    }
    
    // Handle bid placement success
    LaunchedEffect(bidPlaced) {
        if (bidPlaced != null) {
            onBidPlaced()
            bidViewModel.clearBidPlaced()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it.message)
            bidViewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Bid") },
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
            // Task Summary Card
            selectedTask?.let { task ->
                TaskSummaryCard(task)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Bid Amount Field
            OutlinedTextField(
                value = bidAmount,
                onValueChange = {
                    bidAmount = it
                    amountError = null
                },
                label = { Text("Your Bid Amount (₹) *") },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Message Field
            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    messageError = null
                },
                label = { Text("Message to Task Owner *") },
                placeholder = { Text("Explain why you're the best fit for this task...") },
                isError = messageError != null,
                supportingText = messageError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                minLines = 4,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = {
                    // Validate inputs
                    var hasError = false
                    
                    if (bidAmount.isBlank()) {
                        amountError = "Please enter your bid amount"
                        hasError = true
                    } else {
                        val amount = bidAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            amountError = "Please enter a valid positive amount"
                            hasError = true
                        }
                    }
                    
                    if (message.isBlank()) {
                        messageError = "Please provide a message"
                        hasError = true
                    } else if (message.length < 10) {
                        messageError = "Message must be at least 10 characters"
                        hasError = true
                    }
                    
                    if (!hasError && currentUser != null) {
                        val amount = bidAmount.toDouble()
                        bidViewModel.placeBid(
                            taskId = taskId,
                            workerId = currentUser.uid,
                            amount = amount,
                            message = message
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
                    Text("Submit Bid")
                }
            }
        }
    }
}

/**
 * Card displaying task summary
 */
@Composable
private fun TaskSummaryCard(task: Task) {
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
                text = "Task Summary",
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
            
            // Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(task.budget),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Location:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.location.city,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(task.scheduledDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.category.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (task.isInstantJob) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text("Instant Job") },
                    enabled = false
                )
            }
        }
    }
}

/**
 * Format currency value
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}

/**
 * Format date
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
