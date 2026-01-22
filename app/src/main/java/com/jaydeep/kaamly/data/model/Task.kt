package com.jaydeep.kaamly.data.model

/**
 * Data class representing a task
 */
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val requiredEquipment: List<Equipment> = emptyList(),
    val location: Location = Location(),
    val scheduledDate: Long = 0L,
    val scheduledTime: String = "",
    val budget: Double = 0.0,
    val acceptedBidAmount: Double? = null, // The actual bid amount that was accepted
    val isInstantJob: Boolean = false,
    val state: TaskState = TaskState.OPEN,
    val assignedWorkerId: String? = null,
    val completionPhotoUrl: String? = null,
    val workerArrived: Boolean = false,
    val completionRequested: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
