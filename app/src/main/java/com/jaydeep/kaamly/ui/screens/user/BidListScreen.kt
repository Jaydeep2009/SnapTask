package com.jaydeep.kaamly.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaydeep.kaamly.data.model.BidStatus
import com.jaydeep.kaamly.ui.viewmodel.BidViewModel
import com.jaydeep.kaamly.ui.viewmodel.BidWithWorker
import java.text.NumberFormat
import java.util.*

/**
 * Screen displaying all bids for a task
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidListScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onViewWorkerProfile: (String) -> Unit,
    onNavigateToPayment: (String, Double) -> Unit, // Navigate to payment with bidId and amount
    onBidAccepted: () -> Unit,
    viewModel: BidViewModel = hiltViewModel()
) {
    val taskBids by viewModel.taskBids.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val bidAccepted by viewModel.bidAccepted.collectAsState()
    
    // Load bids for this task
    LaunchedEffect(taskId) {
        viewModel.loadBidsForTask(taskId)
    }
    
    // Handle bid acceptance success
    LaunchedEffect(bidAccepted) {
        if (bidAccepted) {
            onBidAccepted()
            viewModel.clearBidAccepted()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bids (${taskBids.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && taskBids.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                taskBids.isEmpty() -> {
                    EmptyBidsState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(taskBids) { bidWithWorker ->
                            BidCard(
                                bidWithWorker = bidWithWorker,
                                onViewProfile = { onViewWorkerProfile(bidWithWorker.worker.workerId) },
                                onAcceptBid = { 
                                    // Navigate to payment screen instead of directly accepting
                                    onNavigateToPayment(bidWithWorker.bid.id, bidWithWorker.bid.amount)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a single bid with worker information
 */
@Composable
private fun BidCard(
    bidWithWorker: BidWithWorker,
    onViewProfile: () -> Unit,
    onAcceptBid: () -> Unit
) {
    val bid = bidWithWorker.bid
    val worker = bidWithWorker.worker
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Worker Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = worker.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            if (worker.aadhaarVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Rating
                        if (worker.totalReviews > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", worker.overallRating),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = " (${worker.totalReviews} reviews)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Bid Amount
                Text(
                    text = formatCurrency(bid.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Worker Skills
            if (worker.skills.isNotEmpty()) {
                Text(
                    text = "Skills: ${worker.skills.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Bid Message
            Text(
                text = bid.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Profile")
                }
                
                if (bid.status == BidStatus.PENDING) {
                    Button(
                        onClick = onAcceptBid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept Bid")
                    }
                } else if (bid.status == BidStatus.ACCEPTED) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Accepted") },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no bids are available
 */
@Composable
private fun EmptyBidsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Bids Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Workers will see your task and place bids soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format currency value
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}
