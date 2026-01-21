package com.jaydeep.kaamly.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaydeep.kaamly.ui.viewmodel.BidViewModel
import com.jaydeep.kaamly.ui.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Screen for payment and escrow operations (mock)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    taskId: String,
    bidId: String,
    bidAmount: Double,
    onNavigateBack: () -> Unit,
    onPaymentConfirmed: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
    bidViewModel: BidViewModel = hiltViewModel()
) {
    val escrowStatus by viewModel.escrowStatus.collectAsState()
    val paymentBreakdown by viewModel.paymentBreakdown.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val escrowLocked by viewModel.escrowLocked.collectAsState()
    val bidAccepted by bidViewModel.bidAccepted.collectAsState()
    
    // Calculate payment breakdown on load
    LaunchedEffect(bidAmount) {
        viewModel.calculateTotal(bidAmount)
    }
    
    // Handle escrow locked success - then accept the bid
    LaunchedEffect(escrowLocked) {
        if (escrowLocked) {
            // After escrow is locked, accept the bid
            bidViewModel.acceptBid(bidId)
            viewModel.clearEscrowLocked()
        }
    }
    
    // Handle bid accepted success - navigate back
    LaunchedEffect(bidAccepted) {
        if (bidAccepted) {
            onPaymentConfirmed()
            bidViewModel.clearBidAccepted()
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
                title = { Text("Payment") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mock Payment Disclaimer
            MockPaymentDisclaimer()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Payment Breakdown Card
            paymentBreakdown?.let { breakdown ->
                PaymentBreakdownCard(breakdown = breakdown)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Escrow Status
            if (escrowStatus != null) {
                EscrowStatusCard(status = escrowStatus!!.status)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Confirm Payment Button
            Button(
                onClick = { viewModel.lockEscrow(taskId, bidAmount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && escrowStatus == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (escrowStatus == null) "Confirm Payment" else "Payment Confirmed",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * Mock payment disclaimer card
 */
@Composable
private fun MockPaymentDisclaimer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Demo Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "This is a mock payment system. No real transactions will be processed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Payment breakdown card showing bid amount, platform fee, and total
 */
@Composable
private fun PaymentBreakdownCard(
    breakdown: com.jaydeep.kaamly.ui.viewmodel.PaymentBreakdown
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bid Amount
            PaymentRow(
                label = "Bid Amount",
                amount = breakdown.bidAmount
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Platform Fee
            PaymentRow(
                label = "Platform Fee",
                amount = breakdown.platformFee
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatCurrency(breakdown.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Single row in payment breakdown
 */
@Composable
private fun PaymentRow(
    label: String,
    amount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Escrow status card
 */
@Composable
private fun EscrowStatusCard(
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (status == "locked") {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (status == "locked") {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = if (status == "locked") {
                        "Amount Locked in Escrow (Mock)"
                    } else {
                        "Escrow Released to Worker (Mock)"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (status == "locked") {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (status == "locked") {
                        "Funds are secured until task completion"
                    } else {
                        "Payment has been released to the worker"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (status == "locked") {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }
        }
    }
}

/**
 * Escrow timeline card showing money flow
 */
@Composable
private fun EscrowTimelineCard(
    isPaid: Boolean,
    isReleased: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timeline Step 1: User Pays
            TimelineStep(
                title = "User Pays",
                description = "Payment confirmed",
                isCompleted = isPaid,
                isActive = !isPaid
            )
            
            // Timeline Connector
            TimelineConnector(isCompleted = isPaid)
            
            // Timeline Step 2: Money in Escrow
            TimelineStep(
                title = "Money in Escrow",
                description = "Funds held securely",
                isCompleted = isReleased,
                isActive = isPaid && !isReleased
            )
            
            // Timeline Connector
            TimelineConnector(isCompleted = isReleased)
            
            // Timeline Step 3: Worker Receives
            TimelineStep(
                title = "Worker Receives",
                description = "After task completion & approval",
                isCompleted = isReleased,
                isActive = false
            )
        }
    }
}

/**
 * Single timeline step
 */
@Composable
private fun TimelineStep(
    title: String,
    description: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Step content
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive || isCompleted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Timeline connector line
 */
@Composable
private fun TimelineConnector(
    isCompleted: Boolean
) {
    Row {
        Spacer(modifier = Modifier.width(15.dp))
        
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .background(
                    if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
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
