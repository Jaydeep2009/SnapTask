package com.jaydeep.kaamly.util

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utility class for input validation
 */
object ValidationUtils {
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate password strength
     * Requirements: At least 8 characters
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
    
    /**
     * Get password strength message
     */
    fun getPasswordStrengthMessage(password: String): String {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            password.length < 12 -> "Password is acceptable"
            else -> "Password is strong"
        }
    }
    
    /**
     * Validate phone number format (Indian format)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isBlank()) return false
        
        // Remove spaces, dashes, and parentheses
        val cleanPhone = phone.replace(Regex("[\\s\\-()]"), "")
        
        // Check for Indian phone number format
        // +91XXXXXXXXXX or 91XXXXXXXXXX or XXXXXXXXXX (10 digits)
        val pattern = Pattern.compile("^(\\+91|91)?[6-9]\\d{9}$")
        return pattern.matcher(cleanPhone).matches()
    }
    
    /**
     * Validate task title
     */
    fun isValidTaskTitle(title: String): Boolean {
        return title.isNotBlank() && title.length in 5..100
    }
    
    /**
     * Get task title validation message
     */
    fun getTaskTitleValidationMessage(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length < 5 -> "Title must be at least 5 characters"
            title.length > 100 -> "Title must be less than 100 characters"
            else -> null
        }
    }
    
    /**
     * Validate task description
     */
    fun isValidTaskDescription(description: String): Boolean {
        return description.isNotBlank() && description.length in 20..1000
    }
    
    /**
     * Get task description validation message
     */
    fun getTaskDescriptionValidationMessage(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 20 -> "Description must be at least 20 characters"
            description.length > 1000 -> "Description must be less than 1000 characters"
            else -> null
        }
    }
    
    /**
     * Validate budget amount
     */
    fun isValidBudget(budget: Double): Boolean {
        return budget > 0 && budget <= 100000
    }
    
    /**
     * Get budget validation message
     */
    fun getBudgetValidationMessage(budget: Double): String? {
        return when {
            budget <= 0 -> "Budget must be greater than 0"
            budget > 100000 -> "Budget must be less than ₹1,00,000"
            else -> null
        }
    }
    
    /**
     * Validate bid amount
     */
    fun isValidBidAmount(amount: Double, taskBudget: Double): Boolean {
        return amount > 0 && amount <= taskBudget * 1.5 // Allow up to 150% of task budget
    }
    
    /**
     * Get bid amount validation message
     */
    fun getBidAmountValidationMessage(amount: Double, taskBudget: Double): String? {
        return when {
            amount <= 0 -> "Bid amount must be greater than 0"
            amount > taskBudget * 1.5 -> "Bid amount is too high (max ${taskBudget * 1.5})"
            else -> null
        }
    }
    
    /**
     * Validate review rating
     */
    fun isValidRating(rating: Int): Boolean {
        return rating in 1..5
    }
    
    /**
     * Validate review text
     */
    fun isValidReviewText(text: String): Boolean {
        return text.isNotBlank() && text.length in 10..500
    }
    
    /**
     * Get review text validation message
     */
    fun getReviewTextValidationMessage(text: String): String? {
        return when {
            text.isBlank() -> "Review text is required"
            text.length < 10 -> "Review must be at least 10 characters"
            text.length > 500 -> "Review must be less than 500 characters"
            else -> null
        }
    }
    
    /**
     * Validate city name
     */
    fun isValidCity(city: String): Boolean {
        return city.isNotBlank() && city.length in 2..50
    }
    
    /**
     * Validate date is in future
     */
    fun isValidFutureDate(dateMillis: Long): Boolean {
        return dateMillis > System.currentTimeMillis()
    }
    
    /**
     * Sanitize user input to prevent injection
     */
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[<>\"']"), "") // Remove special characters
    }
    
    /**
     * Validate image file size (max 5MB)
     */
    fun isValidImageSize(sizeInBytes: Long): Boolean {
        val maxSizeInBytes = 5 * 1024 * 1024 // 5MB
        return sizeInBytes <= maxSizeInBytes
    }
    
    /**
     * Get image size validation message
     */
    fun getImageSizeValidationMessage(sizeInBytes: Long): String? {
        val maxSizeInBytes = 5 * 1024 * 1024 // 5MB
        return if (sizeInBytes > maxSizeInBytes) {
            "Image must be less than 5MB"
        } else {
            null
        }
    }
    
    /**
     * Format file size for display
     */
    fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> "${sizeInBytes / (1024 * 1024)} MB"
        }
    }
    
    /**
     * Validate equipment name
     */
    fun isValidEquipmentName(name: String): Boolean {
        return name.isNotBlank() && name.length in 2..50
    }
    
    /**
     * Validate worker skills
     */
    fun isValidSkills(skills: List<String>): Boolean {
        return skills.isNotEmpty() && skills.all { it.isNotBlank() && it.length in 2..30 }
    }
    
    /**
     * Validate worker bio
     */
    fun isValidBio(bio: String): Boolean {
        return bio.isNotBlank() && bio.length in 20..500
    }
    
    /**
     * Get bio validation message
     */
    fun getBioValidationMessage(bio: String): String? {
        return when {
            bio.isBlank() -> "Bio is required"
            bio.length < 20 -> "Bio must be at least 20 characters"
            bio.length > 500 -> "Bio must be less than 500 characters"
            else -> null
        }
    }
}
