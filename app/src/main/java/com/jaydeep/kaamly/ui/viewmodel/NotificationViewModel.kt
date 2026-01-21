package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.Notification
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing notifications
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val unreadNotifications: StateFlow<List<Notification>> = _unreadNotifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    /**
     * Load notifications for a user
     * @param userId The user ID
     */
    fun loadNotifications(userId: String) {
        _currentUserId.value = userId
        
        viewModelScope.launch {
            notificationRepository.getUserNotifications(userId).collect { notificationList ->
                _notifications.value = notificationList
            }
        }

        viewModelScope.launch {
            notificationRepository.getUnreadNotifications(userId).collect { unreadList ->
                _unreadNotifications.value = unreadList
            }
        }

        viewModelScope.launch {
            notificationRepository.getUnreadCount(userId).collect { count ->
                _unreadCount.value = count
            }
        }
    }

    /**
     * Mark a notification as read
     * @param notificationId The notification ID
     */
    fun markAsRead(notificationId: String) {
        execute(
            onError = { "Failed to mark notification as read: ${it.message}" }
        ) {
            when (val result = notificationRepository.markAsRead(notificationId)) {
                is BaseRepository.Result.Success -> {
                    // Success - the flow will automatically update
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        val userId = _currentUserId.value ?: return
        
        execute(
            onError = { "Failed to mark all notifications as read: ${it.message}" }
        ) {
            when (val result = notificationRepository.markAllAsRead(userId)) {
                is BaseRepository.Result.Success -> {
                    // Success - the flow will automatically update
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Delete a notification
     * @param notificationId The notification ID
     */
    fun deleteNotification(notificationId: String) {
        execute(
            onError = { "Failed to delete notification: ${it.message}" }
        ) {
            when (val result = notificationRepository.deleteNotification(notificationId)) {
                is BaseRepository.Result.Success -> {
                    // Success - the flow will automatically update
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Get the current unread count
     * @return The current unread count
     */
    fun getCurrentUnreadCount(): Int {
        return _unreadCount.value
    }

    /**
     * Refresh notifications
     */
    fun refresh() {
        _currentUserId.value?.let { userId ->
            loadNotifications(userId)
        }
    }
}
