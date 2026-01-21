package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jaydeep.kaamly.data.model.UserProfile
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository for user profile operations
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {
    
    companion object {
        private const val USERS_COLLECTION = "userProfiles"
        private const val PROFILE_PHOTOS_PATH = "profile_photos"
    }
    
    override suspend fun createUserProfile(profile: UserProfile): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(profile.userId)
                .set(profile)
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun getUserProfile(userId: String): BaseRepository.Result<UserProfile> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val profile = document.toObject(UserProfile::class.java)
            if (profile != null) {
                BaseRepository.Result.Success(profile)
            } else {
                BaseRepository.Result.Error(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun updateUserProfile(profile: UserProfile): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(profile.userId)
                .set(profile)
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
    
    override suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): BaseRepository.Result<String> {
        return try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(PROFILE_PHOTOS_PATH)
                .child(userId)
                .child(fileName)
            
            // Upload the file
            storageRef.putFile(imageUri).await()
            
            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await()
            BaseRepository.Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
}
