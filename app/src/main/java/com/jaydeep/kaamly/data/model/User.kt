package com.jaydeep.kaamly.data.model

/**
 * Data class representing a user in the system
 */
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis()
)
