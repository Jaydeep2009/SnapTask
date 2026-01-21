package com.jaydeep.kaamly.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.jaydeep.kaamly.data.model.AITaskSuggestion
import com.jaydeep.kaamly.data.model.Location
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.TaskState
import com.jaydeep.kaamly.data.repository.AIRepository
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for task-related operations
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val aiRepository: AIRepository,
    private val locationRepository: com.jaydeep.kaamly.data.repository.LocationRepository,
    private val walletRepository: com.jaydeep.kaamly.data.repository.WalletRepository
) : BaseViewModel() {

    // User's tasks
    private val _userTasks = MutableStateFlow<List<Task>>(emptyList())
    val userTasks: StateFlow<List<Task>> = _userTasks.asStateFlow()

    // Nearby tasks for workers
    private val _nearbyTasks = MutableStateFlow<List<Task>>(emptyList())
    val nearbyTasks: StateFlow<List<Task>> = _nearbyTasks.asStateFlow()

    // Selected task details
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    // Task filters
    private val _taskFilters = MutableStateFlow(TaskFilters())
    val taskFilters: StateFlow<TaskFilters> = _taskFilters.asStateFlow()

    // AI-generated task suggestion
    private val _aiSuggestion = MutableStateFlow<AITaskSuggestion?>(null)
    val aiSuggestion: StateFlow<AITaskSuggestion?> = _aiSuggestion.asStateFlow()

    // Task creation success
    private val _taskCreated = MutableStateFlow<String?>(null)
    val taskCreated: StateFlow<String?> = _taskCreated.asStateFlow()
    
    // Current location
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    // Distance to selected task
    private val _taskDistance = MutableStateFlow<Double?>(null)
    val taskDistance: StateFlow<Double?> = _taskDistance.asStateFlow()

    // Active tasks for workers
    private val _activeTasks = MutableStateFlow<List<Task>>(emptyList())
    val activeTasks: StateFlow<List<Task>> = _activeTasks.asStateFlow()
    
    // Worker active tasks (approved jobs)
    private val _workerActiveTasks = MutableStateFlow<List<Task>>(emptyList())
    val workerActiveTasks: StateFlow<List<Task>> = _workerActiveTasks.asStateFlow()

    // Photo upload success
    private val _photoUploaded = MutableStateFlow<String?>(null)
    val photoUploaded: StateFlow<String?> = _photoUploaded.asStateFlow()

    /**
     * Create a new task
     */
    fun createTask(task: Task) {
        execute(
            onError = { e -> "Failed to create task: ${e.message}" }
        ) {
            when (val result = taskRepository.createTask(task)) {
                is BaseRepository.Result.Success -> {
                    _taskCreated.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Generate task details using AI
     */
    fun generateTaskWithAI(briefDescription: String) {
        execute(
            onError = { e -> "AI generation failed: ${e.message}. Please create task manually." }
        ) {
            when (val result = aiRepository.generateTaskDetails(briefDescription)) {
                is BaseRepository.Result.Success -> {
                    _aiSuggestion.value = result.data
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Clear AI suggestion
     */
    fun clearAISuggestion() {
        _aiSuggestion.value = null
    }

    /**
     * Clear task created state
     */
    fun clearTaskCreated() {
        _taskCreated.value = null
    }

    /**
     * Load tasks for a specific user
     */
    fun loadUserTasks(userId: String) {
        viewModelScope.launch {
            try {
                taskRepository.getUserTasks(userId).collect { tasks ->
                    _userTasks.value = tasks
                }
            } catch (e: Exception) {
                // Handle error silently - empty list will be shown
                _userTasks.value = emptyList()
            }
        }
    }

    /**
     * Load nearby tasks based on location
     */
    fun loadNearbyTasks(location: Location, radius: Double = 50.0) {
        viewModelScope.launch {
            try {
                taskRepository.getNearbyTasks(location, radius).collect { tasks ->
                    _nearbyTasks.value = applyFilters(tasks)
                }
            } catch (e: Exception) {
                // Handle error silently - empty list will be shown
                _nearbyTasks.value = emptyList()
            }
        }
    }

    /**
     * Load tasks by city
     */
    fun loadTasksByCity(city: String) {
        viewModelScope.launch {
            try {
                taskRepository.getTasksByCity(city).collect { tasks ->
                    _nearbyTasks.value = applyFilters(tasks)
                }
            } catch (e: Exception) {
                // Handle error silently - empty list will be shown
                _nearbyTasks.value = emptyList()
            }
        }
    }

    /**
     * Load all open tasks (for workers to see all available tasks)
     */
    fun loadAllOpenTasks() {
        viewModelScope.launch {
            try {
                taskRepository.getAllOpenTasks().collect { tasks ->
                    _nearbyTasks.value = applyFilters(tasks)
                }
            } catch (e: Exception) {
                // Handle error silently - empty list will be shown
                _nearbyTasks.value = emptyList()
            }
        }
    }

    /**
     * Load a specific task
     */
    fun loadTask(taskId: String) {
        executeWithRetry(
            onError = { e -> "Failed to load task: ${e.message}" }
        ) {
            when (val result = taskRepository.getTask(taskId)) {
                is BaseRepository.Result.Success -> {
                    _selectedTask.value = result.data
                    // Calculate distance if we have current location
                    _currentLocation.value?.let { currentLoc ->
                        _taskDistance.value = locationRepository.getDistanceBetween(
                            currentLoc,
                            result.data.location
                        )
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
     * Get current location
     */
    fun getCurrentLocation() {
        viewModelScope.launch {
            when (val result = locationRepository.getCurrentLocation()) {
                is BaseRepository.Result.Success -> {
                    _currentLocation.value = result.data
                    // Recalculate distance if we have a selected task
                    _selectedTask.value?.let { task ->
                        _taskDistance.value = locationRepository.getDistanceBetween(
                            result.data,
                            task.location
                        )
                    }
                }
                is BaseRepository.Result.Error -> {
                    // Silently fail - distance will just not be shown
                }
                else -> {}
            }
        }
    }

    /**
     * Update task state
     */
    fun updateTaskState(taskId: String, state: TaskState) {
        execute(
            onError = { e -> "Failed to update task state: ${e.message}" }
        ) {
            when (val result = taskRepository.updateTaskState(taskId, state)) {
                is BaseRepository.Result.Success -> {
                    // Reload the task to get updated state
                    loadTask(taskId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Apply filters to task list
     */
    fun applyFilters(filters: TaskFilters) {
        _taskFilters.value = filters
        // Re-apply filters to current task list
        _nearbyTasks.value = applyFilters(_nearbyTasks.value)
    }

    /**
     * Apply filters to a list of tasks
     */
    private fun applyFilters(tasks: List<Task>): List<Task> {
        val filters = _taskFilters.value
        return tasks.filter { task ->
            // Filter by category
            val categoryMatch = filters.category == null || task.category == filters.category
            
            // Filter by instant jobs
            val instantMatch = !filters.instantJobsOnly || task.isInstantJob
            
            // Filter by budget range
            val budgetMatch = (filters.minBudget == null || task.budget >= filters.minBudget) &&
                             (filters.maxBudget == null || task.budget <= filters.maxBudget)
            
            categoryMatch && instantMatch && budgetMatch
        }
    }

    /**
     * Clear selected task
     */
    fun clearSelectedTask() {
        _selectedTask.value = null
    }

    /**
     * Load active tasks for a worker (approved jobs)
     */
    fun loadWorkerActiveTasks(workerId: String) {
        viewModelScope.launch {
            taskRepository.getWorkerActiveTasks(workerId).collect { tasks ->
                _workerActiveTasks.value = tasks
            }
        }
    }
    
    /**
     * Mark task as completed by worker
     */
    fun markTaskCompleted(taskId: String) {
        execute(
            onError = { e -> "Failed to mark task as completed: ${e.message}" }
        ) {
            when (val result = taskRepository.markCompleted(taskId)) {
                is BaseRepository.Result.Success -> {
                    // Task will be updated via the flow
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Mark worker as arrived
     */
    fun markArrived(taskId: String) {
        execute(
            onError = { e -> "Failed to mark arrived: ${e.message}" }
        ) {
            when (val result = taskRepository.markArrived(taskId)) {
                is BaseRepository.Result.Success -> {
                    // Reload the task to get updated state
                    loadTask(taskId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Upload completion photo
     */
    fun uploadCompletionPhoto(taskId: String, imageUri: android.net.Uri) {
        execute(
            onError = { e -> "Failed to upload photo: ${e.message}" }
        ) {
            when (val result = taskRepository.uploadTaskPhoto(taskId, imageUri)) {
                is BaseRepository.Result.Success -> {
                    _photoUploaded.value = result.data
                    // Reload the task to get updated photo URL
                    loadTask(taskId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Mark task as completed
     */
    fun markCompleted(taskId: String) {
        execute(
            onError = { e -> "Failed to mark completed: ${e.message}" }
        ) {
            when (val result = taskRepository.markCompleted(taskId)) {
                is BaseRepository.Result.Success -> {
                    // Reload the task to get updated state
                    loadTask(taskId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Approve task completion
     */
    fun approveCompletion(taskId: String) {
        execute(
            onError = { e -> "Failed to approve completion: ${e.message}" }
        ) {
            when (val result = taskRepository.approveCompletion(taskId)) {
                is BaseRepository.Result.Success -> {
                    // Reload the task to get updated state
                    loadTask(taskId)
                }
                is BaseRepository.Result.Error -> {
                    throw result.exception
                }
                else -> {}
            }
        }
    }

    /**
     * Approve task completion and release payment to worker
     */
    fun approveTaskAndReleasePayment(taskId: String, workerId: String, amount: Double) {
        execute(
            onError = { e -> "Failed to approve completion: ${e.message}" }
        ) {
            // First approve the task completion
            when (val result = taskRepository.approveCompletion(taskId)) {
                is BaseRepository.Result.Success -> {
                    // Then credit the worker's wallet
                    when (walletRepository.creditWallet(
                        userId = workerId,
                        amount = amount,
                        description = "Payment received for task completion",
                        taskId = taskId
                    )) {
                        is BaseRepository.Result.Success -> {
                            // Success - task will be updated via the flow
                        }
                        is BaseRepository.Result.Error -> {
                            throw Exception("Task approved but failed to credit worker wallet")
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
     * Clear photo uploaded state
     */
    fun clearPhotoUploaded() {
        _photoUploaded.value = null
    }
}

/**
 * Data class for task filters
 */
data class TaskFilters(
    val category: TaskCategory? = null,
    val instantJobsOnly: Boolean = false,
    val minBudget: Double? = null,
    val maxBudget: Double? = null
)
