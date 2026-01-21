package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for wallet operations (mock)
 */
interface WalletRepository {
    /**
     * Get wallet for a user
     * @param userId The user ID
     * @return Flow of wallet
     */
    fun getWallet(userId: String): Flow<Wallet>

    /**
     * Credit amount to wallet
     * @param userId The user ID
     * @param amount The amount to credit
     * @param description Transaction description
     * @param taskId Optional task ID
     * @return Result indicating success or failure
     */
    suspend fun creditWallet(
        userId: String,
        amount: Double,
        description: String,
        taskId: String? = null
    ): BaseRepository.Result<Unit>

    /**
     * Debit amount from wallet
     * @param userId The user ID
     * @param amount The amount to debit
     * @param description Transaction description
     * @param taskId Optional task ID
     * @return Result indicating success or failure
     */
    suspend fun debitWallet(
        userId: String,
        amount: Double,
        description: String,
        taskId: String? = null
    ): BaseRepository.Result<Unit>

    /**
     * Get wallet balance
     * @param userId The user ID
     * @return Result containing the balance
     */
    suspend fun getBalance(userId: String): BaseRepository.Result<Double>
}
