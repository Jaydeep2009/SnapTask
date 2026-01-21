package com.jaydeep.kaamly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Composable for displaying empty state with icon, message, and optional action
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(80.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Empty state for no tasks
 */
@Composable
fun NoTasksEmptyState(
    isWorker: Boolean,
    onCreateTask: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Assignment,
        title = if (isWorker) "No Tasks Available" else "No Tasks Yet",
        message = if (isWorker) 
            "There are no tasks in your area right now. Check back later or adjust your filters."
        else 
            "You haven't posted any tasks yet. Create your first task to get started!",
        actionLabel = if (!isWorker) "Create Task" else null,
        onAction = onCreateTask,
        modifier = modifier
    )
}

/**
 * Empty state for no bids
 */
@Composable
fun NoBidsEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Gavel,
        title = "No Bids Yet",
        message = "No workers have bid on this task yet. Workers in your area will see your task and can place bids.",
        modifier = modifier
    )
}

/**
 * Empty state for no reviews
 */
@Composable
fun NoReviewsEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.StarBorder,
        title = "No Reviews Yet",
        message = "This worker hasn't received any reviews yet. Be the first to work with them!",
        modifier = modifier
    )
}

/**
 * Empty state for no notifications
 */
@Composable
fun NoNotificationsEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Notifications,
        title = "No Notifications",
        message = "You're all caught up! You'll see notifications here when there's activity on your tasks or bids.",
        modifier = modifier
    )
}

/**
 * Empty state for no active tasks
 */
@Composable
fun NoActiveTasksEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Work,
        title = "No Active Tasks",
        message = "You don't have any active tasks right now. Browse available tasks and place bids to get started!",
        modifier = modifier
    )
}

/**
 * Empty state for search results
 */
@Composable
fun NoSearchResultsEmptyState(
    searchQuery: String,
    onClearFilters: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = "No Results Found",
        message = "We couldn't find any tasks matching \"$searchQuery\". Try adjusting your search or filters.",
        actionLabel = if (onClearFilters != null) "Clear Filters" else null,
        onAction = onClearFilters,
        modifier = modifier
    )
}
