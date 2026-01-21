package com.jaydeep.kaamly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jaydeep.kaamly.data.model.Bid
import com.jaydeep.kaamly.data.model.BidStatus
import com.jaydeep.kaamly.data.model.TaskState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BidRepository using Firebase Firestore
 */
@Singleton
class BidRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BidRepository {

    companion object {
        private const val BIDS_COLLECTION = "bids"
        private const val TASKS_COLLECTION = "tasks"
    }

    override suspend fun placeBid(bid: Bid): BaseRepository.Result<String> {
        return try {
            // First, check if the task is still open
            val taskDoc = firestore.collection(TASKS_COLLECTION)
                .document(bid.taskId)
                .get()
                .await()
            
            val taskState = taskDoc.getString("state")
            if (taskState != TaskState.OPEN.name) {
                return BaseRepository.Result.Error(
                    Exception("This task is no longer accepting bids")
                )
            }
            
            // Check if worker has already placed a bid on this task
            val existingBids = firestore.collection(BIDS_COLLECTION)
                .whereEqualTo("taskId", bid.taskId)
                .whereEqualTo("workerId", bid.workerId)
                .get()
                .await()
            
            if (!existingBids.isEmpty) {
                return BaseRepository.Result.Error(
                    Exception("You have already placed a bid on this task")
                )
            }
            
            // Create the bid
            val bidRef = firestore.collection(BIDS_COLLECTION).document()
            val bidWithId = bid.copy(
                id = bidRef.id,
                status = BidStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
            bidRef.set(bidWithId).await()
            
            BaseRepository.Result.Success(bidRef.id)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun getBidsForTask(taskId: String): Flow<List<Bid>> = callbackFlow {
        val listener = firestore.collection(BIDS_COLLECTION)
            .whereEqualTo("taskId", taskId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BidRepository", "Error loading bids for task", error)
                    // Don't close the flow, just send empty list
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val bids = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Bid::class.java)
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
                trySend(bids)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun acceptBid(bidId: String): BaseRepository.Result<Unit> {
        return try {
            // Get the bid
            val bidDoc = firestore.collection(BIDS_COLLECTION)
                .document(bidId)
                .get()
                .await()
            
            val bid = bidDoc.toObject(Bid::class.java)
                ?: return BaseRepository.Result.Error(Exception("Bid not found"))
            
            // Check if task is still open
            val taskDoc = firestore.collection(TASKS_COLLECTION)
                .document(bid.taskId)
                .get()
                .await()
            
            val taskState = taskDoc.getString("state")
            if (taskState != TaskState.OPEN.name) {
                return BaseRepository.Result.Error(
                    Exception("This task is no longer accepting bids")
                )
            }
            
            // Use a batch write to ensure atomicity
            val batch = firestore.batch()
            
            // Update bid status to ACCEPTED
            val bidRef = firestore.collection(BIDS_COLLECTION).document(bidId)
            batch.update(bidRef, "status", BidStatus.ACCEPTED.name)
            
            // Update task: assign worker and change state to IN_PROGRESS
            val taskRef = firestore.collection(TASKS_COLLECTION).document(bid.taskId)
            batch.update(
                taskRef,
                mapOf(
                    "assignedWorkerId" to bid.workerId,
                    "state" to TaskState.IN_PROGRESS.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            
            // Reject all other bids for this task
            val otherBids = firestore.collection(BIDS_COLLECTION)
                .whereEqualTo("taskId", bid.taskId)
                .whereEqualTo("status", BidStatus.PENDING.name)
                .get()
                .await()
            
            otherBids.documents.forEach { doc ->
                if (doc.id != bidId) {
                    batch.update(doc.reference, "status", BidStatus.REJECTED.name)
                }
            }
            
            // Commit the batch
            batch.commit().await()
            
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun getWorkerBids(workerId: String): Flow<List<Bid>> = callbackFlow {
        val listener = firestore.collection(BIDS_COLLECTION)
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("BidRepository", "Error loading worker bids", error)
                    // Don't close the flow, just send empty list
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val bids = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Bid::class.java)
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
                trySend(bids)
            }
        
        awaitClose { listener.remove() }
    }
}
