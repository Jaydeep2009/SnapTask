package com.jaydeep.kaamly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jaydeep.kaamly.data.model.Review
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.WorkerRating
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ReviewRepository using Firebase Firestore
 */
@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    companion object {
        private const val REVIEWS_COLLECTION = "reviews"
        private const val TASKS_COLLECTION = "tasks"
        private const val WORKER_PROFILES_COLLECTION = "workerProfiles"
    }

    override suspend fun submitReview(review: Review): BaseRepository.Result<Unit> {
        return try {
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document()
            val reviewWithId = review.copy(
                id = reviewRef.id,
                timestamp = System.currentTimeMillis()
            )
            
            // Submit the review
            reviewRef.set(reviewWithId).await()
            
            // Update worker's overall rating
            updateWorkerRating(review.workerId)
            
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override fun getWorkerReviews(workerId: String): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection(REVIEWS_COLLECTION)
            .whereEqualTo("workerId", workerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)
                } ?: emptyList()
                
                trySend(reviews)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun calculateWorkerRating(workerId: String): BaseRepository.Result<WorkerRating> {
        return try {
            // Get all reviews for the worker
            val reviewsSnapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("workerId", workerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)
            }
            
            if (reviews.isEmpty()) {
                return BaseRepository.Result.Success(
                    WorkerRating(
                        overallRating = 0.0,
                        totalReviews = 0,
                        taskTypeRatings = emptyMap(),
                        recentReviews = emptyList()
                    )
                )
            }
            
            // Calculate overall rating
            val overallRating = reviews.map { it.starRating }.average()
            
            // Calculate task-type specific ratings
            val taskTypeRatings = mutableMapOf<TaskCategory, Double>()
            
            // Get task categories for each review
            for (review in reviews) {
                try {
                    val taskSnapshot = firestore.collection(TASKS_COLLECTION)
                        .document(review.taskId)
                        .get()
                        .await()
                    
                    val categoryString = taskSnapshot.getString("category")
                    if (categoryString != null) {
                        val category = TaskCategory.valueOf(categoryString)
                        val categoryReviews = reviews.filter { r ->
                            try {
                                val ts = firestore.collection(TASKS_COLLECTION)
                                    .document(r.taskId)
                                    .get()
                                    .await()
                                ts.getString("category") == categoryString
                            } catch (e: Exception) {
                                false
                            }
                        }
                        
                        if (categoryReviews.isNotEmpty()) {
                            taskTypeRatings[category] = categoryReviews.map { it.starRating }.average()
                        }
                    }
                } catch (e: Exception) {
                    // Skip if task not found
                    continue
                }
            }
            
            // Get recent reviews (limit to 10)
            val recentReviews = reviews.take(10)
            
            val workerRating = WorkerRating(
                overallRating = overallRating,
                totalReviews = reviews.size,
                taskTypeRatings = taskTypeRatings,
                recentReviews = recentReviews
            )
            
            BaseRepository.Result.Success(workerRating)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    /**
     * Update worker's overall rating in their profile
     */
    private suspend fun updateWorkerRating(workerId: String) {
        try {
            val ratingResult = calculateWorkerRating(workerId)
            if (ratingResult is BaseRepository.Result.Success) {
                val rating = ratingResult.data
                firestore.collection(WORKER_PROFILES_COLLECTION)
                    .document(workerId)
                    .update(
                        mapOf(
                            "overallRating" to rating.overallRating,
                            "totalReviews" to rating.totalReviews,
                            "taskTypeRatings" to rating.taskTypeRatings.mapKeys { it.key.name }
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the review submission
            e.printStackTrace()
        }
    }
}
