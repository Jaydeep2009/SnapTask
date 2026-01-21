package com.jaydeep.kaamly.data.model

/**
 * Data class representing a bid on a task
 */
data class Bid(
    val id: String = "",
    val taskId: String = "",
    val workerId: String = "",
    val amount: Double = 0.0,
    val message: String = "",
    val status: BidStatus = BidStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing bid status
 */
enum class BidStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
