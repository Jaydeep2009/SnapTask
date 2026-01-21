package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.Notification
import com.jaydeep.kaamly.data.model.NotificationType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification operations
 */
interface NotificationRepository {
    /**
     * Create a new notification
     * @param notification The notification to create
     * @return Result containing the notification ID
     */
    suspend fun createNotification(notification: Notification): BaseRepository.Result<String>

    /**
     * Create a notification for a new bid
     * @param userId The user ID to notify
     * @param taskId The task ID
     * @param bidId The bid ID
     * @param workerName The name of the worker who placed the bid
     * @return Result containing the notification ID
     */
    suspend fun createNewBidNotification(
        userId: String,
        taskId: String,
        bidId: String,
        workerName: String
    ): BaseRepository.Result<String>

    /**
     * Create a notification for bid acceptance
     * @param workerId The worker ID to notify
     * @param taskId The task ID
     * @param taskTitle The title of the task
     * @return Result containing the notification ID
     */
    suspend fun createBidAcceptedNotification(
        workerId: String,
        taskId: String,
        taskTitle: String
    ): BaseRepository.Result<String>

    /**
     * Create a notification for task completion
     * @param userId The user ID to notify
     * @param taskId The task ID
     * @param taskTitle The title of the task
     * @return Result containing the notification ID
     */
    suspend fun createTaskCompletedNotification(
        userId: String,
        taskId: String,
        taskTitle: String
    ): BaseRepository.Result<String>

    /**
     * Create a notification for worker arrival
     * @param userId The user ID to notify
     * @param taskId The task ID
     * @param workerName The name of the worker
     * @return Result containing the notification ID
     */
    suspend fun createWorkerArrivedNotification(
        userId: String,
        taskId: String,
        workerName: String
    ): BaseRepository.Result<String>

    /**
     * Get all notifications for a user
     * @param userId The user ID
     * @return Flow of notification list
     */
    fun getUserNotifications(userId: String): Flow<List<Notification>>

    /**
     * Get unread notifications for a user
     * @param userId The user ID
     * @return Flow of unread notification list
     */
    fun getUnreadNotifications(userId: String): Flow<List<Notification>>

    /**
     * Get unread notification count for a user
     * @param userId The user ID
     * @return Flow of unread count
     */
    fun getUnreadCount(userId: String): Flow<Int>

    /**
     * Mark a notification as read
     * @param notificationId The notification ID
     * @return Result indicating success or failure
     */
    suspend fun markAsRead(notificationId: String): BaseRepository.Result<Unit>

    /**
     * Mark all notifications as read for a user
     * @param userId The user ID
     * @return Result indicating success or failure
     */
    suspend fun markAllAsRead(userId: String): BaseRepository.Result<Unit>

    /**
     * Delete a notification
     * @param notificationId The notification ID
     * @return Result indicating success or failure
     */
    suspend fun deleteNotification(notificationId: String): BaseRepository.Result<Unit>
}
