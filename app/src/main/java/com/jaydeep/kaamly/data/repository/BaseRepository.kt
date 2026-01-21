package com.jaydeep.kaamly.data.repository

/**
 * Base repository interface for common repository operations
 */
interface BaseRepository {
    /**
     * Result wrapper for repository operations
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
        data object Loading : Result<Nothing>()
    }
}
