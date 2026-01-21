package com.jaydeep.kaamly.data.model

/**
 * Data class representing a notification
 */
data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.NEW_BID,
    val relatedId: String = "", // taskId or bidId
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing notification types
 */
enum class NotificationType {
    NEW_BID,
    BID_ACCEPTED,
    TASK_COMPLETED,
    WORKER_ARRIVED,
    REVIEW_RECEIVED
}
