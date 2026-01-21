package com.jaydeep.kaamly.data.model

/**
 * Data class representing escrow status (mock payment system)
 */
data class EscrowStatus(
    val taskId: String = "",
    val amount: Double = 0.0,
    val platformFee: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "locked", // "locked" or "released"
    val timestamp: Long = System.currentTimeMillis()
)
