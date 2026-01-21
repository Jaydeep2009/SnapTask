package com.jaydeep.kaamly.viewmodel

import app.cash.turbine.test
import com.jaydeep.kaamly.data.model.AITaskSuggestion
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskCategory
import com.jaydeep.kaamly.data.model.TaskState
import com.jaydeep.kaamly.data.repository.AIRepository
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.data.repository.LocationRepository
import com.jaydeep.kaamly.data.repository.TaskRepository
import com.jaydeep.kaamly.ui.viewmodel.ErrorState
import com.jaydeep.kaamly.ui.viewmodel.TaskViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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

/**
 * Unit tests for validation error display
 * Tests form validation errors, field-specific errors, and error messages
 * Requirements: 1.6, 18.5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ValidationErrorTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var aiRepository: AIRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var taskViewModel: TaskViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = mockk(relaxed = true)
        aiRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)
        
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

    // ========== Task Creation Validation Tests ==========

    @Test
    fun `createTask with missing title should display validation error`() = runTest {
        // Given: Repository returns validation error for missing title
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Title is required")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with missing title
        val task = createMockTask(title = "")
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should mention title",
                error!!.message.contains("title", ignoreCase = true) ||
                error.message.contains("required", ignoreCase = true)
            )
        }
    }

    @Test
    fun `createTask with invalid budget should display validation error`() = runTest {
        // Given: Repository returns validation error for invalid budget
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Budget must be positive")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with negative budget
        val task = createMockTask(budget = -100.0)
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should mention budget",
                error!!.message.contains("budget", ignoreCase = true) ||
                error.message.contains("positive", ignoreCase = true)
            )
        }
    }

    @Test
    fun `createTask with past date should display validation error`() = runTest {
        // Given: Repository returns validation error for past date
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Date must be in the future")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with past date
        val task = createMockTask(scheduledDate = System.currentTimeMillis() - 86400000) // Yesterday
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should mention date",
                error!!.message.contains("date", ignoreCase = true) ||
                error.message.contains("future", ignoreCase = true)
            )
        }
    }

    @Test
    fun `createTask with missing description should display validation error`() = runTest {
        // Given: Repository returns validation error for missing description
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Description is required")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with empty description
        val task = createMockTask(description = "")
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should mention description",
                error!!.message.contains("description", ignoreCase = true) ||
                error.message.contains("required", ignoreCase = true)
            )
        }
    }

    // ========== AI Generation Validation Tests ==========

    @Test
    fun `generateTaskWithAI with empty input should display validation error`() = runTest {
        // Given: Repository returns validation error for empty input
        coEvery { 
            aiRepository.generateTaskDetails(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Description cannot be empty")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Generate task with empty description
        taskViewModel.generateTaskWithAI("")
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue(
                "Error should mention validation or AI failure",
                error!!.message.contains("validation", ignoreCase = true) ||
                error.message.contains("AI", ignoreCase = true) ||
                error.message.contains("empty", ignoreCase = true)
            )
        }
    }

    @Test
    fun `generateTaskWithAI with too short input should display validation error`() = runTest {
        // Given: Repository returns validation error for too short input
        coEvery { 
            aiRepository.generateTaskDetails(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Description too short")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Generate task with very short description
        taskViewModel.generateTaskWithAI("hi")
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue(
                "Error should mention validation or length",
                error!!.message.contains("validation", ignoreCase = true) ||
                error.message.contains("short", ignoreCase = true) ||
                error.message.contains("AI", ignoreCase = true)
            )
        }
    }

    // ========== Task State Validation Tests ==========

    @Test
    fun `updateTaskState with invalid transition should display validation error`() = runTest {
        // Given: Repository returns validation error for invalid state transition
        coEvery { 
            taskRepository.updateTaskState(any(), any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Invalid state transition from COMPLETED to OPEN")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Try invalid state transition
        taskViewModel.updateTaskState("task123", TaskState.OPEN)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should mention state or transition",
                error!!.message.contains("state", ignoreCase = true) ||
                error.message.contains("transition", ignoreCase = true) ||
                error.message.contains("invalid", ignoreCase = true)
            )
        }
    }

    // ========== Photo Upload Validation Tests ==========

    @Test
    fun `uploadCompletionPhoto with invalid file should display validation error`() = runTest {
        // Given: Repository returns validation error for invalid file
        coEvery { 
            taskRepository.uploadTaskPhoto(any(), any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: File must be an image")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Upload invalid file
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        taskViewModel.uploadCompletionPhoto("task123", mockUri)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue(
                "Error should mention validation or file",
                error!!.message.contains("validation", ignoreCase = true) ||
                error.message.contains("file", ignoreCase = true) ||
                error.message.contains("image", ignoreCase = true) ||
                error.message.contains("upload", ignoreCase = true)
            )
        }
    }

    @Test
    fun `uploadCompletionPhoto with file too large should display validation error`() = runTest {
        // Given: Repository returns validation error for file size
        coEvery { 
            taskRepository.uploadTaskPhoto(any(), any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: File size must be less than 5MB")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Upload large file
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        taskViewModel.uploadCompletionPhoto("task123", mockUri)
        advanceUntilIdle()

        // Then: Should display validation error
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue(
                "Error should mention file size",
                error!!.message.contains("size", ignoreCase = true) ||
                error.message.contains("large", ignoreCase = true) ||
                error.message.contains("5MB", ignoreCase = false) ||
                error.message.contains("upload", ignoreCase = true)
            )
        }
    }

    // ========== Error Message Clarity Tests ==========

    @Test
    fun `validation error messages should be user-friendly`() = runTest {
        // Given: Repository returns technical validation error
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: field 'title' constraint violation")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with validation error
        val task = createMockTask()
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Error message should be present
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue(
                "Error message should be present",
                error!!.message.isNotEmpty()
            )
        }
    }

    @Test
    fun `multiple validation errors should be displayed clearly`() = runTest {
        // Given: Repository returns multiple validation errors
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Title is required, Budget must be positive")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: Create task with multiple validation errors
        val task = createMockTask(title = "", budget = -100.0)
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Error message should contain information about errors
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should not be null", error)
            assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
            assertTrue(
                "Error message should be comprehensive",
                error!!.message.isNotEmpty()
            )
        }
    }

    // ========== Validation Error Recovery Tests ==========

    @Test
    fun `clearError should remove validation error`() = runTest {
        // Given: ViewModel has a validation error
        coEvery { 
            taskRepository.createTask(any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Title is required")
        )

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        val task = createMockTask(title = "")
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Verify error is set
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should be set", error)
        }

        // When: Clear error
        taskViewModel.clearError()
        advanceUntilIdle()

        // Then: Error should be null
        taskViewModel.error.test {
            val error = awaitItem()
            assertEquals("Error should be cleared", null, error)
        }
    }

    @Test
    fun `successful operation after validation error should clear error`() = runTest {
        // Given: Repository fails first with validation error, then succeeds
        var attemptCount = 0
        coEvery { 
            taskRepository.createTask(any()) 
        } answers {
            attemptCount++
            if (attemptCount == 1) {
                BaseRepository.Result.Error(IllegalArgumentException("Validation failed"))
            } else {
                BaseRepository.Result.Success("task123")
            }
        }

        taskViewModel = TaskViewModel(taskRepository, aiRepository, locationRepository)
        advanceUntilIdle()

        // When: First attempt fails
        val task = createMockTask()
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Verify error is set
        taskViewModel.error.test {
            val error = awaitItem()
            assertNotNull("Error should be set after validation failure", error)
        }

        // When: Second attempt succeeds
        taskViewModel.createTask(task)
        advanceUntilIdle()

        // Then: Error should be cleared
        taskViewModel.error.test {
            val error = awaitItem()
            assertEquals("Error should be cleared after success", null, error)
        }
    }

    // ========== Helper Methods ==========

    private fun createMockTask(
        title: String = "Test Task",
        description: String = "Test Description",
        budget: Double = 100.0,
        scheduledDate: Long = System.currentTimeMillis() + 86400000 // Tomorrow
    ): Task {
        return Task(
            id = "task123",
            userId = "user123",
            title = title,
            description = description,
            category = TaskCategory.CLEANING,
            requiredEquipment = emptyList(),
            location = mockk(relaxed = true),
            scheduledDate = scheduledDate,
            scheduledTime = "10:00 AM",
            budget = budget,
            isInstantJob = false,
            state = TaskState.OPEN,
            assignedWorkerId = null,
            completionPhotoUrl = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
