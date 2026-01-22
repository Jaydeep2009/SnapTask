package com.jaydeep.kaamly.ui.screens.user

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskState
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.ui.components.NotificationBadge
import com.jaydeep.kaamly.ui.components.RoleIndicatorChip
import com.jaydeep.kaamly.ui.components.RoleSwitcherFAB
import com.jaydeep.kaamly.ui.viewmodel.AuthViewModel
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel

/**
 * User Dashboard Screen showing posted tasks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    onNavigateToBidList: (String) -> Unit = {},
    onNavigateToWorkerDashboard: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userTasks by taskViewModel.userTasks.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()
    
    // Load user tasks on first composition
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            taskViewModel.loadUserTasks(user.id)
        }
    }
    
    // Show loading if user is not loaded yet
    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
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
            if (currentUser?.role == UserRole.BOTH || currentUser?.role == UserRole.WORKER) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Role switcher FAB
                    RoleSwitcherFAB(
                        currentRole = currentUser?.role ?: UserRole.USER,
                        onRoleSwitch = { newRole ->
                            authViewModel.switchRole(newRole)
                            if (newRole == UserRole.WORKER) {
                                onNavigateToWorkerDashboard()
                            }
                        }
                    )
                    
                    // Create task FAB
                    ExtendedFloatingActionButton(
                        onClick = onNavigateToCreateTask,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Create Task") }
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Create Task") }
                )
            }
        }
    ) { paddingValues ->
        when {
            error != null -> {
                ErrorState(
                    message = error?.message ?: "An error occurred",
                    onRetry = {
                        currentUser?.let { user ->
                            taskViewModel.loadUserTasks(user.id)
                        }
                    }
                )
            }
            userTasks.isEmpty() && !isLoading -> {
                EmptyTasksState(
                    onCreateTask = onNavigateToCreateTask
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Welcome header
                    item {
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
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Welcome, ${currentUser?.name ?: "User"}!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Role: ${currentUser?.role?.name ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Task statistics
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TaskStatCard(
                                title = "Open",
                                count = userTasks.count { it.state == TaskState.OPEN },
                                icon = Icons.Default.Schedule,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            TaskStatCard(
                                title = "In Progress",
                                count = userTasks.count { it.state == TaskState.IN_PROGRESS },
                                icon = Icons.Default.PlayArrow,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TaskStatCard(
                                title = "Completed",
                                count = userTasks.count { it.state == TaskState.COMPLETED },
                                icon = Icons.Default.CheckCircle,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Section header
                    item {
                        Text(
                            text = "Your Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Task list
                    items(userTasks) { task ->
                        UserTaskCard(
                            task = task,
                            onTaskClick = { onNavigateToTaskDetail(task.id) },
                            onViewBids = { onNavigateToBidList(task.id) },
                            onApproveCompletion = if (task.completionRequested && task.state == TaskState.IN_PROGRESS) {
                                {
                                    // Approve completion and release payment using the accepted bid amount
                                    val paymentAmount = task.acceptedBidAmount ?: task.budget
                                    taskViewModel.approveTaskAndReleasePayment(task.id, task.assignedWorkerId ?: "", paymentAmount)
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

/**
 * Task statistics card
 */
@Composable
fun TaskStatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

/**
 * User task card component
 */
@Composable
fun UserTaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onViewBids: () -> Unit,
    onApproveCompletion: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and state badge
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
                TaskStateBadge(state = task.state)
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
            
            // Category and instant job
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Location, budget, and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "₹${task.budget}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // View bids button (only for open tasks)
                if (task.state == TaskState.OPEN) {
                    Button(
                        onClick = onViewBids,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Bids", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
            // Approval section for tasks with completion requested
            if (task.completionRequested && task.state == TaskState.IN_PROGRESS && onApproveCompletion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Completion request notice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Worker has marked this task as complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Status Timeline
                TaskPaymentTimeline(
                    isPaid = true, // Payment already made when bid was accepted
                    isCompleted = false, // Not yet approved
                    task = task
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Approve button
                Button(
                    onClick = onApproveCompletion,
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
                    Text("Approve & Release Payment")
                }
            }
        }
    }
}

/**
 * Task state badge
 */
@Composable
fun TaskStateBadge(state: TaskState) {
    val (color, icon, text) = when (state) {
        TaskState.OPEN -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule,
            "Open"
        )
        TaskState.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.PlayArrow,
            "In Progress"
        )
        TaskState.COMPLETED -> Triple(
            MaterialTheme.colorScheme.secondary,
            Icons.Default.CheckCircle,
            "Completed"
        )
        TaskState.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Default.Cancel,
            "Cancelled"
        )
    }
    
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

/**
 * Empty tasks state
 */
@Composable
fun EmptyTasksState(
    onCreateTask: () -> Unit
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
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first task to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateTask,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Task")
            }
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
 * Payment status timeline for task approval
 */
@Composable
private fun TaskPaymentTimeline(
    isPaid: Boolean,
    isCompleted: Boolean,
    task: Task
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Payment Status",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Timeline Step 1: Payment Made
        val paymentAmount = task.acceptedBidAmount ?: task.budget
        TimelineStep(
            title = "Payment Made",
            description = "₹$paymentAmount paid to escrow",
            isCompleted = isPaid,
            isActive = false
        )
        
        // Timeline Connector
        TimelineConnector(isCompleted = isPaid)
        
        // Timeline Step 2: Work Completed
        TimelineStep(
            title = "Work Completed",
            description = "Worker marked task as done",
            isCompleted = true, // Always true in this section
            isActive = !isCompleted
        )
        
        // Timeline Connector
        TimelineConnector(isCompleted = isCompleted)
        
        // Timeline Step 3: Payment Released
        TimelineStep(
            title = "Payment Released",
            description = "Money transferred to worker",
            isCompleted = isCompleted,
            isActive = false
        )
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
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
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
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
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
                style = MaterialTheme.typography.bodyMedium,
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
        Spacer(modifier = Modifier.width(13.dp))
        
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(20.dp)
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
