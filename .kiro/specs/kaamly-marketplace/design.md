# Design Document: Kaamly Marketplace

## Overview

Kaamly is a hyperlocal micro-task marketplace Android application built with Kotlin and Jetpack Compose. The architecture follows MVVM (Model-View-ViewModel) pattern with Firebase as the backend service. The app features dual role support (User/Worker), real-time bidding, AI-powered task creation, and a complete task lifecycle from posting to completion with mock payment and verification systems.

The design prioritizes hackathon requirements: stable demo flow, polished UI, and clear product logic over enterprise complexity.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│                    (Jetpack Compose UI)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ User Screen  │  │Worker Screen │  │ Shared       │      │
│  │ Composables  │  │ Composables  │  │ Components   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ AuthViewModel│  │ TaskViewModel│  │ BidViewModel │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ProfileVM     │  │ ReviewVM     │  │ LocationVM   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ UserRepo     │  │ TaskRepo     │  │ BidRepo      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ ReviewRepo   │  │ AIRepo       │  │ StorageRepo  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Source Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Firebase   │  │   Firebase   │  │   Firebase   │      │
│  │     Auth     │  │  Firestore   │  │   Storage    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │  AI Service  │  │ Google Maps  │                         │
│  │   (Mock/API) │  │     API      │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

### Navigation Architecture

The app uses Jetpack Compose Navigation with a single-activity architecture:

```
MainActivity
    │
    ├─ AuthNavGraph
    │   ├─ LoginScreen
    │   ├─ SignupScreen
    │   └─ RoleSelectionScreen
    │
    ├─ UserNavGraph
    │   ├─ UserDashboardScreen
    │   ├─ CreateTaskScreen
    │   ├─ TaskDetailScreen
    │   ├─ BidListScreen
    │   ├─ WorkerProfileScreen
    │   ├─ PaymentScreen
    │   └─ ReviewScreen
    │
    ├─ WorkerNavGraph
    │   ├─ WorkerDashboardScreen
    │   ├─ TaskFeedScreen
    │   ├─ TaskDetailScreen
    │   ├─ PlaceBidScreen
    │   ├─ ActiveTasksScreen
    │   └─ VerificationScreen
    │
    └─ SharedNavGraph
        ├─ ProfileScreen
        ├─ SettingsScreen
        └─ NotificationScreen
```

## Components and Interfaces

### 1. Authentication Module

**AuthRepository**
```kotlin
interface AuthRepository {
    suspend fun signUp(email: String, password: String, name: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit>
}
```

**AuthViewModel**
```kotlin
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val authState: StateFlow<AuthState>
    fun signUp(email: String, password: String, name: String)
    fun login(email: String, password: String)
    fun logout()
    fun selectRole(role: UserRole)
}
```

### 2. Profile Module

**UserRepository**
```kotlin
interface UserRepository {
    suspend fun createUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Result<String>
}
```

**WorkerRepository**
```kotlin
interface WorkerRepository {
    suspend fun createWorkerProfile(profile: WorkerProfile): Result<Unit>
    suspend fun getWorkerProfile(workerId: String): Result<WorkerProfile>
    suspend fun updateWorkerProfile(profile: WorkerProfile): Result<Unit>
    suspend fun verifyAadhaar(workerId: String, imageUri: Uri): Result<Unit>
    suspend fun getWorkerRating(workerId: String): Result<WorkerRating>
}
```

**ProfileViewModel**
```kotlin
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    val userProfile: StateFlow<UserProfile?>
    val workerProfile: StateFlow<WorkerProfile?>
    fun updateProfile(profile: Profile)
    fun uploadPhoto(imageUri: Uri)
    fun verifyAadhaar(imageUri: Uri)
}
```

### 3. Task Module

**TaskRepository**
```kotlin
interface TaskRepository {
    suspend fun createTask(task: Task): Result<String>
    suspend fun getTask(taskId: String): Result<Task>
    suspend fun getUserTasks(userId: String): Flow<List<Task>>
    suspend fun getNearbyTasks(location: Location, radius: Double): Flow<List<Task>>
    suspend fun getTasksByCity(city: String): Flow<List<Task>>
    suspend fun updateTaskState(taskId: String, state: TaskState): Result<Unit>
    suspend fun assignWorker(taskId: String, workerId: String): Result<Unit>
    suspend fun uploadTaskPhoto(taskId: String, imageUri: Uri): Result<String>
}
```

**TaskViewModel**
```kotlin
class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val aiRepository: AIRepository
) : ViewModel() {
    val userTasks: StateFlow<List<Task>>
    val nearbyTasks: StateFlow<List<Task>>
    val selectedTask: StateFlow<Task?>
    val taskFilters: StateFlow<TaskFilters>
    
    fun createTask(task: Task)
    fun generateTaskWithAI(briefDescription: String)
    fun updateTaskState(taskId: String, state: TaskState)
    fun applyFilters(filters: TaskFilters)
    fun uploadCompletionPhoto(imageUri: Uri)
}
```

### 4. AI Assistant Module

**AIRepository**
```kotlin
interface AIRepository {
    suspend fun generateTaskDetails(briefDescription: String): Result<AITaskSuggestion>
}

data class AITaskSuggestion(
    val title: String,
    val description: String,
    val category: TaskCategory,
    val requiredEquipment: List<String>,
    val estimatedDuration: Int, // minutes
    val suggestedPriceRange: PriceRange
)
```

**AIService Implementation**
```kotlin
class AIService : AIRepository {
    // Option 1: Mock implementation with predefined templates
    // Option 2: Integration with OpenAI/Gemini API
    override suspend fun generateTaskDetails(briefDescription: String): Result<AITaskSuggestion> {
        // Parse brief description
        // Generate structured output
        // Return formatted task details
    }
}
```

### 5. Bidding Module

**BidRepository**
```kotlin
interface BidRepository {
    suspend fun placeBid(bid: Bid): Result<String>
    suspend fun getBidsForTask(taskId: String): Flow<List<Bid>>
    suspend fun acceptBid(bidId: String): Result<Unit>
    suspend fun getWorkerBids(workerId: String): Flow<List<Bid>>
}
```

**BidViewModel**
```kotlin
class BidViewModel(
    private val bidRepository: BidRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    val taskBids: StateFlow<List<BidWithWorker>>
    val workerBids: StateFlow<List<Bid>>
    
    fun placeBid(taskId: String, amount: Double, message: String)
    fun acceptBid(bidId: String)
    fun loadBidsForTask(taskId: String)
}

data class BidWithWorker(
    val bid: Bid,
    val worker: WorkerProfile
)
```

### 6. Payment Module (Mock)

**PaymentRepository**
```kotlin
interface PaymentRepository {
    suspend fun lockEscrow(taskId: String, amount: Double): Result<EscrowStatus>
    suspend fun releaseEscrow(taskId: String): Result<EscrowStatus>
    suspend fun calculatePlatformFee(amount: Double): Double
}

data class EscrowStatus(
    val taskId: String,
    val amount: Double,
    val platformFee: Double,
    val total: Double,
    val status: String, // "locked" or "released"
    val timestamp: Long
)
```

**PaymentViewModel**
```kotlin
class PaymentViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {
    val escrowStatus: StateFlow<EscrowStatus?>
    
    fun lockEscrow(taskId: String, amount: Double)
    fun releaseEscrow(taskId: String)
    fun calculateTotal(bidAmount: Double): PaymentBreakdown
}

data class PaymentBreakdown(
    val bidAmount: Double,
    val platformFee: Double,
    val total: Double
)
```

### 7. Review Module

**ReviewRepository**
```kotlin
interface ReviewRepository {
    suspend fun submitReview(review: Review): Result<Unit>
    suspend fun getWorkerReviews(workerId: String): Flow<List<Review>>
    suspend fun calculateWorkerRating(workerId: String): Result<WorkerRating>
}

data class Review(
    val id: String,
    val taskId: String,
    val workerId: String,
    val userId: String,
    val starRating: Int, // 1-5
    val textReview: String,
    val taskSpecificRating: Map<String, Int>, // e.g., "punctuality": 5, "quality": 4
    val timestamp: Long
)

data class WorkerRating(
    val overallRating: Double,
    val totalReviews: Int,
    val taskTypeRatings: Map<TaskCategory, Double>,
    val recentReviews: List<Review>
)
```

**ReviewViewModel**
```kotlin
class ReviewViewModel(private val reviewRepository: ReviewRepository) : ViewModel() {
    val workerReviews: StateFlow<List<Review>>
    val workerRating: StateFlow<WorkerRating?>
    
    fun submitReview(taskId: String, workerId: String, rating: Int, text: String, taskRatings: Map<String, Int>)
    fun loadWorkerReviews(workerId: String)
}
```

### 8. Location Module

**LocationRepository**
```kotlin
interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location>
    suspend fun searchLocation(query: String): Result<List<LocationResult>>
    suspend fun getDistanceBetween(loc1: Location, loc2: Location): Double
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val address: String?
)
```

**LocationViewModel**
```kotlin
class LocationViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    val currentLocation: StateFlow<Location?>
    val searchResults: StateFlow<List<LocationResult>>
    
    fun getCurrentLocation()
    fun searchLocation(query: String)
    fun selectLocation(location: Location)
}
```

## Data Models

### Core Data Models

**User**
```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole, // USER, WORKER, or BOTH
    val createdAt: Long
)

enum class UserRole {
    USER, WORKER, BOTH
}
```

**UserProfile**
```kotlin
data class UserProfile(
    val userId: String,
    val name: String,
    val city: String,
    val profilePhotoUrl: String?,
    val aadhaarVerified: Boolean = false,
    val phoneNumber: String?,
    val createdAt: Long
)
```

**WorkerProfile**
```kotlin
data class WorkerProfile(
    val workerId: String,
    val name: String,
    val city: String,
    val profilePhotoUrl: String?,
    val skills: List<String>,
    val bio: String,
    val overallRating: Double = 0.0,
    val totalReviews: Int = 0,
    val taskTypeRatings: Map<TaskCategory, Double> = emptyMap(),
    val aadhaarVerified: Boolean = false,
    val phoneNumber: String?,
    val createdAt: Long
)
```

**Task**
```kotlin
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val requiredEquipment: List<Equipment>,
    val location: Location,
    val scheduledDate: Long,
    val scheduledTime: String,
    val budget: Double,
    val isInstantJob: Boolean,
    val state: TaskState,
    val assignedWorkerId: String? = null,
    val completionPhotoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

enum class TaskCategory {
    CLEANING, REPAIR, DELIVERY, ASSEMBLY, 
    INSTALLATION, MOVING, GARDENING, PAINTING,
    PLUMBING, ELECTRICAL, OTHER
}

enum class TaskState {
    OPEN, IN_PROGRESS, COMPLETED, CANCELLED
}

data class Equipment(
    val name: String,
    val providedBy: EquipmentProvider // USER or WORKER
)

enum class EquipmentProvider {
    USER, WORKER
}
```

**Bid**
```kotlin
data class Bid(
    val id: String,
    val taskId: String,
    val workerId: String,
    val amount: Double,
    val message: String,
    val status: BidStatus,
    val createdAt: Long
)

enum class BidStatus {
    PENDING, ACCEPTED, REJECTED
}
```

**Notification**
```kotlin
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedId: String, // taskId or bidId
    val isRead: Boolean = false,
    val createdAt: Long
)

enum class NotificationType {
    NEW_BID, BID_ACCEPTED, TASK_COMPLETED, 
    WORKER_ARRIVED, REVIEW_RECEIVED
}
```

### Firestore Collection Structure

```
users/
  {userId}/
    - email: String
    - name: String
    - role: String
    - createdAt: Timestamp

userProfiles/
  {userId}/
    - name: String
    - city: String
    - profilePhotoUrl: String
    - aadhaarVerified: Boolean
    - phoneNumber: String
    - createdAt: Timestamp

workerProfiles/
  {workerId}/
    - name: String
    - city: String
    - profilePhotoUrl: String
    - skills: Array<String>
    - bio: String
    - overallRating: Number
    - totalReviews: Number
    - taskTypeRatings: Map<String, Number>
    - aadhaarVerified: Boolean
    - phoneNumber: String
    - createdAt: Timestamp

tasks/
  {taskId}/
    - userId: String
    - title: String
    - description: String
    - category: String
    - requiredEquipment: Array<Object>
    - location: Object
    - scheduledDate: Timestamp
    - scheduledTime: String
    - budget: Number
    - isInstantJob: Boolean
    - state: String
    - assignedWorkerId: String (optional)
    - completionPhotoUrl: String (optional)
    - createdAt: Timestamp
    - updatedAt: Timestamp

bids/
  {bidId}/
    - taskId: String
    - workerId: String
    - amount: Number
    - message: String
    - status: String
    - createdAt: Timestamp

reviews/
  {reviewId}/
    - taskId: String
    - workerId: String
    - userId: String
    - starRating: Number
    - textReview: String
    - taskSpecificRating: Map<String, Number>
    - timestamp: Timestamp

escrow/
  {taskId}/
    - amount: Number
    - platformFee: Number
    - total: Number
    - status: String
    - timestamp: Timestamp

notifications/
  {notificationId}/
    - userId: String
    - title: String
    - message: String
    - type: String
    - relatedId: String
    - isRead: Boolean
    - createdAt: Timestamp
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Role persistence and retrieval
*For any* user who selects a role, storing the role in Firestore and then retrieving it should return the same role value.
**Validates: Requirements 1.3, 1.4**

### Property 2: Profile data round-trip
*For any* profile update (user or worker), persisting the changes to Firestore and then reading back should return the updated values.
**Validates: Requirements 2.5, 3.5**

### Property 3: Photo storage round-trip
*For any* uploaded photo (profile or task completion), storing it in Firebase Storage should return a URL that can be used to retrieve the same image.
**Validates: Requirements 2.2, 10.5**

### Property 4: Task creation with correct initial state
*For any* new task submission, the task should be stored in Firestore with state="Open" and userId matching the creator.
**Validates: Requirements 4.4, 4.5**

### Property 5: Required field validation
*For any* task submission with missing required fields, the Platform should prevent submission and display validation errors.
**Validates: Requirements 4.6**

### Property 6: AI task generation completeness
*For any* brief task description submitted to the AI assistant, the generated output should contain all required fields: title, description, category, equipment, duration, and price range.
**Validates: Requirements 5.2**

### Property 7: AI autofill accuracy
*For any* AI-generated task details, the autofilled form fields should match the corresponding values from the AI response.
**Validates: Requirements 5.3**

### Property 8: Location-based task filtering
*For any* worker location (city or GPS coordinates), the task feed should only display tasks with state="Open" that match the city or fall within the specified radius.
**Validates: Requirements 6.1, 14.2**

### Property 9: Multi-criteria task filtering
*For any* combination of filters (distance, city, instant jobs, category), the displayed task list should only contain tasks matching all applied criteria.
**Validates: Requirements 6.3, 19.5**

### Property 10: Bid data persistence
*For any* submitted bid, it should be stored in Firestore with correct taskId and workerId associations.
**Validates: Requirements 7.3**

### Property 11: Multiple bids per task
*For any* task in "Open" state, the Platform should accept bids from multiple different workers.
**Validates: Requirements 7.4**

### Property 12: Bid acceptance state transition
*For any* accepted bid, the task state should transition to "In Progress" and the task's assignedWorkerId should equal the bid's workerId.
**Validates: Requirements 8.4, 8.5**

### Property 13: Bid acceptance exclusivity
*For any* task with an accepted bid (state="In Progress"), new bid submissions should be rejected.
**Validates: Requirements 8.6**

### Property 14: Payment calculation accuracy
*For any* bid amount, the payment screen should display correct calculations: platformFee = bidAmount * feePercentage, total = bidAmount + platformFee.
**Validates: Requirements 9.1**

### Property 15: Escrow status persistence
*For any* escrow action (lock or release), the status should be stored in Firestore associated with the correct taskId.
**Validates: Requirements 9.3**

### Property 16: Active task visibility for workers
*For any* worker whose bid was accepted, that task should appear in their "Active Tasks" section.
**Validates: Requirements 10.1**

### Property 17: Task state machine integrity
*For any* task, its state should always be one of: Open, In Progress, or Completed, and state transitions should follow the valid flow: Open → In Progress → Completed.
**Validates: Requirements 17.7, 8.4, 11.5**

### Property 18: Real-time UI synchronization
*For any* data change in Firestore (task status, bid submission, photo upload), the UI should update to reflect the change without manual refresh.
**Validates: Requirements 17.8, 11.1, 11.2, 11.3**

### Property 19: Aadhaar verification state update
*For any* successful Aadhaar image upload, the worker's isVerified field should be set to true in Firestore and the verified badge should appear on their profile.
**Validates: Requirements 12.4, 12.5**

### Property 20: Review data persistence and rating calculation
*For any* submitted review, it should be stored in Firestore with correct workerId, and the worker's overall rating should be recalculated as the average of all review star ratings.
**Validates: Requirements 13.3, 13.4**

### Property 21: Review chronological ordering
*For any* list of reviews for a worker, they should be sorted by timestamp in descending order (most recent first).
**Validates: Requirements 13.6**

### Property 22: Role-based dashboard display
*For any* role switch, the Platform should immediately display the appropriate dashboard with role-specific features (worker features for Worker role, user features for User role).
**Validates: Requirements 15.2, 15.3, 15.4**

### Property 23: Profile data isolation by role
*For any* user with both User and Worker roles, the user profile data and worker profile data should be stored separately and not interfere with each other.
**Validates: Requirements 15.5**

### Property 24: Empty state display
*For any* data query that returns zero results (no tasks, no bids, no reviews), the Platform should display an appropriate empty state UI.
**Validates: Requirements 16.4**

### Property 25: Firestore collection organization
*For any* data entity (user, worker, task, bid, review), it should be stored in its designated Firestore collection with the correct structure.
**Validates: Requirements 17.2, 17.3, 17.4, 17.5, 17.6**

### Property 26: Role switching stability
*For any* role switch operation, the app should not crash and all existing data should remain accessible.
**Validates: Requirements 18.3**

### Property 27: Network error handling
*For any* network error during data operations, the Platform should display an appropriate error message to the user.
**Validates: Requirements 1.6, 18.5**

### Property 28: Notification creation for events
*For any* significant event (bid placed, bid accepted, task completed), a notification record should be created in Firestore for the relevant user.
**Validates: Requirements 20.1, 20.2, 20.3**

### Property 29: Unread notification indication
*For any* notification with isRead=false, a visual indicator (badge or highlight) should be displayed in the notification center.
**Validates: Requirements 20.5**

### Property 30: Task detail display completeness
*For any* task or profile view, all required fields should be present and visible in the rendered UI.
**Validates: Requirements 2.4, 3.4, 6.4, 8.2, 19.4**

## Error Handling

### Authentication Errors
- **Invalid credentials**: Display "Invalid email or password" message
- **Network timeout**: Display "Connection timeout. Please check your internet connection"
- **Email already exists**: Display "This email is already registered"
- **Weak password**: Display "Password must be at least 8 characters"

### Task Creation Errors
- **Missing required fields**: Highlight missing fields with red borders and error text
- **Invalid budget**: Display "Budget must be a positive number"
- **Invalid date**: Display "Please select a future date"
- **Location not selected**: Display "Please select a location"
- **AI generation failure**: Display "AI assistant is temporarily unavailable. Please create task manually" and allow manual entry

### Bidding Errors
- **Bid on closed task**: Display "This task is no longer accepting bids"
- **Invalid bid amount**: Display "Bid amount must be positive"
- **Duplicate bid**: Display "You have already placed a bid on this task"
- **Network error during bid**: Display "Failed to submit bid. Please try again"

### File Upload Errors
- **File too large**: Display "Image must be less than 5MB"
- **Invalid file type**: Display "Please upload a valid image file (JPG, PNG)"
- **Upload failure**: Display "Upload failed. Please try again"
- **Storage quota exceeded**: Display "Storage limit reached. Please contact support"

### Data Loading Errors
- **Failed to load tasks**: Display empty state with "Unable to load tasks. Pull to refresh"
- **Failed to load profile**: Display "Unable to load profile. Please try again"
- **Failed to load bids**: Display "Unable to load bids. Please try again"

### State Transition Errors
- **Invalid state transition**: Log error and prevent transition, display "Invalid operation"
- **Concurrent modification**: Retry operation with exponential backoff
- **Permission denied**: Display "You don't have permission to perform this action"

### Location Errors
- **Location permission denied**: Fall back to city-based filtering, display "Location access denied. Using city-based search"
- **GPS unavailable**: Use last known location or city, display "GPS unavailable. Using approximate location"
- **Invalid location**: Display "Please select a valid location"

### General Error Handling Strategy
1. **User-facing errors**: Display clear, actionable error messages
2. **Recoverable errors**: Implement retry logic with exponential backoff (max 3 attempts)
3. **Critical errors**: Log to Firebase Crashlytics and display generic error message
4. **Network errors**: Cache data locally and sync when connection restored
5. **Validation errors**: Prevent submission and highlight specific issues

## Testing Strategy

### Unit Testing

Unit tests will focus on specific examples, edge cases, and integration points. We'll use JUnit 5 and MockK for mocking Firebase services.

**Key Unit Test Areas:**
1. **ViewModel logic**: Test state management, data transformation, error handling
2. **Repository implementations**: Test Firebase interactions with mocked services
3. **Data validation**: Test input validation functions
4. **Business logic**: Test payment calculations, rating calculations, state transitions
5. **Edge cases**: Empty inputs, null values, boundary conditions
6. **Error scenarios**: Network failures, authentication failures, invalid data

**Example Unit Tests:**
```kotlin
class TaskViewModelTest {
    @Test
    fun `createTask with missing title should show validation error`()
    
    @Test
    fun `createTask with valid data should call repository`()
    
    @Test
    fun `AI generation failure should allow manual creation`()
}

class PaymentRepositoryTest {
    @Test
    fun `calculatePlatformFee should return 10% of bid amount`()
    
    @Test
    fun `lockEscrow should store correct data in Firestore`()
}

class BidRepositoryTest {
    @Test
    fun `placeBid on closed task should return error`()
    
    @Test
    fun `multiple bids from same worker should be rejected`()
}
```

### Property-Based Testing

Property-based tests will verify universal properties across randomized inputs. We'll use Kotest Property Testing framework.

**Configuration:**
- Minimum 100 iterations per property test
- Each test tagged with: `Feature: kaamly-marketplace, Property {number}: {property_text}`

**Property Test Implementation:**

```kotlin
class ProfilePropertiesTest : StringSpec({
    "Property 2: Profile data round-trip" {
        // Feature: kaamly-marketplace, Property 2: Profile data round-trip
        checkAll(100, Arb.userProfile()) { profile ->
            val saved = profileRepository.updateProfile(profile)
            val retrieved = profileRepository.getProfile(profile.userId)
            retrieved shouldBe profile
        }
    }
})

class TaskPropertiesTest : StringSpec({
    "Property 4: Task creation with correct initial state" {
        // Feature: kaamly-marketplace, Property 4: Task creation with correct initial state
        checkAll(100, Arb.task()) { task ->
            val taskId = taskRepository.createTask(task)
            val retrieved = taskRepository.getTask(taskId)
            retrieved.state shouldBe TaskState.OPEN
            retrieved.userId shouldBe task.userId
        }
    }
    
    "Property 17: Task state machine integrity" {
        // Feature: kaamly-marketplace, Property 17: Task state machine integrity
        checkAll(100, Arb.task()) { task ->
            val taskId = taskRepository.createTask(task)
            
            // Valid transition: Open -> In Progress
            taskRepository.updateTaskState(taskId, TaskState.IN_PROGRESS)
            taskRepository.getTask(taskId).state shouldBe TaskState.IN_PROGRESS
            
            // Valid transition: In Progress -> Completed
            taskRepository.updateTaskState(taskId, TaskState.COMPLETED)
            taskRepository.getTask(taskId).state shouldBe TaskState.COMPLETED
            
            // Invalid transition: Completed -> Open should fail
            shouldThrow<IllegalStateException> {
                taskRepository.updateTaskState(taskId, TaskState.OPEN)
            }
        }
    }
})

class BidPropertiesTest : StringSpec({
    "Property 11: Multiple bids per task" {
        // Feature: kaamly-marketplace, Property 11: Multiple bids per task
        checkAll(100, Arb.task(), Arb.list(Arb.bid(), 2..5)) { task, bids ->
            val taskId = taskRepository.createTask(task)
            
            bids.forEach { bid ->
                val result = bidRepository.placeBid(bid.copy(taskId = taskId))
                result.isSuccess shouldBe true
            }
            
            val allBids = bidRepository.getBidsForTask(taskId)
            allBids.size shouldBe bids.size
        }
    }
    
    "Property 13: Bid acceptance exclusivity" {
        // Feature: kaamly-marketplace, Property 13: Bid acceptance exclusivity
        checkAll(100, Arb.task(), Arb.bid(), Arb.bid()) { task, bid1, bid2 ->
            val taskId = taskRepository.createTask(task)
            bidRepository.placeBid(bid1.copy(taskId = taskId))
            bidRepository.acceptBid(bid1.id)
            
            // New bid should be rejected
            val result = bidRepository.placeBid(bid2.copy(taskId = taskId))
            result.isFailure shouldBe true
        }
    }
})

class PaymentPropertiesTest : StringSpec({
    "Property 14: Payment calculation accuracy" {
        // Feature: kaamly-marketplace, Property 14: Payment calculation accuracy
        checkAll(100, Arb.double(10.0, 10000.0)) { bidAmount ->
            val breakdown = paymentRepository.calculateTotal(bidAmount)
            val expectedFee = bidAmount * 0.10
            val expectedTotal = bidAmount + expectedFee
            
            breakdown.platformFee shouldBe expectedFee
            breakdown.total shouldBe expectedTotal
        }
    }
})

class ReviewPropertiesTest : StringSpec({
    "Property 20: Review data persistence and rating calculation" {
        // Feature: kaamly-marketplace, Property 20: Review data persistence and rating calculation
        checkAll(100, Arb.list(Arb.review(), 1..10)) { reviews ->
            val workerId = reviews.first().workerId
            
            reviews.forEach { review ->
                reviewRepository.submitReview(review.copy(workerId = workerId))
            }
            
            val rating = reviewRepository.calculateWorkerRating(workerId)
            val expectedRating = reviews.map { it.starRating }.average()
            
            rating.overallRating shouldBe expectedRating
            rating.totalReviews shouldBe reviews.size
        }
    }
})
```

**Custom Generators (Arbitraries):**
```kotlin
fun Arb.Companion.userProfile() = arbitrary {
    UserProfile(
        userId = Arb.uuid().bind().toString(),
        name = Arb.string(5..30).bind(),
        city = Arb.city().bind(),
        profilePhotoUrl = Arb.string().orNull().bind(),
        aadhaarVerified = Arb.bool().bind(),
        phoneNumber = Arb.phoneNumber().orNull().bind(),
        createdAt = Arb.long(1000000000000L, 2000000000000L).bind()
    )
}

fun Arb.Companion.task() = arbitrary {
    Task(
        id = Arb.uuid().bind().toString(),
        userId = Arb.uuid().bind().toString(),
        title = Arb.string(10..100).bind(),
        description = Arb.string(50..500).bind(),
        category = Arb.enum<TaskCategory>().bind(),
        requiredEquipment = Arb.list(Arb.equipment(), 0..5).bind(),
        location = Arb.location().bind(),
        scheduledDate = Arb.long().bind(),
        scheduledTime = Arb.time().bind(),
        budget = Arb.double(10.0, 10000.0).bind(),
        isInstantJob = Arb.bool().bind(),
        state = TaskState.OPEN,
        createdAt = Arb.long().bind(),
        updatedAt = Arb.long().bind()
    )
}

fun Arb.Companion.bid() = arbitrary {
    Bid(
        id = Arb.uuid().bind().toString(),
        taskId = Arb.uuid().bind().toString(),
        workerId = Arb.uuid().bind().toString(),
        amount = Arb.double(10.0, 10000.0).bind(),
        message = Arb.string(10..200).bind(),
        status = BidStatus.PENDING,
        createdAt = Arb.long().bind()
    )
}
```

### Integration Testing

Integration tests will verify end-to-end flows using Firebase Emulator Suite:

1. **Complete task flow**: User creates task → Worker bids → User accepts → Worker completes → User reviews
2. **Role switching**: User switches between User and Worker roles without data loss
3. **Real-time updates**: Changes in Firestore trigger UI updates
4. **Authentication flow**: Sign up → Role selection → Profile creation
5. **AI task creation**: Brief description → AI generation → Form autofill → Task creation

### UI Testing

Compose UI tests will verify user interactions:

1. **Navigation flows**: Screen transitions work correctly
2. **Form validation**: Error messages appear for invalid inputs
3. **Button states**: Buttons enable/disable based on state
4. **List rendering**: Tasks, bids, and reviews display correctly
5. **Empty states**: Proper empty state UI when no data

### Testing Priorities for Hackathon

Given the 2-day timeline, prioritize:
1. ✅ Core flow integration test (sign up → create → bid → accept → complete → review)
2. ✅ Critical property tests (state machine, data persistence, calculations)
3. ✅ Key unit tests (validation, business logic)
4. ⚠️ UI tests (if time permits)
5. ⚠️ Comprehensive property tests (if time permits)

## Implementation Notes

### Firebase Setup

**Firestore Security Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Anyone can read profiles, only owner can write
    match /userProfiles/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /workerProfiles/{workerId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == workerId;
    }
    
    // Tasks: owner can write, authenticated users can read
    match /tasks/{taskId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        (resource.data.userId == request.auth.uid || 
         resource.data.assignedWorkerId == request.auth.uid);
    }
    
    // Bids: worker can create, task owner can read
    match /bids/{bidId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
    }
    
    // Reviews: anyone can read, task owner can create
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
    }
  }
}
```

**Firebase Storage Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_photos/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /task_photos/{taskId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    match /aadhaar_images/{workerId}/{fileName} {
      allow read: if request.auth != null && request.auth.uid == workerId;
      allow write: if request.auth != null && request.auth.uid == workerId;
    }
  }
}
```

### Dependency Injection

Use Hilt for dependency injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, firestore)
    
    // ... other repository providers
}
```

### State Management

Use Kotlin StateFlow for reactive state management:

```kotlin
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getNearbyTasks(location, radius)
                .catch { e -> _uiState.value = TaskUiState.Error(e.message) }
                .collect { tasks -> _uiState.value = TaskUiState.Success(tasks) }
        }
    }
}

sealed class TaskUiState {
    object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String?) : TaskUiState()
}
```

### AI Integration Options

**Option 1: Mock AI (Recommended for Hackathon)**
```kotlin
class MockAIService : AIRepository {
    private val templates = mapOf(
        "drill" to AITaskSuggestion(
            title = "Drill Holes in Wall",
            description = "Need professional drilling service for mounting...",
            category = TaskCategory.REPAIR,
            requiredEquipment = listOf("Power drill", "Drill bits", "Safety goggles"),
            estimatedDuration = 30,
            suggestedPriceRange = PriceRange(200.0, 500.0)
        ),
        // ... more templates
    )
    
    override suspend fun generateTaskDetails(brief: String): Result<AITaskSuggestion> {
        delay(1000) // Simulate API call
        val keyword = brief.lowercase().split(" ").firstOrNull { templates.containsKey(it) }
        return keyword?.let { Result.success(templates[it]!!) }
            ?: Result.success(generateGenericTask(brief))
    }
}
```

**Option 2: Real AI Integration**
```kotlin
class OpenAIService(private val apiKey: String) : AIRepository {
    override suspend fun generateTaskDetails(brief: String): Result<AITaskSuggestion> {
        val prompt = """
            Generate a professional task description for: "$brief"
            Return JSON with: title, description, category, equipment, duration, priceRange
        """.trimIndent()
        
        // Call OpenAI API
        // Parse structured response
        // Return AITaskSuggestion
    }
}
```

### Performance Optimizations

1. **Pagination**: Load tasks in batches of 20
2. **Image compression**: Compress photos before upload (max 1MB)
3. **Caching**: Cache user/worker profiles locally
4. **Lazy loading**: Load task details only when viewed
5. **Debouncing**: Debounce search and filter inputs (300ms)

### Demo Flow Optimization

For smooth 2-minute demo:
1. **Pre-populate test data**: Create sample tasks and workers
2. **Skip animations**: Reduce animation durations in demo mode
3. **Auto-fill forms**: Provide "Use demo data" buttons
4. **Quick switches**: Add floating action button for role switching
5. **Clear indicators**: Show current role prominently

## Conclusion

This design provides a comprehensive architecture for the Kaamly marketplace app, balancing hackathon constraints with production-quality patterns. The MVVM architecture with Firebase backend enables rapid development while maintaining clean separation of concerns. The dual testing approach (unit + property-based) ensures correctness while the mock systems (AI, payments, verification) allow for impressive demos without complex integrations.

Key success factors:
- Clear data models and state management
- Comprehensive error handling
- Real-time synchronization
- Stable demo flow
- Professional UI with Jetpack Compose
