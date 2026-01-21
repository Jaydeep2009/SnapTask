package com.jaydeep.kaamly.data.model

/**
 * Data class representing a review
 */
data class Review(
    val id: String = "",
    val taskId: String = "",
    val workerId: String = "",
    val userId: String = "",
    val starRating: Int = 0,
    val textReview: String = "",
    val taskSpecificRating: Map<String, Int> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
