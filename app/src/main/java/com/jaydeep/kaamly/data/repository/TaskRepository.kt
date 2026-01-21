package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.jaydeep.kaamly.data.model.Location
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task operations
 */
interface TaskRepository {
    /**
     * Create a new task
     * @param task The task to create
     * @return Result containing the task ID
     */
    suspend fun createTask(task: Task): BaseRepository.Result<String>

    /**
     * Get a task by ID
     * @param taskId The task ID
     * @return Result containing the task
     */
    suspend fun getTask(taskId: String): BaseRepository.Result<Task>

    /**
     * Get all tasks for a specific user
     * @param userId The user ID
     * @return Flow of task list
     */
    fun getUserTasks(userId: String): Flow<List<Task>>

    /**
     * Get nearby tasks based on location and radius
     * @param location The center location
     * @param radius The search radius in kilometers
     * @return Flow of task list
     */
    fun getNearbyTasks(location: Location, radius: Double): Flow<List<Task>>

    /**
     * Get tasks by city
     * @param city The city name
     * @return Flow of task list
     */
    fun getTasksByCity(city: String): Flow<List<Task>>

    /**
     * Get all open tasks (for workers to browse all available tasks)
     * @return Flow of task list
     */
    fun getAllOpenTasks(): Flow<List<Task>>

    /**
     * Update task state
     * @param taskId The task ID
     * @param state The new state
     * @return Result indicating success or failure
     */
    suspend fun updateTaskState(taskId: String, state: TaskState): BaseRepository.Result<Unit>

    /**
     * Assign a worker to a task
     * @param taskId The task ID
     * @param workerId The worker ID
     * @return Result indicating success or failure
     */
    suspend fun assignWorker(taskId: String, workerId: String): BaseRepository.Result<Unit>

    /**
     * Upload a task completion photo
     * @param taskId The task ID
     * @param imageUri The image URI
     * @return Result containing the photo URL
     */
    suspend fun uploadTaskPhoto(taskId: String, imageUri: Uri): BaseRepository.Result<String>

    /**
     * Mark worker as arrived at task location
     * @param taskId The task ID
     * @return Result indicating success or failure
     */
    suspend fun markArrived(taskId: String): BaseRepository.Result<Unit>

    /**
     * Mark task as completed by worker
     * @param taskId The task ID
     * @return Result indicating success or failure
     */
    suspend fun markCompleted(taskId: String): BaseRepository.Result<Unit>

    /**
     * Approve task completion by user
     * @param taskId The task ID
     * @return Result indicating success or failure
     */
    suspend fun approveCompletion(taskId: String): BaseRepository.Result<Unit>

    /**
     * Get active tasks for a worker (tasks where their bid was accepted)
     * @param workerId The worker ID
     * @return Flow of active task list
     */
    fun getWorkerActiveTasks(workerId: String): Flow<List<Task>>
}
