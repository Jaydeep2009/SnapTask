package com.jaydeep.kaamly.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.UserProfile
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.UserRepository
import com.jaydeep.kaamly.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user and worker profiles
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workerRepository: WorkerRepository
) : BaseViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _workerProfile = MutableStateFlow<WorkerProfile?>(null)
    val workerProfile: StateFlow<WorkerProfile?> = _workerProfile.asStateFlow()
    
    private val _photoUploadProgress = MutableStateFlow(false)
    val photoUploadProgress: StateFlow<Boolean> = _photoUploadProgress.asStateFlow()
    
    private val _verificationProgress = MutableStateFlow(false)
    val verificationProgress: StateFlow<Boolean> = _verificationProgress.asStateFlow()
    
    private val _profileUpdateSuccess = MutableStateFlow(false)
    val profileUpdateSuccess: StateFlow<Boolean> = _profileUpdateSuccess.asStateFlow()
    
    /**
     * Load user profile by ID
     */
    fun loadUserProfile(userId: String) {
        execute {
            when (val result = userRepository.getUserProfile(userId)) {
                is BaseRepository.Result.Success -> {
                    _userProfile.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }
    
    /**
     * Load worker profile by ID
     */
    fun loadWorkerProfile(workerId: String) {
        execute {
            when (val result = workerRepository.getWorkerProfile(workerId)) {
                is BaseRepository.Result.Success -> {
                    _workerProfile.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }
    
    /**
     * Create or update user profile
     */
    fun updateUserProfile(profile: UserProfile) {
        execute {
            _profileUpdateSuccess.value = false
            when (val result = userRepository.updateUserProfile(profile)) {
                is BaseRepository.Result.Success -> {
                    _userProfile.value = profile
                    _profileUpdateSuccess.value = true
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }
    
    /**
     * Create or update worker profile
     */
    fun updateWorkerProfile(profile: WorkerProfile) {
        execute {
            _profileUpdateSuccess.value = false
            when (val result = workerRepository.updateWorkerProfile(profile)) {
                is BaseRepository.Result.Success -> {
                    _workerProfile.value = profile
                    _profileUpdateSuccess.value = true
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }
    
    /**
     * Upload profile photo for user
     */
    fun uploadUserPhoto(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _photoUploadProgress.value = true
                when (val result = userRepository.uploadProfilePhoto(userId, imageUri)) {
                    is BaseRepository.Result.Success -> {
                        // Update the profile with new photo URL
                        _userProfile.value?.let { profile ->
                            val updatedProfile = profile.copy(profilePhotoUrl = result.data)
                            updateUserProfile(updatedProfile)
                        }
                    }
                    is BaseRepository.Result.Error -> {
                        throw result.exception
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Error handling is done in BaseViewModel
            } finally {
                _photoUploadProgress.value = false
            }
        }
    }
    
    /**
     * Upload profile photo for worker
     */
    fun uploadWorkerPhoto(workerId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _photoUploadProgress.value = true
                // Reuse user repository's photo upload (same storage path structure)
                when (val result = userRepository.uploadProfilePhoto(workerId, imageUri)) {
                    is BaseRepository.Result.Success -> {
                        // Update the profile with new photo URL
                        _workerProfile.value?.let { profile ->
                            val updatedProfile = profile.copy(profilePhotoUrl = result.data)
                            updateWorkerProfile(updatedProfile)
                        }
                    }
                    is BaseRepository.Result.Error -> {
                        throw result.exception
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Error handling is done in BaseViewModel
            } finally {
                _photoUploadProgress.value = false
            }
        }
    }
    
    /**
     * Verify Aadhaar for worker (mock implementation)
     */
    fun verifyAadhaar(workerId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                _verificationProgress.value = true
                when (val result = workerRepository.verifyAadhaar(workerId, imageUri)) {
                    is BaseRepository.Result.Success -> {
                        // Reload worker profile to get updated verification status
                        loadWorkerProfile(workerId)
                    }
                    is BaseRepository.Result.Error -> {
                        throw result.exception
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Error handling is done in BaseViewModel
            } finally {
                _verificationProgress.value = false
            }
        }
    }
    
    /**
     * Reset profile update success flag
     */
    fun resetProfileUpdateSuccess() {
        _profileUpdateSuccess.value = false
    }
}
