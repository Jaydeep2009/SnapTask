package com.jaydeep.kaamly.data.model

/**
 * Data class representing worker rating and reviews
 */
data class WorkerRating(
    val overallRating: Double = 0.0,
    val totalReviews: Int = 0,
    val taskTypeRatings: Map<TaskCategory, Double> = emptyMap(),
    val recentReviews: List<Review> = emptyList()
)
