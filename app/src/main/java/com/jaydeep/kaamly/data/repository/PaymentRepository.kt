package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.EscrowStatus

/**
 * Repository interface for payment and escrow operations (mock)
 */
interface PaymentRepository {
    /**
     * Lock funds in escrow (mock)
     * @param taskId The task ID
     * @param amount The bid amount
     * @return Result containing EscrowStatus
     */
    suspend fun lockEscrow(taskId: String, amount: Double): BaseRepository.Result<EscrowStatus>
    
    /**
     * Release funds from escrow (mock)
     * @param taskId The task ID
     * @return Result containing updated EscrowStatus
     */
    suspend fun releaseEscrow(taskId: String): BaseRepository.Result<EscrowStatus>
    
    /**
     * Calculate platform fee (Flat ₹20)
     * @param amount The bid amount
     * @return Platform fee amount
     */
    fun calculatePlatformFee(amount: Double): Double
}
