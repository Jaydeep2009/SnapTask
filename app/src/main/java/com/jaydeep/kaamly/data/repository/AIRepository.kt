package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.AITaskSuggestion

/**
 * Repository interface for AI task generation operations
 */
interface AIRepository {
    /**
     * Generate task details from a brief description
     */
    suspend fun generateTaskDetails(briefDescription: String): BaseRepository.Result<AITaskSuggestion>
}
