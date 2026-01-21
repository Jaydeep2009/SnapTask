package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.Review
import com.jaydeep.kaamly.data.model.WorkerRating
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for review-related operations
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : BaseViewModel() {

    // Worker reviews
    private val _workerReviews = MutableStateFlow<List<Review>>(emptyList())
    val workerReviews: StateFlow<List<Review>> = _workerReviews.asStateFlow()

    // Worker rating
    private val _workerRating = MutableStateFlow<WorkerRating?>(null)
    val workerRating: StateFlow<WorkerRating?> = _workerRating.asStateFlow()

    // Review submission success
    private val _reviewSubmitted = MutableStateFlow(false)
    val reviewSubmitted: StateFlow<Boolean> = _reviewSubmitted.asStateFlow()

    /**
     * Submit a review for a worker
     * @param taskId The task ID
     * @param workerId The worker ID
     * @param userId The user ID
     * @param starRating The star rating (1-5)
     * @param textReview The text review
     * @param taskSpecificRating Task-specific ratings (e.g., punctuality, quality)
     */
    fun submitReview(
        taskId: String,
        workerId: String,
        userId: String,
        starRating: Int,
        textReview: String,
        taskSpecificRating: Map<String, Int>
    ) {
        execute(
            onError = { e -> "Failed to submit review: ${e.message}" }
        ) {
            val review = Review(
                taskId = taskId,
                workerId = workerId,
                userId = userId,
                starRating = starRating,
                textReview = textReview,
                taskSpecificRating = taskSpecificRating,
                timestamp = System.currentTimeMillis()
            )

            when (val result = reviewRepository.submitReview(review)) {
                is BaseRepository.Result.Success -> {
                    _reviewSubmitted.value = true
                    // Reload worker rating to get updated values
                    loadWorkerRating(workerId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Load all reviews for a worker
     * @param workerId The worker ID
     */
    fun loadWorkerReviews(workerId: String) {
        viewModelScope.launch {
            reviewRepository.getWorkerReviews(workerId).collect { reviews ->
                _workerReviews.value = reviews
            }
        }
    }

    /**
     * Load worker rating
     * @param workerId The worker ID
     */
    fun loadWorkerRating(workerId: String) {
        execute(
            onError = { e -> "Failed to load worker rating: ${e.message}" }
        ) {
            when (val result = reviewRepository.calculateWorkerRating(workerId)) {
                is BaseRepository.Result.Success -> {
                    _workerRating.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Clear review submitted state
     */
    fun clearReviewSubmitted() {
        _reviewSubmitted.value = false
    }

    /**
     * Clear worker rating
     */
    fun clearWorkerRating() {
        _workerRating.value = null
    }

    /**
     * Clear worker reviews
     */
    fun clearWorkerReviews() {
        _workerReviews.value = emptyList()
    }
}
