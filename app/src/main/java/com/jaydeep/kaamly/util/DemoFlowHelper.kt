package com.jaydeep.kaamly.util

import com.jaydeep.kaamly.data.model.*

/**
 * Helper class for demo flow optimization
 * Provides utilities for quick demo data generation and flow validation
 */
object DemoFlowHelper {
    
    /**
     * Generate demo task data for quick form filling
     */
    fun getDemoTaskData(): DemoTaskData {
        return DemoTaskData(
            title = "Fix Leaking Bathroom Tap",
            description = "My bathroom tap has been leaking for a few days. Need someone to fix it urgently. The tap is a standard mixer tap and water is dripping constantly.",
            category = TaskCategory.PLUMBING,
            equipment = listOf(
                Equipment("Wrench", EquipmentProvider.WORKER),
                Equipment("Plumber's tape", EquipmentProvider.WORKER)
            ),
            city = "Mumbai",
            address = "Andheri West, Mumbai",
            scheduledDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
            scheduledTime = "10:00 AM",
            budget = 500.0,
            isInstantJob = false
        )
    }
    
    /**
     * Generate alternative demo task data
     */
    fun getAlternativeDemoTaskData(): DemoTaskData {
        return DemoTaskData(
            title = "Deep Clean 2BHK Apartment",
            description = "Need thorough cleaning of my 2BHK apartment including kitchen, bathrooms, and all rooms. Dusting, mopping, and bathroom cleaning required.",
            category = TaskCategory.CLEANING,
            equipment = listOf(
                Equipment("Cleaning supplies", EquipmentProvider.WORKER),
                Equipment("Vacuum cleaner", EquipmentProvider.USER)
            ),
            city = "Mumbai",
            address = "Bandra East, Mumbai",
            scheduledDate = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L),
            scheduledTime = "9:00 AM",
            budget = 1500.0,
            isInstantJob = false
        )
    }
    
    /**
     * Generate demo bid data
     */
    fun getDemoBidData(): DemoBidData {
        return DemoBidData(
            amount = 450.0,
            message = "I can fix this today! I have 5+ years experience with plumbing repairs. Will bring all necessary tools."
        )
    }
    
    /**
     * Generate demo review data
     */
    fun getDemoReviewData(): DemoReviewData {
        return DemoReviewData(
            starRating = 5,
            textReview = "Excellent work! Fixed the tap quickly and professionally. Very satisfied with the service.",
            taskSpecificRatings = mapOf(
                "punctuality" to 5,
                "quality" to 5,
                "communication" to 5
            )
        )
    }
    
    /**
     * Validate demo flow completion
     */
    fun validateDemoFlow(
        userCreated: Boolean,
        taskCreated: Boolean,
        roleSwitched: Boolean,
        bidPlaced: Boolean,
        bidAccepted: Boolean,
        taskCompleted: Boolean,
        reviewSubmitted: Boolean
    ): DemoFlowValidation {
        val completedSteps = listOf(
            userCreated,
            taskCreated,
            roleSwitched,
            bidPlaced,
            bidAccepted,
            taskCompleted,
            reviewSubmitted
        ).count { it }
        
        val totalSteps = 7
        val progress = (completedSteps.toFloat() / totalSteps.toFloat()) * 100
        
        return DemoFlowValidation(
            isComplete = completedSteps == totalSteps,
            completedSteps = completedSteps,
            totalSteps = totalSteps,
            progress = progress,
            nextStep = getNextStep(
                userCreated,
                taskCreated,
                roleSwitched,
                bidPlaced,
                bidAccepted,
                taskCompleted,
                reviewSubmitted
            )
        )
    }
    
    /**
     * Get next step in demo flow
     */
    private fun getNextStep(
        userCreated: Boolean,
        taskCreated: Boolean,
        roleSwitched: Boolean,
        bidPlaced: Boolean,
        bidAccepted: Boolean,
        taskCompleted: Boolean,
        reviewSubmitted: Boolean
    ): String {
        return when {
            !userCreated -> "Sign up or login"
            !taskCreated -> "Create a task"
            !roleSwitched -> "Switch to worker role"
            !bidPlaced -> "Place a bid on the task"
            !bidAccepted -> "Switch back to user and accept the bid"
            !taskCompleted -> "Complete the task as worker"
            !reviewSubmitted -> "Submit a review"
            else -> "Demo flow complete!"
        }
    }
    
    /**
     * Get demo flow step labels
     */
    fun getDemoFlowSteps(): List<String> {
        return listOf(
            "Sign up / Login",
            "Create task",
            "Switch to worker role",
            "View and bid on task",
            "Switch back to user",
            "Accept bid and complete task",
            "Leave review"
        )
    }
    
    /**
     * Calculate estimated time remaining for demo
     */
    fun getEstimatedTimeRemaining(completedSteps: Int): String {
        val timePerStep = 17 // seconds (120 seconds / 7 steps ≈ 17 seconds)
        val remainingSteps = 7 - completedSteps
        val remainingSeconds = remainingSteps * timePerStep
        
        return when {
            remainingSeconds <= 0 -> "Complete!"
            remainingSeconds < 60 -> "$remainingSeconds seconds"
            else -> {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                "${minutes}m ${seconds}s"
            }
        }
    }
}

/**
 * Data class for demo task data
 */
data class DemoTaskData(
    val title: String,
    val description: String,
    val category: TaskCategory,
    val equipment: List<Equipment>,
    val city: String,
    val address: String,
    val scheduledDate: Long,
    val scheduledTime: String,
    val budget: Double,
    val isInstantJob: Boolean
)

/**
 * Data class for demo bid data
 */
data class DemoBidData(
    val amount: Double,
    val message: String
)

/**
 * Data class for demo review data
 */
data class DemoReviewData(
    val starRating: Int,
    val textReview: String,
    val taskSpecificRatings: Map<String, Int>
)

/**
 * Data class for demo flow validation result
 */
data class DemoFlowValidation(
    val isComplete: Boolean,
    val completedSteps: Int,
    val totalSteps: Int,
    val progress: Float,
    val nextStep: String
)
