package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.Bid
import com.jaydeep.kaamly.data.model.WorkerProfile
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.BidRepository
import com.jaydeep.kaamly.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for bid-related operations
 */
@HiltViewModel
class BidViewModel @Inject constructor(
    private val bidRepository: BidRepository,
    private val workerRepository: WorkerRepository
) : BaseViewModel() {

    // Bids for a specific task with worker profiles
    private val _taskBids = MutableStateFlow<List<BidWithWorker>>(emptyList())
    val taskBids: StateFlow<List<BidWithWorker>> = _taskBids.asStateFlow()

    // Bids placed by a worker
    private val _workerBids = MutableStateFlow<List<Bid>>(emptyList())
    val workerBids: StateFlow<List<Bid>> = _workerBids.asStateFlow()

    // Bid placement success
    private val _bidPlaced = MutableStateFlow<String?>(null)
    val bidPlaced: StateFlow<String?> = _bidPlaced.asStateFlow()

    // Bid acceptance success
    private val _bidAccepted = MutableStateFlow<Boolean>(false)
    val bidAccepted: StateFlow<Boolean> = _bidAccepted.asStateFlow()

    /**
     * Place a bid on a task
     */
    fun placeBid(taskId: String, workerId: String, amount: Double, message: String) {
        execute(
            onError = { e -> "Failed to place bid: ${e.message}" }
        ) {
            // Validate bid amount
            if (amount <= 0) {
                throw IllegalArgumentException("Bid amount must be positive")
            }
            
            if (message.isBlank()) {
                throw IllegalArgumentException("Please provide a message with your bid")
            }
            
            val bid = Bid(
                taskId = taskId,
                workerId = workerId,
                amount = amount,
                message = message
            )
            
            when (val result = bidRepository.placeBid(bid)) {
                is BaseRepository.Result.Success -> {
                    _bidPlaced.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Accept a bid
     */
    fun acceptBid(bidId: String) {
        execute(
            onError = { e -> "Failed to accept bid: ${e.message}" }
        ) {
            when (val result = bidRepository.acceptBid(bidId)) {
                is BaseRepository.Result.Success -> {
                    _bidAccepted.value = true
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Load all bids for a task with worker profiles
     */
    fun loadBidsForTask(taskId: String) {
        viewModelScope.launch {
            try {
                bidRepository.getBidsForTask(taskId).collect { bids ->
                    android.util.Log.d("BidViewModel", "Loaded ${bids.size} bids for task $taskId")
                    
                    // Load worker profiles for each bid
                    val bidsWithWorkers = bids.map { bid ->
                        try {
                            when (val result = workerRepository.getWorkerProfile(bid.workerId)) {
                                is BaseRepository.Result.Success -> {
                                    android.util.Log.d("BidViewModel", "Loaded worker profile for bid ${bid.id}")
                                    BidWithWorker(bid, result.data)
                                }
                                else -> {
                                    android.util.Log.w("BidViewModel", "Failed to load worker profile for bid ${bid.id}, using placeholder")
                                    // Create a placeholder worker profile if loading fails
                                    BidWithWorker(
                                        bid,
                                        WorkerProfile(
                                            workerId = bid.workerId,
                                            name = "Worker ${bid.workerId.take(6)}",
                                            city = "Unknown",
                                            skills = emptyList(),
                                            bio = "",
                                            overallRating = 0.0,
                                            totalReviews = 0
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("BidViewModel", "Error loading worker profile for bid ${bid.id}", e)
                            // Create a placeholder worker profile on error
                            BidWithWorker(
                                bid,
                                WorkerProfile(
                                    workerId = bid.workerId,
                                    name = "Worker ${bid.workerId.take(6)}",
                                    city = "Unknown",
                                    skills = emptyList(),
                                    bio = "",
                                    overallRating = 0.0,
                                    totalReviews = 0
                                )
                            )
                        }
                    }
                    
                    android.util.Log.d("BidViewModel", "Setting ${bidsWithWorkers.size} bids with workers")
                    _taskBids.value = bidsWithWorkers
                }
            } catch (e: Exception) {
                android.util.Log.e("BidViewModel", "Error loading bids for task", e)
                _taskBids.value = emptyList()
            }
        }
    }

    /**
     * Load all bids placed by a worker
     */
    fun loadWorkerBids(workerId: String) {
        viewModelScope.launch {
            bidRepository.getWorkerBids(workerId).collect { bids ->
                _workerBids.value = bids
            }
        }
    }

    /**
     * Clear bid placed state
     */
    fun clearBidPlaced() {
        _bidPlaced.value = null
    }

    /**
     * Clear bid accepted state
     */
    fun clearBidAccepted() {
        _bidAccepted.value = false
    }

    /**
     * Clear task bids
     */
    fun clearTaskBids() {
        _taskBids.value = emptyList()
    }
}

/**
 * Data class combining a bid with the worker's profile
 */
data class BidWithWorker(
    val bid: Bid,
    val worker: WorkerProfile
)
