package com.jaydeep.kaamly.ui.screens.worker

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Location
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.ui.components.NotificationBadge
import com.jaydeep.kaamly.ui.components.RoleIndicatorChip
import com.jaydeep.kaamly.ui.components.RoleSwitcherFAB
import com.jaydeep.kaamly.ui.viewmodel.AuthViewModel
import com.jaydeep.kaamly.ui.viewmodel.BidViewModel
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import com.jaydeep.kaamly.ui.viewmodel.TaskFilters
import com.jaydeep.kaamly.ui.viewmodel.WalletViewModel

/**
 * Worker dashboard screen showing nearby tasks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    onTaskClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToUserDashboard: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: TaskViewModel = hiltViewModel(),
    bidViewModel: BidViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val nearbyTasks by viewModel.nearbyTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val taskFilters by viewModel.taskFilters.collectAsState()
    
    // Add state for worker bids
    val workerBids by bidViewModel.workerBids.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Available Tasks, 1 = My Applications, 2 = Approved Jobs
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var userCity by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }
    
    // Load worker's active tasks (approved jobs)
    val workerActiveTasks by viewModel.workerActiveTasks.collectAsState()
    
    // Load wallet balance
    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    val walletBalance by walletViewModel.balance.collectAsState()
    
    LaunchedEffect(currentFirebaseUser) {
        currentFirebaseUser?.let { user ->
            // Load wallet balance
            walletViewModel.loadWallet(user.uid)
        }
    }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                               permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (hasLocationPermission) {
            // Load all open tasks
            viewModel.loadAllOpenTasks()
        }
    }
    
    // Load tasks on first composition
    LaunchedEffect(Unit) {
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (currentFirebaseUser != null) {
            // Load all open tasks for workers to see all available jobs
            viewModel.loadAllOpenTasks()
            // Load worker's bids
            bidViewModel.loadWorkerBids(currentFirebaseUser.uid)
            // Load worker's active tasks (approved jobs)
            viewModel.loadWorkerActiveTasks(currentFirebaseUser.uid)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text("Available Tasks")
                            // Show wallet balance
                            currentFirebaseUser?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", walletBalance)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Notification badge
                    NotificationBadge(
                        onClick = onNavigateToNotifications
                    )
                    
                    // Role indicator chip
                    currentUser?.let { user ->
                        RoleIndicatorChip(
                            currentRole = user.role,
                            onClick = {
                                // Show role switcher dialog
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showFilterDialog = true }) {
                        Badge(
                            containerColor = if (taskFilters.category != null || 
                                                taskFilters.instantJobsOnly ||
                                                taskFilters.minBudget != null ||
                                                taskFilters.maxBudget != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            // Show both FABs if user has BOTH role
            if (currentUser?.role == UserRole.BOTH || currentUser?.role == UserRole.USER) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Role switcher FAB
                    RoleSwitcherFAB(
                        currentRole = currentUser?.role ?: UserRole.WORKER,
                        onRoleSwitch = { newRole ->
                            authViewModel.switchRole(newRole)
                            if (newRole == UserRole.USER) {
                                onNavigateToUserDashboard()
                            }
                        }
                    )
                    
                    // Location FAB
                    ExtendedFloatingActionButton(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        icon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                        text = { Text("Use My Location") }
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    icon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                    text = { Text("Use My Location") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs for switching between Available Tasks, My Applications, and Approved Jobs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Available") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Applications") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Approved") }
                )
            }
            
            // Wallet Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Wallet Balance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "₹${String.format("%.2f", walletBalance)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Optional: Add withdraw button for future
                    // OutlinedButton(onClick = { /* TODO */ }) {
                    //     Text("Withdraw")
                    // }
                }
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Available Tasks Tab
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isLoading),
                        onRefresh = {
                            viewModel.loadAllOpenTasks()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            error != null -> {
                                ErrorState(
                                    message = error?.message ?: "An error occurred",
                                    onRetry = {
                                        viewModel.loadAllOpenTasks()
                                    }
                                )
                            }
                            nearbyTasks.isEmpty() && !isLoading -> {
                                EmptyState(
                                    message = "No tasks available",
                                    icon = Icons.Default.WorkOff
                                )
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Task list
                                    items(nearbyTasks) { task ->
                                        TaskCard(
                                            task = task,
                                            onClick = { onTaskClick(task.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // My Applications Tab
                    if (workerBids.isEmpty() && !isLoading) {
                        EmptyState(
                            message = "You haven't applied to any tasks yet",
                            icon = Icons.Default.Assignment
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(workerBids) { bid ->
                                WorkerBidCard(
                                    bid = bid,
                                    onClick = { onTaskClick(bid.taskId) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Approved Jobs Tab
                    if (workerActiveTasks.isEmpty() && !isLoading) {
                        EmptyState(
                            message = "No approved jobs yet",
                            icon = Icons.Default.CheckCircle
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(workerActiveTasks) { task ->
                                ApprovedJobCard(
                                    task = task,
                                    onMarkComplete = {
                                        viewModel.markTaskCompleted(task.id)
                                    },
                                    onClick = { onTaskClick(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilters = taskFilters,
            onDismiss = { showFilterDialog = false },
            onApply = { filters ->
                viewModel.applyFilters(filters)
                showFilterDialog = false
            }
        )
    }
}

/**
 * Task card component
 */
@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and instant job badge
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
                if (task.isInstantJob) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Instant", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category chip
            AssistChip(
                onClick = {},
                label = { 
                    Text(
                        task.category.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location and budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.location.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "₹${task.budget}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Empty state component
 */
@Composable
fun EmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state component
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Filter dialog component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilters: TaskFilters,
    onDismiss: () -> Unit,
    onApply: (TaskFilters) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentFilters.category) }
    var instantJobsOnly by remember { mutableStateOf(currentFilters.instantJobsOnly) }
    var minBudget by remember { mutableStateOf(currentFilters.minBudget?.toString() ?: "") }
    var maxBudget by remember { mutableStateOf(currentFilters.maxBudget?.toString() ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category filter
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name?.replace("_", " ") ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategory = null
                                categoryExpanded = false
                            }
                        )
                        TaskCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.replace("_", " ")) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Instant jobs filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Instant jobs only", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = instantJobsOnly,
                        onCheckedChange = { instantJobsOnly = it }
                    )
                }
                
                // Budget range
                Text(
                    text = "Budget Range",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minBudget,
                        onValueChange = { minBudget = it },
                        label = { Text("Min (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = maxBudget,
                        onValueChange = { maxBudget = it },
                        label = { Text("Max (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val filters = TaskFilters(
                        category = selectedCategory,
                        instantJobsOnly = instantJobsOnly,
                        minBudget = minBudget.toDoubleOrNull(),
                        maxBudget = maxBudget.toDoubleOrNull()
                    )
                    onApply(filters)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/**
 * Card displaying a worker's bid application
 */
@Composable
fun WorkerBidCard(
    bid: com.jaydeep.kaamly.data.model.Bid,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bid Amount: ₹${bid.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Status badge
                Surface(
                    color = when (bid.status) {
                        com.jaydeep.kaamly.data.model.BidStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                        com.jaydeep.kaamly.data.model.BidStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                        com.jaydeep.kaamly.data.model.BidStatus.REJECTED -> MaterialTheme.colorScheme.error
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (bid.status) {
                            com.jaydeep.kaamly.data.model.BidStatus.PENDING -> "Pending"
                            com.jaydeep.kaamly.data.model.BidStatus.ACCEPTED -> "Accepted ✓"
                            com.jaydeep.kaamly.data.model.BidStatus.REJECTED -> "Rejected"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (bid.status) {
                            com.jaydeep.kaamly.data.model.BidStatus.PENDING -> MaterialTheme.colorScheme.onTertiary
                            com.jaydeep.kaamly.data.model.BidStatus.ACCEPTED -> MaterialTheme.colorScheme.onPrimary
                            com.jaydeep.kaamly.data.model.BidStatus.REJECTED -> MaterialTheme.colorScheme.onError
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bid message
            if (bid.message.isNotBlank()) {
                Text(
                    text = bid.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Bid date
            Text(
                text = "Applied: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(bid.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Action hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Tap to view task details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


/**
 * Card displaying an approved job that the worker can mark as complete
 */
@Composable
fun ApprovedJobCard(
    task: Task,
    onMarkComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title with status badge
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
                
                // Status badge
                Surface(
                    color = if (task.completionRequested) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (task.completionRequested) "Pending Approval" else "In Progress",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.completionRequested) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location and budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.location.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = "₹${task.budget}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mark Complete Button
            if (!task.completionRequested) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Complete")
                }
            } else {
                Text(
                    text = "Waiting for user approval...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
