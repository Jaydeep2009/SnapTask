package com.jaydeep.kaamly.data.model

/**
 * Data class representing a user/worker wallet (mock)
 */
data class Wallet(
    val userId: String = "",
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class representing a wallet transaction
 */
data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.CREDIT,
    val description: String = "",
    val taskId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enum representing transaction types
 */
enum class TransactionType {
    CREDIT,  // Money added to wallet
    DEBIT    // Money deducted from wallet
}
