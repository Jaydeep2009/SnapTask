package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.User
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.data.repository.AuthRepository
import com.jaydeep.kaamly.data.repository.BaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * Check if user is already authenticated
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Sign up a new user
     */
    fun signUp(email: String, password: String, name: String) {
        execute(
            onError = { e -> e.message ?: "Sign up failed" }
        ) {
            when (val result = authRepository.signUp(email, password, name)) {
                is BaseRepository.Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.NeedsRoleSelection(result.data)
                }
                is BaseRepository.Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Sign up failed")
                    throw result.exception
                }
                is BaseRepository.Result.Loading -> {
                    // Loading state handled by BaseViewModel
                }
            }
        }
    }

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        execute(
            onError = { e -> e.message ?: "Login failed" }
        ) {
            when (val result = authRepository.login(email, password)) {
                is BaseRepository.Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is BaseRepository.Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Login failed")
                    throw result.exception
                }
                is BaseRepository.Result.Loading -> {
                    // Loading state handled by BaseViewModel
                }
            }
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        execute(
            onError = { e -> e.message ?: "Logout failed" }
        ) {
            when (val result = authRepository.logout()) {
                is BaseRepository.Result.Success -> {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
                is BaseRepository.Result.Error -> {
                    // Even if logout fails, clear local state
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                    throw result.exception
                }
                is BaseRepository.Result.Loading -> {
                    // Loading state handled by BaseViewModel
                }
            }
        }
    }

    /**
     * Select user role after signup
     */
    fun selectRole(role: UserRole) {
        val user = _currentUser.value
        
        if (user == null) {
            android.util.Log.e("AuthViewModel", "❌ selectRole called but currentUser is null!")
            _authState.value = AuthState.Error("User not found. Please login again.")
            return
        }
        
        android.util.Log.d("AuthViewModel", "Selecting role: $role for user: ${user.id}")
        
        execute(
            onError = { e -> 
                android.util.Log.e("AuthViewModel", "❌ Failed to update role: ${e.message}", e)
                "Failed to update role: ${e.message}"
            }
        ) {
            android.util.Log.d("AuthViewModel", "Calling repository to update role...")
            when (val result = authRepository.updateUserRole(user.id, role)) {
                is BaseRepository.Result.Success -> {
                    android.util.Log.d("AuthViewModel", "✅ Role updated successfully in Firestore")
                    val updatedUser = user.copy(role = role)
                    _currentUser.value = updatedUser
                    _authState.value = AuthState.Authenticated(updatedUser)
                    android.util.Log.d("AuthViewModel", "✅ AuthState updated to Authenticated")
                }
                is BaseRepository.Result.Error -> {
                    android.util.Log.e("AuthViewModel", "❌ Repository returned error: ${result.exception.message}")
                    _authState.value = AuthState.Error("Failed to update role: ${result.exception.message}")
                    throw result.exception
                }
                is BaseRepository.Result.Loading -> {
                    // Loading state handled by BaseViewModel
                }
            }
        }
    }

    /**
     * Switch user role (for users with BOTH role or switching between USER and WORKER)
     */
    fun switchRole(newRole: UserRole) {
        val user = _currentUser.value ?: return
        
        execute(
            onError = { e -> e.message ?: "Failed to switch role" }
        ) {
            when (val result = authRepository.updateUserRole(user.id, newRole)) {
                is BaseRepository.Result.Success -> {
                    val updatedUser = user.copy(role = newRole)
                    _currentUser.value = updatedUser
                    _authState.value = AuthState.Authenticated(updatedUser)
                }
                is BaseRepository.Result.Error -> {
                    _authState.value = AuthState.Error("Failed to switch role")
                    throw result.exception
                }
                is BaseRepository.Result.Loading -> {
                    // Loading state handled by BaseViewModel
                }
            }
        }
    }

    /**
     * Clear authentication error
     */
    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
        clearError()
    }
}

/**
 * Sealed class representing authentication states
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class NeedsRoleSelection(val user: User) : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
