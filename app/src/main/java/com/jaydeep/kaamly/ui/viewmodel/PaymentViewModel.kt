package com.jaydeep.kaamly.ui.viewmodel

import com.jaydeep.kaamly.data.model.EscrowStatus
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.PaymentRepository
import com.jaydeep.kaamly.data.repository.TaskRepository
import com.jaydeep.kaamly.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for payment and escrow operations (mock)
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val taskRepository: TaskRepository,
    private val walletRepository: WalletRepository
) : BaseViewModel() {

    // Current escrow status
    private val _escrowStatus = MutableStateFlow<EscrowStatus?>(null)
    val escrowStatus: StateFlow<EscrowStatus?> = _escrowStatus.asStateFlow()

    // Payment breakdown
    private val _paymentBreakdown = MutableStateFlow<PaymentBreakdown?>(null)
    val paymentBreakdown: StateFlow<PaymentBreakdown?> = _paymentBreakdown.asStateFlow()

    // Escrow locked success
    private val _escrowLocked = MutableStateFlow<Boolean>(false)
    val escrowLocked: StateFlow<Boolean> = _escrowLocked.asStateFlow()

    // Escrow released success
    private val _escrowReleased = MutableStateFlow<Boolean>(false)
    val escrowReleased: StateFlow<Boolean> = _escrowReleased.asStateFlow()

    /**
     * Lock funds in escrow (mock)
     */
    fun lockEscrow(taskId: String, amount: Double) {
        execute(
            onError = { e -> "Failed to lock escrow: ${e.message}" }
        ) {
            // Validate amount
            if (amount <= 0) {
                throw IllegalArgumentException("Amount must be positive")
            }
            
            when (val result = paymentRepository.lockEscrow(taskId, amount)) {
                is BaseRepository.Result.Success -> {
                    _escrowStatus.value = result.data
                    _escrowLocked.value = true
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Release funds from escrow to worker wallet (mock)
     */
    fun releaseEscrowToWorker(taskId: String, workerId: String, amount: Double) {
        execute(
            onError = { e -> "Failed to release escrow: ${e.message}" }
        ) {
            // Release escrow
            when (val result = paymentRepository.releaseEscrow(taskId)) {
                is BaseRepository.Result.Success -> {
                    _escrowStatus.value = result.data
                    
                    // Credit worker wallet
                    when (walletRepository.creditWallet(
                        userId = workerId,
                        amount = amount,
                        description = "Payment received for task completion",
                        taskId = taskId
                    )) {
                        is BaseRepository.Result.Success -> {
                            _escrowReleased.value = true
                        }
                        is BaseRepository.Result.Error -> {
                            throw Exception("Failed to credit worker wallet")
                        }
                        else -> {}
                    }
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Calculate payment breakdown
     */
    fun calculateTotal(bidAmount: Double): PaymentBreakdown {
        val platformFee = paymentRepository.calculatePlatformFee(bidAmount)
        val total = bidAmount + platformFee
        
        val breakdown = PaymentBreakdown(
            bidAmount = bidAmount,
            platformFee = platformFee,
            total = total
        )
        
        _paymentBreakdown.value = breakdown
        return breakdown
    }

    /**
     * Clear escrow locked state
     */
    fun clearEscrowLocked() {
        _escrowLocked.value = false
    }

    /**
     * Clear escrow released state
     */
    fun clearEscrowReleased() {
        _escrowReleased.value = false
    }

    /**
     * Clear escrow status
     */
    fun clearEscrowStatus() {
        _escrowStatus.value = null
    }

    /**
     * Clear payment breakdown
     */
    fun clearPaymentBreakdown() {
        _paymentBreakdown.value = null
    }
}

/**
 * Data class representing payment breakdown
 */
data class PaymentBreakdown(
    val bidAmount: Double,
    val platformFee: Double,
    val total: Double
)
