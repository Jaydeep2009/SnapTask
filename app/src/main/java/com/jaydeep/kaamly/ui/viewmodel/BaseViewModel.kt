package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Base ViewModel with common functionality for all ViewModels
 */
abstract class BaseViewModel : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<ErrorState?>(null)
    val error: StateFlow<ErrorState?> = _error.asStateFlow()
    
    /**
     * Execute a suspend function with loading and error handling
     */
    protected fun <T> execute(
        onError: (Exception) -> String = { it.message ?: "An error occurred" },
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                block()
            } catch (e: Exception) {
                _error.value = mapExceptionToErrorState(e, onError)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Execute with retry logic for network errors
     */
    protected fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 5000L,
        factor: Double = 2.0,
        onError: (Exception) -> String = { it.message ?: "An error occurred" },
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            var currentDelay = initialDelay
            var lastException: Exception? = null
            
            try {
                _isLoading.value = true
                _error.value = null
                
                repeat(maxRetries) { attempt ->
                    try {
                        block()
                        return@launch // Success, exit
                    } catch (e: Exception) {
                        lastException = e
                        
                        // Only retry on network errors
                        if (isNetworkError(e) && attempt < maxRetries - 1) {
                            delay(currentDelay)
                            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                        } else {
                            throw e
                        }
                    }
                }
                
                // If we get here, all retries failed
                lastException?.let { throw it }
                
            } catch (e: Exception) {
                _error.value = mapExceptionToErrorState(e, onError)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Check if exception is a network error
     */
    private fun isNetworkError(exception: Exception): Boolean {
        return exception is IOException ||
               exception is SocketTimeoutException ||
               exception is UnknownHostException ||
               exception.message?.contains("network", ignoreCase = true) == true ||
               exception.message?.contains("connection", ignoreCase = true) == true
    }
    
    /**
     * Map exception to ErrorState
     */
    private fun mapExceptionToErrorState(
        exception: Exception,
        onError: (Exception) -> String
    ): ErrorState {
        return when {
            isNetworkError(exception) -> ErrorState.NetworkError(
                message = "Connection error. Please check your internet connection.",
                exception = exception
            )
            exception.message?.contains("permission", ignoreCase = true) == true -> ErrorState.PermissionError(
                message = "Permission denied. Please check your permissions.",
                exception = exception
            )
            exception.message?.contains("not found", ignoreCase = true) == true -> ErrorState.NotFoundError(
                message = "The requested resource was not found.",
                exception = exception
            )
            exception.message?.contains("unauthorized", ignoreCase = true) == true ||
            exception.message?.contains("authentication", ignoreCase = true) == true -> ErrorState.AuthenticationError(
                message = "Authentication failed. Please login again.",
                exception = exception
            )
            exception.message?.contains("validation", ignoreCase = true) == true -> ErrorState.ValidationError(
                message = onError(exception),
                exception = exception
            )
            else -> ErrorState.GenericError(
                message = onError(exception),
                exception = exception
            )
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Retry the last failed operation
     */
    fun retry(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                operation()
            } catch (e: Exception) {
                _error.value = mapExceptionToErrorState(e) { it.message ?: "An error occurred" }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * Sealed class representing different error states
 */
sealed class ErrorState(
    open val message: String,
    open val exception: Exception
) {
    data class NetworkError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    data class AuthenticationError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    data class PermissionError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    data class ValidationError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    data class NotFoundError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    data class GenericError(
        override val message: String,
        override val exception: Exception
    ) : ErrorState(message, exception)
    
    /**
     * Check if this error is retryable
     */
    fun isRetryable(): Boolean {
        return this is NetworkError
    }
}
