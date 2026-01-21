package com.jaydeep.kaamly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jaydeep.kaamly.data.model.Notification
import com.jaydeep.kaamly.data.model.NotificationType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of NotificationRepository using Firebase Firestore
 */
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val notificationsCollection = firestore.collection("notifications")

    override suspend fun createNotification(notification: Notification): BaseRepository.Result<String> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(id = docRef.id)
            docRef.set(notificationWithId).await()
            BaseRepository.Result.Success(docRef.id)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun createNewBidNotification(
        userId: String,
        taskId: String,
        bidId: String,
        workerName: String
    ): BaseRepository.Result<String> {
        val notification = Notification(
            userId = userId,
            title = "New Bid Received",
            message = "$workerName has placed a bid on your task",
            type = NotificationType.NEW_BID,
            relatedId = taskId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        return createNotification(notification)
    }

    override suspend fun createBidAcceptedNotification(
        workerId: String,
        taskId: String,
        taskTitle: String
    ): BaseRepository.Result<String> {
        val notification = Notification(
            userId = workerId,
            title = "Bid Accepted",
            message = "Your bid for \"$taskTitle\" has been accepted",
            type = NotificationType.BID_ACCEPTED,
            relatedId = taskId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        return createNotification(notification)
    }

    override suspend fun createTaskCompletedNotification(
        userId: String,
        taskId: String,
        taskTitle: String
    ): BaseRepository.Result<String> {
        val notification = Notification(
            userId = userId,
            title = "Task Completed",
            message = "\"$taskTitle\" has been marked as completed",
            type = NotificationType.TASK_COMPLETED,
            relatedId = taskId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        return createNotification(notification)
    }

    override suspend fun createWorkerArrivedNotification(
        userId: String,
        taskId: String,
        workerName: String
    ): BaseRepository.Result<String> {
        val notification = Notification(
            userId = userId,
            title = "Worker Arrived",
            message = "$workerName has arrived at the task location",
            type = NotificationType.WORKER_ARRIVED,
            relatedId = taskId,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        return createNotification(notification)
    }

    override fun getUserNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash - return empty list
                    android.util.Log.e("NotificationRepo", "Error loading notifications: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(notifications)
            }

        awaitClose { listener.remove() }
    }

    override fun getUnreadNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't crash - return empty list
                    android.util.Log.e("NotificationRepo", "Error loading notifications: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(notifications)
            }

        awaitClose { listener.remove() }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val count = snapshot?.size() ?: 0
                trySend(count)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun markAsRead(notificationId: String): BaseRepository.Result<Unit> {
        return try {
            notificationsCollection
                .document(notificationId)
                .update("isRead", true)
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun markAllAsRead(userId: String): BaseRepository.Result<Unit> {
        return try {
            val unreadNotifications = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unreadNotifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): BaseRepository.Result<Unit> {
        return try {
            notificationsCollection
                .document(notificationId)
                .delete()
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
}
