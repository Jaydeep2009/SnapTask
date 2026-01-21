package com.jaydeep.kaamly.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jaydeep.kaamly.data.model.Transaction
import com.jaydeep.kaamly.data.model.TransactionType
import com.jaydeep.kaamly.data.model.Wallet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WalletRepository using Firebase Firestore (mock wallet system)
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WalletRepository {

    companion object {
        private const val WALLETS_COLLECTION = "wallets"
    }

    override fun getWallet(userId: String): Flow<Wallet> = callbackFlow {
        val listener = firestore.collection(WALLETS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("WalletRepository", "Error loading wallet", error)
                    // Send empty wallet on error
                    trySend(Wallet(userId = userId, balance = 0.0))
                    return@addSnapshotListener
                }
                
                val wallet = snapshot?.toObject(Wallet::class.java) ?: Wallet(userId = userId, balance = 0.0)
                trySend(wallet)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun creditWallet(
        userId: String,
        amount: Double,
        description: String,
        taskId: String?
    ): BaseRepository.Result<Unit> {
        return try {
            val walletRef = firestore.collection(WALLETS_COLLECTION).document(userId)
            
            // Get current wallet or create new one
            val walletDoc = walletRef.get().await()
            val currentWallet = walletDoc.toObject(Wallet::class.java) ?: Wallet(userId = userId)
            
            // Create transaction
            val transaction = Transaction(
                id = firestore.collection("temp").document().id,
                amount = amount,
                type = TransactionType.CREDIT,
                description = description,
                taskId = taskId,
                timestamp = System.currentTimeMillis()
            )
            
            // Update wallet
            val updatedWallet = currentWallet.copy(
                balance = currentWallet.balance + amount,
                transactions = currentWallet.transactions + transaction,
                updatedAt = System.currentTimeMillis()
            )
            
            walletRef.set(updatedWallet).await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun debitWallet(
        userId: String,
        amount: Double,
        description: String,
        taskId: String?
    ): BaseRepository.Result<Unit> {
        return try {
            val walletRef = firestore.collection(WALLETS_COLLECTION).document(userId)
            
            // Get current wallet
            val walletDoc = walletRef.get().await()
            val currentWallet = walletDoc.toObject(Wallet::class.java) 
                ?: return BaseRepository.Result.Error(Exception("Wallet not found"))
            
            // Check sufficient balance
            if (currentWallet.balance < amount) {
                return BaseRepository.Result.Error(Exception("Insufficient balance"))
            }
            
            // Create transaction
            val transaction = Transaction(
                id = firestore.collection("temp").document().id,
                amount = amount,
                type = TransactionType.DEBIT,
                description = description,
                taskId = taskId,
                timestamp = System.currentTimeMillis()
            )
            
            // Update wallet
            val updatedWallet = currentWallet.copy(
                balance = currentWallet.balance - amount,
                transactions = currentWallet.transactions + transaction,
                updatedAt = System.currentTimeMillis()
            )
            
            walletRef.set(updatedWallet).await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun getBalance(userId: String): BaseRepository.Result<Double> {
        return try {
            val walletDoc = firestore.collection(WALLETS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val wallet = walletDoc.toObject(Wallet::class.java)
            BaseRepository.Result.Success(wallet?.balance ?: 0.0)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }
}
