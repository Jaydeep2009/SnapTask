package com.jaydeep.kaamly.util

/**
 * Utility class for formatting user-friendly error messages
 */
object ErrorMessageUtils {
    
    /**
     * Convert Firebase exception to user-friendly message
     */
    fun getFirebaseErrorMessage(exception: Exception): String {
        val message = exception.message ?: return "An unexpected error occurred"
        
        return when {
            // Authentication errors
            message.contains("INVALID_EMAIL", ignoreCase = true) -> 
                "Please enter a valid email address"
            message.contains("WRONG_PASSWORD", ignoreCase = true) -> 
                "Incorrect password. Please try again"
            message.contains("USER_NOT_FOUND", ignoreCase = true) -> 
                "No account found with this email"
            message.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) -> 
                "This email is already registered"
            message.contains("WEAK_PASSWORD", ignoreCase = true) -> 
                "Password is too weak. Use at least 8 characters"
            message.contains("TOO_MANY_REQUESTS", ignoreCase = true) -> 
                "Too many attempts. Please try again later"
            message.contains("NETWORK_ERROR", ignoreCase = true) -> 
                "Network error. Please check your connection"
            message.contains("USER_DISABLED", ignoreCase = true) -> 
                "This account has been disabled"
            
            // Firestore errors
            message.contains("PERMISSION_DENIED", ignoreCase = true) -> 
                "You don't have permission to perform this action"
            message.contains("NOT_FOUND", ignoreCase = true) -> 
                "The requested data was not found"
            message.contains("ALREADY_EXISTS", ignoreCase = true) -> 
                "This item already exists"
            message.contains("UNAVAILABLE", ignoreCase = true) -> 
                "Service temporarily unavailable. Please try again"
            message.contains("DEADLINE_EXCEEDED", ignoreCase = true) -> 
                "Request timed out. Please try again"
            
            // Storage errors
            message.contains("OBJECT_NOT_FOUND", ignoreCase = true) -> 
                "File not found"
            message.contains("UNAUTHORIZED", ignoreCase = true) -> 
                "You don't have permission to access this file"
            message.contains("QUOTA_EXCEEDED", ignoreCase = true) -> 
                "Storage quota exceeded. Please contact support"
            message.contains("RETRY_LIMIT_EXCEEDED", ignoreCase = true) -> 
                "Upload failed. Please try again"
            
            // Network errors
            message.contains("Unable to resolve host", ignoreCase = true) -> 
                "No internet connection. Please check your network"
            message.contains("timeout", ignoreCase = true) -> 
                "Connection timed out. Please try again"
            message.contains("Failed to connect", ignoreCase = true) -> 
                "Connection failed. Please check your internet"
            
            // Default
            else -> "An error occurred. Please try again"
        }
    }
    
    /**
     * Get network error message
     */
    fun getNetworkErrorMessage(): String {
        return "No internet connection. Please check your network and try again."
    }
    
    /**
     * Get generic error message
     */
    fun getGenericErrorMessage(): String {
        return "Something went wrong. Please try again."
    }
    
    /**
     * Get validation error message
     */
    fun getValidationErrorMessage(field: String): String {
        return "$field is required. Please fill in all required fields."
    }
    
    /**
     * Get task creation error message
     */
    fun getTaskCreationErrorMessage(): String {
        return "Failed to create task. Please check your inputs and try again."
    }
    
    /**
     * Get bid placement error message
     */
    fun getBidPlacementErrorMessage(): String {
        return "Failed to place bid. Please try again."
    }
    
    /**
     * Get bid acceptance error message
     */
    fun getBidAcceptanceErrorMessage(): String {
        return "Failed to accept bid. The task may no longer be available."
    }
    
    /**
     * Get review submission error message
     */
    fun getReviewSubmissionErrorMessage(): String {
        return "Failed to submit review. Please try again."
    }
    
    /**
     * Get profile update error message
     */
    fun getProfileUpdateErrorMessage(): String {
        return "Failed to update profile. Please try again."
    }
    
    /**
     * Get photo upload error message
     */
    fun getPhotoUploadErrorMessage(): String {
        return "Failed to upload photo. Please check file size and format."
    }
    
    /**
     * Get task state transition error message
     */
    fun getTaskStateTransitionErrorMessage(): String {
        return "Invalid operation. The task state cannot be changed."
    }
    
    /**
     * Get duplicate bid error message
     */
    fun getDuplicateBidErrorMessage(): String {
        return "You have already placed a bid on this task."
    }
    
    /**
     * Get closed task error message
     */
    fun getClosedTaskErrorMessage(): String {
        return "This task is no longer accepting bids."
    }
    
    /**
     * Get AI generation error message
     */
    fun getAIGenerationErrorMessage(): String {
        return "AI assistant is temporarily unavailable. Please create task manually."
    }
    
    /**
     * Get location permission error message
     */
    fun getLocationPermissionErrorMessage(): String {
        return "Location permission denied. Using city-based search instead."
    }
    
    /**
     * Get empty list message
     */
    fun getEmptyListMessage(itemType: String): String {
        return "No $itemType found. ${getEmptyListAction(itemType)}"
    }
    
    /**
     * Get empty list action suggestion
     */
    private fun getEmptyListAction(itemType: String): String {
        return when (itemType.lowercase()) {
            "tasks" -> "Create your first task to get started."
            "bids" -> "Be the first to bid on this task."
            "reviews" -> "Complete a task to receive reviews."
            "notifications" -> "You're all caught up!"
            else -> "Check back later."
        }
    }
    
    /**
     * Format error for logging (includes technical details)
     */
    fun formatErrorForLogging(exception: Exception, context: String): String {
        return """
            Context: $context
            Error: ${exception.javaClass.simpleName}
            Message: ${exception.message}
            Stack trace: ${exception.stackTraceToString()}
        """.trimIndent()
    }
    
    /**
     * Check if error is network-related
     */
    fun isNetworkError(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("network") ||
                message.contains("internet") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("unable to resolve host")
    }
    
    /**
     * Check if error is authentication-related
     */
    fun isAuthError(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("auth") ||
                message.contains("login") ||
                message.contains("password") ||
                message.contains("email") ||
                message.contains("user")
    }
    
    /**
     * Check if error is permission-related
     */
    fun isPermissionError(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("permission") ||
                message.contains("unauthorized") ||
                message.contains("forbidden")
    }
}
