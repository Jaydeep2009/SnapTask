package com.jaydeep.kaamly.data.model

/**
 * Enum representing the state of a task
 */
enum class TaskState {
    OPEN,         // Task is open for bids
    IN_PROGRESS,  // Worker has been assigned and is working
    COMPLETED,    // Task has been completed
    CANCELLED     // Task was cancelled
}
