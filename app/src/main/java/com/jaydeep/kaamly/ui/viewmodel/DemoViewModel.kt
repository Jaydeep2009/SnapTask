package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.util.DemoDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for demo mode operations
 */
@HiltViewModel
class DemoViewModel @Inject constructor(
    private val demoDataSeeder: DemoDataSeeder
) : BaseViewModel() {

    private val _demoDataSeeded = MutableStateFlow(false)
    val demoDataSeeded: StateFlow<Boolean> = _demoDataSeeded.asStateFlow()

    private val _seedingProgress = MutableStateFlow<String?>(null)
    val seedingProgress: StateFlow<String?> = _seedingProgress.asStateFlow()

    /**
     * Seed all demo data
     */
    fun seedDemoData() {
        execute(
            onError = { e -> e.message ?: "Failed to seed demo data" }
        ) {
            _seedingProgress.value = "Seeding demo users..."
            val result = demoDataSeeder.seedAllDemoData()
            
            if (result.isSuccess) {
                _demoDataSeeded.value = true
                _seedingProgress.value = "Demo data seeded successfully!"
                
                // Clear progress message after 2 seconds
                kotlinx.coroutines.delay(2000)
                _seedingProgress.value = null
            } else {
                _demoDataSeeded.value = false
                _seedingProgress.value = null
                throw result.exceptionOrNull() ?: Exception("Failed to seed demo data")
            }
        }
    }

    /**
     * Clear all demo data
     */
    fun clearDemoData() {
        execute(
            onError = { e -> e.message ?: "Failed to clear demo data" }
        ) {
            _seedingProgress.value = "Clearing demo data..."
            val result = demoDataSeeder.clearDemoData()
            
            if (result.isSuccess) {
                _demoDataSeeded.value = false
                _seedingProgress.value = "Demo data cleared successfully!"
                
                // Clear progress message after 2 seconds
                kotlinx.coroutines.delay(2000)
                _seedingProgress.value = null
            } else {
                _seedingProgress.value = null
                throw result.exceptionOrNull() ?: Exception("Failed to clear demo data")
            }
        }
    }

    /**
     * Reset demo data state
     */
    fun resetDemoState() {
        _demoDataSeeded.value = false
        _seedingProgress.value = null
        clearError()
    }
}
