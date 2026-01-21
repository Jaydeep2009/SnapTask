package com.jaydeep.kaamly.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jaydeep.kaamly.ui.viewmodel.NotificationViewModel

/**
 * Notification icon with badge showing unread count
 */
@Composable
fun NotificationBadge(
    onClick: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by viewModel.unreadCount.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Load notifications on first composition
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            viewModel.loadNotifications(userId)
        }
    }

    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
