package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.data.model.WorkerRating

/**
 * Repository interface for worker profile operations
 */
interface WorkerRepository {
    /**
     * Create a new worker profile
     */
    suspend fun createWorkerProfile(profile: WorkerProfile): BaseRepository.Result<Unit>
    
    /**
     * Get worker profile by ID
     */
    suspend fun getWorkerProfile(workerId: String): BaseRepository.Result<WorkerProfile>
    
    /**
     * Update worker profile
     */
    suspend fun updateWorkerProfile(profile: WorkerProfile): BaseRepository.Result<Unit>
    
    /**
     * Verify Aadhaar (mock implementation)
     */
    suspend fun verifyAadhaar(workerId: String, imageUri: Uri): BaseRepository.Result<Unit>
    
    /**
     * Get worker rating and reviews
     */
    suspend fun getWorkerRating(workerId: String): BaseRepository.Result<WorkerRating>
}
