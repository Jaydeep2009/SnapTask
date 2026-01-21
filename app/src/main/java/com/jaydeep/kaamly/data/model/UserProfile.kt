package com.jaydeep.kaamly.data.model

/**
 * Data class representing a user profile
 */
data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val city: String = "",
    val profilePhotoUrl: String? = null,
    val aadhaarVerified: Boolean = false,
    val phoneNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
