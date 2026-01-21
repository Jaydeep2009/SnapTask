package com.jaydeep.kaamly.viewmodel

import app.cash.turbine.test
import com.jaydeep.kaamly.data.model.User
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.data.repository.AuthRepository
import com.jaydeep.kaamly.data.repository.BaseRepository
import com.jaydeep.kaamly.ui.viewmodel.AuthState
import com.jaydeep.kaamly.ui.viewmodel.AuthViewModel
import com.jaydeep.kaamly.ui.viewmodel.ErrorState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for error handling in ViewModels
 * Tests network error display, validation error display, and retry logic
 * Requirements: 1.6, 18.5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ErrorHandlingTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Network Error Display Tests ==========

    @Test
    fun `login with network error should display network error message`() = runTest {
        // Given: Repository throws IOException (network error)
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(IOException("Network unavailable"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: User attempts to login
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Then: Error state should be NetworkError with appropriate message
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be NetworkError", error is ErrorState.NetworkError)
        assertTrue(
            "Error message should mention connection",
            error!!.message.contains("connection", ignoreCase = true)
        )
    }

    @Test
    fun `signup with SocketTimeoutException should display network error`() = runTest {
        // Given: Repository throws SocketTimeoutException
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.signUp(any(), any(), any()) 
        } returns BaseRepository.Result.Error(SocketTimeoutException("Connection timeout"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: User attempts to signup
        authViewModel.signUp("test@example.com", "password123", "Test User")
        advanceUntilIdle()

        // Then: Error state should be NetworkError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be NetworkError", error is ErrorState.NetworkError)
    }

    @Test
    fun `operation with UnknownHostException should display network error`() = runTest {
        // Given: Repository throws UnknownHostException
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(UnknownHostException("Unable to resolve host"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: User attempts to login
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Then: Error state should be NetworkError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be NetworkError", error is ErrorState.NetworkError)
    }

    // ========== Validation Error Display Tests ==========

    @Test
    fun `signup with validation error should display validation error message`() = runTest {
        // Given: Repository returns validation error
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.signUp(any(), any(), any()) 
        } returns BaseRepository.Result.Error(
            IllegalArgumentException("Validation failed: Password must be at least 8 characters")
        )

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: User attempts to signup with invalid data
        authViewModel.signUp("test@example.com", "weak", "Test User")
        advanceUntilIdle()

        // Then: Error state should be ValidationError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be ValidationError", error is ErrorState.ValidationError)
        assertTrue(
            "Error message should mention validation",
            error!!.message.contains("validation", ignoreCase = true) ||
            error.message.contains("Password", ignoreCase = true)
        )
    }

    @Test
    fun `login with authentication error should display auth error message`() = runTest {
        // Given: Repository returns authentication error
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(
            SecurityException("Authentication failed: Invalid credentials")
        )

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: User attempts to login with wrong credentials
        authViewModel.login("test@example.com", "wrongpassword")
        advanceUntilIdle()

        // Then: Error state should be AuthenticationError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be AuthenticationError", error is ErrorState.AuthenticationError)
        assertTrue(
            "Error message should mention authentication",
            error!!.message.contains("authentication", ignoreCase = true) ||
            error.message.contains("login", ignoreCase = true)
        )
    }

    @Test
    fun `operation with permission error should display permission error message`() = runTest {
        // Given: Repository returns permission error
        coEvery { authRepository.getCurrentUser() } returns null
        val user = User("user123", "test@example.com", "Test User", UserRole.USER, System.currentTimeMillis())
        coEvery { authRepository.login(any(), any()) } returns BaseRepository.Result.Success(user)
        coEvery { 
            authRepository.updateUserRole(any(), any()) 
        } returns BaseRepository.Result.Error(
            SecurityException("Permission denied: Cannot update role")
        )

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // Login first
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // When: User attempts operation without permission
        authViewModel.selectRole(UserRole.WORKER)
        advanceUntilIdle()

        // Then: Error state should be PermissionError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be PermissionError", error is ErrorState.PermissionError)
        assertTrue(
            "Error message should mention permission",
            error!!.message.contains("permission", ignoreCase = true)
        )
    }

    // ========== Retry Logic Tests ==========

    @Test
    fun `retry should clear error and attempt operation again`() = runTest {
        // Given: Repository initially fails, then succeeds
        coEvery { authRepository.getCurrentUser() } returns null
        val user = User("user123", "test@example.com", "Test User", UserRole.USER, System.currentTimeMillis())
        
        var attemptCount = 0
        coEvery { 
            authRepository.login(any(), any()) 
        } answers {
            attemptCount++
            if (attemptCount == 1) {
                BaseRepository.Result.Error(IOException("Network error"))
            } else {
                BaseRepository.Result.Success(user)
            }
        }

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: First attempt fails
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Verify error is set
        val errorAfterFirstAttempt = authViewModel.error.value
        assertNotNull("Error should be set after first attempt", errorAfterFirstAttempt)

        // When: Retry the operation
        authViewModel.retry {
            authRepository.login("test@example.com", "password123")
        }
        advanceUntilIdle()

        // Then: Error should be cleared after successful retry
        val errorAfterRetry = authViewModel.error.value
        assertEquals("Error should be cleared after successful retry", null, errorAfterRetry)
    }

    @Test
    fun `clearError should remove error state`() = runTest {
        // Given: ViewModel has an error
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(IOException("Network error"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Verify error is set
        val errorBeforeClear = authViewModel.error.value
        assertNotNull("Error should be set", errorBeforeClear)

        // When: Clear error is called
        authViewModel.clearError()
        advanceUntilIdle()

        // Then: Error should be null
        val errorAfterClear = authViewModel.error.value
        assertEquals("Error should be cleared", null, errorAfterClear)
    }

    @Test
    fun `loading state should be true during operation and false after completion`() = runTest {
        // Given: Repository has a delayed response
        coEvery { authRepository.getCurrentUser() } returns null
        val user = User("user123", "test@example.com", "Test User", UserRole.USER, System.currentTimeMillis())
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Success(user)

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: Operation is started
        authViewModel.login("test@example.com", "password123")

        // Then: Loading should be true during operation
        authViewModel.isLoading.test {
            // Initial state
            assertEquals("Loading should be false initially", false, awaitItem())
            
            // During operation
            assertEquals("Loading should be true during operation", true, awaitItem())
            
            advanceUntilIdle()
            
            // After completion
            assertEquals("Loading should be false after completion", false, awaitItem())
        }
    }

    @Test
    fun `loading state should be false after error`() = runTest {
        // Given: Repository throws error
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(IOException("Network error"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: Operation fails
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Then: Loading should be false after error
        val loading = authViewModel.isLoading.value
        assertEquals("Loading should be false after error", false, loading)
    }

    // ========== Error State Classification Tests ==========

    @Test
    fun `NetworkError should be retryable`() {
        // Given: A network error
        val error = ErrorState.NetworkError(
            message = "Connection error",
            exception = IOException("Network unavailable")
        )

        // Then: Error should be retryable
        assertTrue("NetworkError should be retryable", error.isRetryable())
    }

    @Test
    fun `ValidationError should not be retryable`() {
        // Given: A validation error
        val error = ErrorState.ValidationError(
            message = "Invalid input",
            exception = IllegalArgumentException("Validation failed")
        )

        // Then: Error should not be retryable
        assertTrue("ValidationError should not be retryable", !error.isRetryable())
    }

    @Test
    fun `AuthenticationError should not be retryable`() {
        // Given: An authentication error
        val error = ErrorState.AuthenticationError(
            message = "Authentication failed",
            exception = SecurityException("Invalid credentials")
        )

        // Then: Error should not be retryable
        assertTrue("AuthenticationError should not be retryable", !error.isRetryable())
    }

    // ========== Generic Error Handling Tests ==========

    @Test
    fun `generic exception should be mapped to GenericError`() = runTest {
        // Given: Repository throws generic exception
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(RuntimeException("Something went wrong"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: Operation fails with generic error
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Then: Error state should be GenericError
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be GenericError", error is ErrorState.GenericError)
    }

    @Test
    fun `error message should be user-friendly`() = runTest {
        // Given: Repository throws error with technical message
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { 
            authRepository.login(any(), any()) 
        } returns BaseRepository.Result.Error(IOException("java.net.ConnectException: Failed to connect"))

        authViewModel = AuthViewModel(authRepository)
        advanceUntilIdle()

        // When: Operation fails
        authViewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        // Then: Error message should be user-friendly
        val error = authViewModel.error.value
        assertNotNull("Error should not be null", error)
        assertTrue(
            "Error message should be user-friendly",
            error!!.message.contains("connection", ignoreCase = true) ||
            error.message.contains("internet", ignoreCase = true)
        )
        assertTrue(
            "Error message should not contain technical jargon",
            !error.message.contains("java.net", ignoreCase = true)
        )
    }
}
