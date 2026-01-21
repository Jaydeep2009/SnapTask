package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.Wallet
import com.jaydeep.kaamly.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for wallet operations (mock)
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : BaseViewModel() {

    // Current wallet
    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet.asStateFlow()

    // Wallet balance
    private val _balance = MutableStateFlow<Double>(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    /**
     * Load wallet for a user
     */
    fun loadWallet(userId: String) {
        viewModelScope.launch {
            try {
                walletRepository.getWallet(userId).collect { wallet ->
                    _wallet.value = wallet
                    _balance.value = wallet.balance
                }
            } catch (e: Exception) {
                android.util.Log.e("WalletViewModel", "Error loading wallet", e)
                _wallet.value = Wallet(userId = userId, balance = 0.0)
                _balance.value = 0.0
            }
        }
    }

    /**
     * Clear wallet
     */
    fun clearWallet() {
        _wallet.value = null
        _balance.value = 0.0
    }
}
