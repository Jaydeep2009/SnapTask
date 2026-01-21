package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.Bid
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for bid operations
 */
interface BidRepository {
    /**
     * Place a bid on a task
     */
    suspend fun placeBid(bid: Bid): BaseRepository.Result<String>
    
    /**
     * Get all bids for a task
     */
    suspend fun getBidsForTask(taskId: String): Flow<List<Bid>>
    
    /**
     * Accept a bid
     */
    suspend fun acceptBid(bidId: String): BaseRepository.Result<Unit>
    
    /**
     * Get all bids placed by a worker
     */
    suspend fun getWorkerBids(workerId: String): Flow<List<Bid>>
}
