package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.User
import com.jaydeep.kaamly.data.model.UserRole

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Sign up a new user with email and password
     */
    suspend fun signUp(email: String, password: String, name: String): BaseRepository.Result<User>
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): BaseRepository.Result<User>
    
    /**
     * Logout current user
     */
    suspend fun logout(): BaseRepository.Result<Unit>
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Update user role
     */
    suspend fun updateUserRole(userId: String, role: UserRole): BaseRepository.Result<Unit>
}
