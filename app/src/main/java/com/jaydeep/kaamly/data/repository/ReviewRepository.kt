package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.Review
import com.jaydeep.kaamly.data.model.WorkerRating
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for review operations
 */
interface ReviewRepository {
    /**
     * Submit a review for a worker
     * @param review The review to submit
     * @return Result indicating success or failure
     */
    suspend fun submitReview(review: Review): BaseRepository.Result<Unit>

    /**
     * Get all reviews for a specific worker
     * @param workerId The worker ID
     * @return Flow of review list
     */
    fun getWorkerReviews(workerId: String): Flow<List<Review>>

    /**
     * Calculate worker rating based on all reviews
     * @param workerId The worker ID
     * @return Result containing the worker rating
     */
    suspend fun calculateWorkerRating(workerId: String): BaseRepository.Result<WorkerRating>
}
