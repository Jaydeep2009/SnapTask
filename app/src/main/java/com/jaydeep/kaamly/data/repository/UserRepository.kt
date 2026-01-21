package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.jaydeep.kaamly.data.model.UserProfile

/**
 * Repository interface for user profile operations
 */
interface UserRepository {
    /**
     * Create a new user profile
     */
    suspend fun createUserProfile(profile: UserProfile): BaseRepository.Result<Unit>
    
    /**
     * Get user profile by ID
     */
    suspend fun getUserProfile(userId: String): BaseRepository.Result<UserProfile>
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(profile: UserProfile): BaseRepository.Result<Unit>
    
    /**
     * Upload profile photo and return the download URL
     */
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): BaseRepository.Result<String>
}
