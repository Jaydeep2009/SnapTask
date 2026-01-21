package com.jaydeep.kaamly.data.model

/**
 * Data class representing a worker profile
 */
data class WorkerProfile(
    val workerId: String = "",
    val name: String = "",
    val city: String = "",
    val profilePhotoUrl: String? = null,
    val skills: List<String> = emptyList(),
    val bio: String = "",
    val overallRating: Double = 0.0,
    val totalReviews: Int = 0,
    val taskTypeRatings: Map<TaskCategory, Double> = emptyMap(),
    val aadhaarVerified: Boolean = false,
    val phoneNumber: String? = null,
    val walletBalance: Double = 0.0, // Mock wallet balance
    val createdAt: Long = System.currentTimeMillis()
)
