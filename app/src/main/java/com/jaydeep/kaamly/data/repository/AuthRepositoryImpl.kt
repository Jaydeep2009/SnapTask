package com.jaydeep.kaamly.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.jaydeep.kaamly.data.model.User
import com.jaydeep.kaamly.data.model.UserRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository using Firebase Authentication and Firestore
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): BaseRepository.Result<User> {
        return try {
            // Create user with Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return BaseRepository.Result.Error(Exception("Failed to create user"))

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                role = UserRole.USER, // Default role
                createdAt = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()

            BaseRepository.Result.Success(user)
        } catch (e: FirebaseAuthException) {
            BaseRepository.Result.Error(Exception(getAuthErrorMessage(e)))
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): BaseRepository.Result<User> {
        return try {
            // Sign in with Firebase Auth
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return BaseRepository.Result.Error(Exception("Failed to login"))

            // Fetch user data from Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: return BaseRepository.Result.Error(Exception("User data not found"))

            BaseRepository.Result.Success(user)
        } catch (e: FirebaseAuthException) {
            BaseRepository.Result.Error(Exception(getAuthErrorMessage(e)))
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun logout(): BaseRepository.Result<Unit> {
        return try {
            firebaseAuth.signOut()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return null

            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserRole(
        userId: String,
        role: UserRole
    ): BaseRepository.Result<Unit> {
        return try {
            android.util.Log.d("AuthRepository", "Updating role for user: $userId to: $role")
            
            // Update the role field in Firestore
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("role", role)
                .await()

            android.util.Log.d("AuthRepository", "✅ Role updated successfully in Firestore")
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "❌ Failed to update role: ${e.message}", e)
            BaseRepository.Result.Error(e)
        }
    }

    /**
     * Convert Firebase Auth error codes to user-friendly messages
     */
    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address"
            "ERROR_WRONG_PASSWORD" -> "Invalid email or password"
            "ERROR_USER_NOT_FOUND" -> "Invalid email or password"
            "ERROR_USER_DISABLED" -> "This account has been disabled"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection"
            else -> exception.message ?: "Authentication failed"
        }
    }
}
