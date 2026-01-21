package com.jaydeep.kaamly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaydeep.kaamly.data.model.EscrowStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PaymentRepository using Firebase Firestore (mock payment system)
 */
@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentRepository {

    companion object {
        private const val ESCROW_COLLECTION = "escrow"
        private const val PLATFORM_FEE = 20.0 // Flat ₹20 platform fee
    }

    override suspend fun lockEscrow(taskId: String, amount: Double): BaseRepository.Result<EscrowStatus> {
        return try {
            val platformFee = calculatePlatformFee(amount)
            val total = amount + platformFee
            
            val escrowStatus = EscrowStatus(
                taskId = taskId,
                amount = amount,
                platformFee = platformFee,
                total = total,
                status = "locked",
                timestamp = System.currentTimeMillis()
            )
            
            // Store escrow status in Firestore
            firestore.collection(ESCROW_COLLECTION)
                .document(taskId)
                .set(escrowStatus)
                .await()
            
            BaseRepository.Result.Success(escrowStatus)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun releaseEscrow(taskId: String): BaseRepository.Result<EscrowStatus> {
        return try {
            // Get existing escrow status
            val escrowDoc = firestore.collection(ESCROW_COLLECTION)
                .document(taskId)
                .get()
                .await()
            
            val existingEscrow = escrowDoc.toObject(EscrowStatus::class.java)
                ?: return BaseRepository.Result.Error(Exception("Escrow not found for task"))
            
            // Update status to released
            val updatedEscrow = existingEscrow.copy(
                status = "released",
                timestamp = System.currentTimeMillis()
            )
            
            firestore.collection(ESCROW_COLLECTION)
                .document(taskId)
                .set(updatedEscrow)
                .await()
            
            BaseRepository.Result.Success(updatedEscrow)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override fun calculatePlatformFee(amount: Double): Double {
        return PLATFORM_FEE // Flat ₹20 fee
    }
}
