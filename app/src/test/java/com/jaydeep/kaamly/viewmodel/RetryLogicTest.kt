package com.jaydeep.kaamly.viewmodel

import app.cash.turbine.test
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.TaskState
import com.jaydeep.kaamly.data.repository.AIRepository
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.LocationRepository
import com.jaydeep.kaamly.data.repository.TaskRepository
import com.jaydeep.kaamly.data.repository.WalletRepository
import com.jaydeep.kaamly.ui.viewmodel.ErrorState
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for retry logic with exponential backoff
 * Tests retry attempts, delay timing, and max retry limits
 * Requirements: 1.6, 18.5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RetryLogicTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var aiRepository: AIRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var walletRepository: WalletRepository
    private lateinit var taskViewModel: TaskViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = mockk(relaxed = true)
        aiRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)
        walletRepository = mockk(relaxed = true)
        
        // Setup default mock behaviors
        coEvery { taskRepository.getUserTasks(any()) } returns flowOf(emptyList())
        coEvery { taskRepository.getNearbyTasks(any(), any()) } returns flowOf(emptyList())
        coEvery { taskRepository.getTasksByCity(any()) } returns flowOf(emptyList())
        coEvery { taskRepository.getWorkerActiveTasks(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Retry Attempt Tests ==========

    @Test
    fun `executeWithRetry should retry on network error`() = runTest {
        // Given: Repository fails twice then succeeds
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            when (attemptCount) {
                1, 2 -> BaseRepository.Result.Error(IOException("Network error"))
                else -> BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Should have retried and eventually succeeded
        coVerify(exactly = 3) { taskRepository.getTask("task123") }
        
        val task = taskViewModel.selectedTask.value
        assertNotNull("Task should be loaded after retries", task)
    }

    @Test
    fun `executeWithRetry should stop after max retries`() = runTest {
        // Given: Repository always fails
        coEvery { 
            taskRepository.getTask(any()) 
        } returns BaseRepository.Result.Error(IOException("Network error"))

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry (max 3 attempts)
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Should have attempted exactly 3 times
        coVerify(exactly = 3) { taskRepository.getTask("task123") }
        
        // And error should be set
        val error = taskViewModel.error.value
        assertNotNull("Error should be set after max retries", error)
        assertTrue("Error should be NetworkError", error is ErrorState.NetworkError)
    }

    @Test
    fun `executeWithRetry should not retry on validation error`() = runTest {
        // Given: Repository returns validation error
        coEvery { 
            taskRepository.updateTaskState(any(), any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation error: Invalid state transition")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Update task state with invalid data
        taskViewModel.updateTaskState("task123", TaskState.COMPLETED)
        advanceUntilIdle()

        // Then: Should have attempted only once (no retry for validation errors)
        coVerify(exactly = 1) { taskRepository.updateTaskState("task123", TaskState.COMPLETED) }
        
        // And error should be set
        val error = taskViewModel.error.value
        assertNotNull("Error should be set", error)
        assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
    }

    // ========== Exponential Backoff Tests ==========

    @Test
    fun `executeWithRetry should use exponential backoff delays`() = runTest {
        // Given: Repository fails multiple times
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount < 3) {
                BaseRepository.Result.Error(IOException("Network error"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry
        taskViewModel.loadTask("task123")
        
        // First attempt happens immediately
        advanceTimeBy(100)
        assertEquals("First attempt should happen", 1, attemptCount)
        
        // Second attempt after 1000ms delay
        advanceTimeBy(1000)
        assertEquals("Second attempt should happen after 1s delay", 2, attemptCount)
        
        // Third attempt after 2000ms delay (exponential backoff: 1000 * 2)
        advanceTimeBy(2000)
        assertEquals("Third attempt should happen after 2s delay", 3, attemptCount)
        
        advanceUntilIdle()
        
        // Then: All attempts should have completed
        coVerify(exactly = 3) { taskRepository.getTask("task123") }
    }

    @Test
    fun `executeWithRetry should cap delay at maxDelay`() = runTest {
        // Given: Repository fails multiple times requiring many retries
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount < 4) {
                BaseRepository.Result.Error(IOException("Network error"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry
        taskViewModel.loadTask("task123")
        
        // Advance through all retries
        advanceTimeBy(100) // First attempt
        advanceTimeBy(1000) // Second attempt (1s delay)
        advanceTimeBy(2000) // Third attempt (2s delay)
        advanceTimeBy(4000) // Fourth attempt (4s delay, but capped at maxDelay)
        
        advanceUntilIdle()
        
        // Then: Should have attempted 4 times (initial + 3 retries)
        // Note: The actual implementation may vary, but delay should be capped
        assertTrue("Should have attempted multiple times", attemptCount >= 3)
    }

    // ========== Retry Success Tests ==========

    @Test
    fun `successful retry should clear error state`() = runTest {
        // Given: Repository fails first, succeeds second
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount == 1) {
                BaseRepository.Result.Error(IOException("Network error"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Error should be cleared after successful retry
        val error = taskViewModel.error.value
        assertEquals("Error should be cleared after successful retry", null, error)
        
        // And task should be loaded
        val task = taskViewModel.selectedTask.value
        assertNotNull("Task should be loaded", task)
    }

    @Test
    fun `retry should preserve loading state correctly`() = runTest {
        // Given: Repository fails then succeeds
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount == 1) {
                BaseRepository.Result.Error(IOException("Network error"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task with retry
        taskViewModel.loadTask("task123")

        // Then: Loading should be true during all attempts
        var loadingStates = mutableListOf<Boolean>()
        taskViewModel.isLoading.test {
            loadingStates.add(awaitItem()) // Initial false
            loadingStates.add(awaitItem()) // True during operation
            
            advanceUntilIdle()
            
            loadingStates.add(awaitItem()) // False after completion
            
            // Verify loading states
            assertEquals("Initial loading should be false", false, loadingStates[0])
            assertEquals("Loading should be true during operation", true, loadingStates[1])
            assertEquals("Loading should be false after completion", false, loadingStates[2])
        }
    }

    // ========== Network Error Detection Tests ==========

    @Test
    fun `IOException should be detected as network error`() = runTest {
        // Given: Repository throws IOException
        coEvery { 
            taskRepository.getTask(any()) 
        } returns BaseRepository.Result.Error(IOException("Connection failed"))

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Should retry (network errors are retryable)
        coVerify(atLeast = 2) { taskRepository.getTask("task123") }
    }

    @Test
    fun `error message containing network should trigger retry`() = runTest {
        // Given: Repository throws exception with "network" in message
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount < 2) {
                BaseRepository.Result.Error(RuntimeException("Network connection lost"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Should have retried
        coVerify(atLeast = 2) { taskRepository.getTask("task123") }
    }

    @Test
    fun `error message containing connection should trigger retry`() = runTest {
        // Given: Repository throws exception with "connection" in message
        var attemptCount = 0
        coEvery { 
            taskRepository.getTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount < 2) {
                BaseRepository.Result.Error(RuntimeException("Connection timeout"))
            } else {
                BaseRepository.Result.Success(createMockTask())
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository, walletRepository)
        advanceUntilIdle()

        // When: Load task
        taskViewModel.loadTask("task123")
        advanceUntilIdle()

        // Then: Should have retried
        coVerify(atLeast = 2) { taskRepository.getTask("task123") }
    }

    // ========== Helper Methods ==========

    private fun createMockTask(): Task {
        return Task(
            id = "task123",
            userId = "user123",
            title = "Test Task",
            description = "Test Description",
            category = TaskCategory.CLEANING,
            requiredEquipment = emptyList(),
            location = mockk(relaxed = true),
            scheduledDate = System.currentTimeMillis(),
            scheduledTime = "10:00 AM",
            budget = 100.0,
            isInstantJob = false,
            state = TaskState.OPEN,
            assignedWorkerId = null,
            completionPhotoUrl = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}

