package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.data.model.WorkerRating
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WorkerRepository for worker profile operations
 */
@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : WorkerRepository {
    
    companion object {
        private const val WORKERS_COLLECTION = "workerProfiles"
        private const val PROFILE_PHOTOS_PATH = "profile_photos"
        private const val AADHAAR_IMAGES_PATH = "aadhaar_images"
    }
    
    override suspend fun createWorkerProfile(profile: WorkerProfile): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(WORKERS_COLLECTION)
                .document(profile.workerId)
                .set(profile)
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun getWorkerProfile(workerId: String): BaseRepository.Result<WorkerProfile> {
        return try {
            val document = firestore.collection(WORKERS_COLLECTION)
                .document(workerId)
                .get()
                .await()
            
            val profile = document.toObject(WorkerProfile::class.java)
            if (profile != null) {
                BaseRepository.Result.Success(profile)
            } else {
                BaseRepository.Result.Error(Exception("Worker profile not found"))
            }
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun updateWorkerProfile(profile: WorkerProfile): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(WORKERS_COLLECTION)
                .document(profile.workerId)
                .set(profile)
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun verifyAadhaar(workerId: String, imageUri: Uri): BaseRepository.Result<Unit> {
        return try {
            val fileName = "aadhaar_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(AADHAAR_IMAGES_PATH)
                .child(workerId)
                .child(fileName)
            
            // Upload the Aadhaar image
            storageRef.putFile(imageUri).await()
            
            // Mock verification delay (simulating processing)
            delay(2000)
            
            // Update worker profile to set aadhaarVerified = true
            val workerResult = getWorkerProfile(workerId)
            if (workerResult is BaseRepository.Result.Success) {
                val updatedProfile = workerResult.data.copy(aadhaarVerified = true)
                updateWorkerProfile(updatedProfile)
            } else {
                BaseRepository.Result.Error(Exception("Failed to update verification status"))
            }
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun getWorkerRating(workerId: String): BaseRepository.Result<WorkerRating> {
        return try {
            val profile = getWorkerProfile(workerId)
            if (profile is BaseRepository.Result.Success) {
                val rating = WorkerRating(
                    overallRating = profile.data.overallRating,
                    totalReviews = profile.data.totalReviews,
                    taskTypeRatings = profile.data.taskTypeRatings,
                    recentReviews = emptyList() // Will be populated by ReviewRepository
                )
                BaseRepository.Result.Success(rating)
            } else {
                BaseRepository.Result.Error(Exception("Worker profile not found"))
            }
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
}
