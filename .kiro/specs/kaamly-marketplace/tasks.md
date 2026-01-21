# Implementation Plan: Kaamly Marketplace

## Overview

This implementation plan breaks down the Kaamly marketplace app into discrete, incremental coding tasks. Each task builds on previous work to create a complete hyperlocal micro-task marketplace with dual role support, bidding system, AI task creation, and mock payment/verification systems. The plan prioritizes the core demo flow to ensure a stable 2-minute demonstration.

## Tasks

- [x] 1. Project setup and Firebase configuration
  - Initialize Hilt dependency injection
  - Configure Firebase Authentication, Firestore, and Storage
  - Set up Firestore security rules and Storage rules
  - Create base MVVM structure (base ViewModel, Repository interfaces)
  - Configure Jetpack Compose navigation
  - _Requirements: 1.5, 17.1_

- [x] 2. Authentication module
  - [x] 2.1 Implement AuthRepository and AuthViewModel
    - Create sign up, login, logout functions
    - Implement user session management
    - _Requirements: 1.1, 1.5_

  - [ ]* 2.2 Write property test for role persistence
    - **Property 1: Role persistence and retrieval**
    - **Validates: Requirements 1.3, 1.4**

  - [x] 2.3 Create authentication UI screens
    - Build LoginScreen with email/password fields
    - Build SignupScreen with name, email, password fields
    - Build RoleSelectionScreen with "Post tasks" and "Work" options
    - Implement navigation between auth screens
    - _Requirements: 1.1, 1.2_

  - [ ]* 2.4 Write unit tests for authentication
    - Test invalid credentials error handling
    - Test successful signup flow
    - Test role selection storage
    - _Requirements: 1.6_

- [x] 3. User and Worker profile module
  - [x] 3.1 Implement UserRepository and WorkerRepository
    - Create profile CRUD operations
    - Implement photo upload to Firebase Storage
    - Implement Aadhaar verification (mock)
    - _Requirements: 2.1, 2.2, 3.1, 12.4_

  - [ ]* 3.2 Write property tests for profile operations
    - **Property 2: Profile data round-trip**
    - **Property 3: Photo storage round-trip**
    - **Property 19: Aadhaar verification state update**
    - **Validates: Requirements 2.5, 3.5, 2.2, 10.5, 12.4, 12.5**

  - [x] 3.3 Create ProfileViewModel
    - Implement profile state management
    - Handle photo upload with loading states
    - Implement verification flow
    - _Requirements: 2.5, 3.5, 12.1_

  - [x] 3.4 Build profile UI screens
    - Create UserProfileScreen with name, city, photo fields
    - Create WorkerProfileScreen with skills, bio, ratings display
    - Implement photo picker and upload UI
    - Create VerificationScreen with Aadhaar upload
    - Display verification badge when isVerified=true
    - _Requirements: 2.1, 2.3, 3.1, 3.2, 3.3, 12.2, 12.3, 12.5_

  - [ ]* 3.5 Write unit tests for profile module
    - Test profile validation
    - Test photo upload error handling
    - Test verification flow
    - _Requirements: 2.4, 3.4_

- [x] 4. Checkpoint - Ensure authentication and profiles work
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Task creation module
  - [x] 5.1 Implement TaskRepository
    - Create task CRUD operations
    - Implement task state management
    - Implement location-based task queries
    - _Requirements: 4.4, 4.5, 6.1, 14.2_

  - [ ]* 5.2 Write property tests for task operations
    - **Property 4: Task creation with correct initial state**
    - **Property 5: Required field validation**
    - **Property 17: Task state machine integrity**
    - **Validates: Requirements 4.4, 4.5, 4.6, 17.7, 8.4, 11.5**

  - [x] 5.3 Create TaskViewModel
    - Implement task creation logic
    - Implement task filtering and search
    - Handle task state transitions
    - _Requirements: 4.4, 6.3, 8.4_

  - [x] 5.4 Build CreateTaskScreen
    - Create form with title, description, category, equipment fields
    - Implement location picker (map or city selection)
    - Add date/time pickers
    - Add budget input field
    - Add "Instant job" toggle
    - Implement form validation with error messages
    - _Requirements: 4.1, 4.2, 4.3, 4.6_

  - [ ]* 5.5 Write unit tests for task creation
    - Test form validation
    - Test task creation with valid data
    - Test invalid date handling
    - _Requirements: 4.6_

- [x] 6. AI Task Assistant module
  - [x] 6.1 Implement AIRepository with mock service
    - Create template-based AI task generation
    - Implement keyword matching for common tasks
    - Generate structured AITaskSuggestion output
    - _Requirements: 5.2_

  - [ ]* 6.2 Write property tests for AI generation
    - **Property 6: AI task generation completeness**
    - **Property 7: AI autofill accuracy**
    - **Validates: Requirements 5.2, 5.3**

  - [x] 6.3 Integrate AI assistant into CreateTaskScreen
    - Add "Create with AI" button
    - Create AI input dialog
    - Implement form autofill from AI response
    - Handle AI generation errors with fallback
    - Ensure autofilled fields are editable
    - _Requirements: 5.1, 5.3, 5.4, 5.5_

  - [ ]* 6.4 Write unit tests for AI integration
    - Test AI generation failure handling
    - Test form autofill
    - Test field editability after autofill
    - _Requirements: 5.5_

- [ ] 7. Worker task discovery module
  - [x] 7.1 Implement LocationRepository
    - Create location detection (GPS or city)
    - Implement distance calculation
    - Handle location permission denial
    - _Requirements: 14.1, 14.4_

  - [ ]* 7.2 Write property tests for location and filtering
    - **Property 8: Location-based task filtering**
    - **Property 9: Multi-criteria task filtering**
    - **Validates: Requirements 6.1, 14.2, 6.3, 19.5**

  - [x] 7.3 Create WorkerDashboardScreen
    - Display nearby tasks in list/card format
    - Implement filter UI (distance, city, instant jobs, category)
    - Show task summary: title, budget, location, category
    - Implement pull-to-refresh
    - Display empty state when no tasks available
    - _Requirements: 6.1, 6.2, 6.5, 16.4_

  - [x] 7.4 Create TaskDetailScreen for workers
    - Display full task details
    - Show equipment requirements
    - Display location and distance
    - Add "Place Bid" button
    - _Requirements: 6.4, 19.4_

  - [ ]* 7.5 Write unit tests for task discovery
    - Test filter application
    - Test empty state display
    - Test distance calculation
    - _Requirements: 6.3, 16.4_

- [x] 8. Checkpoint - Ensure task creation and discovery work
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Bidding system module
  - [x] 9.1 Implement BidRepository
    - Create bid submission
    - Implement bid queries (by task, by worker)
    - Implement bid acceptance logic
    - _Requirements: 7.3, 8.1, 8.4_

  - [ ]* 9.2 Write property tests for bidding
    - **Property 10: Bid data persistence**
    - **Property 11: Multiple bids per task**
    - **Property 12: Bid acceptance state transition**
    - **Property 13: Bid acceptance exclusivity**
    - **Validates: Requirements 7.3, 7.4, 8.4, 8.5, 8.6**

  - [x] 9.3 Create BidViewModel
    - Implement bid placement logic
    - Implement bid acceptance logic
    - Load bids with worker profiles
    - _Requirements: 7.3, 8.1, 8.4_

  - [x] 9.4 Build PlaceBidScreen
    - Create bid form with amount and message fields
    - Implement bid amount validation
    - Show task summary
    - _Requirements: 7.1, 7.2_

  - [x] 9.5 Build BidListScreen for users
    - Display all bids for a task
    - Show worker profile, rating, bid amount, message
    - Add "View Profile" button for each bid
    - Add "Accept Bid" button
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ]* 9.6 Write unit tests for bidding
    - Test bid validation
    - Test bid on closed task rejection
    - Test duplicate bid prevention
    - _Requirements: 7.3, 8.6_

- [x] 10. Mock payment and escrow module
  - [x] 10.1 Implement PaymentRepository
    - Create escrow lock function (mock)
    - Create escrow release function (mock)
    - Implement platform fee calculation (10%)
    - _Requirements: 9.1, 9.3, 9.4_

  - [ ]* 10.2 Write property tests for payment
    - **Property 14: Payment calculation accuracy**
    - **Property 15: Escrow status persistence**
    - **Validates: Requirements 9.1, 9.3**

  - [x] 10.3 Create PaymentViewModel
    - Implement payment breakdown calculation
    - Handle escrow lock/release
    - _Requirements: 9.1, 9.3_

  - [x] 10.4 Build PaymentScreen
    - Display bid amount, platform fee, total
    - Show "Amount locked in escrow (mock)" status
    - Add confirmation button
    - Display mock payment disclaimer
    - _Requirements: 9.1, 9.2, 9.5_

  - [ ]* 10.5 Write unit tests for payment
    - Test platform fee calculation
    - Test payment breakdown
    - _Requirements: 9.1_

- [x] 11. Task execution flow module
  - [x] 11.1 Implement task execution in TaskRepository
    - Add mark arrived function
    - Add upload completion photo function
    - Add mark completed function
    - Add approve completion function
    - _Requirements: 10.3, 10.5, 10.7, 11.5_

  - [ ]* 11.2 Write property tests for task execution
    - **Property 16: Active task visibility for workers**
    - **Property 18: Real-time UI synchronization**
    - **Validates: Requirements 10.1, 17.8, 11.1, 11.2, 11.3**

  - [x] 11.3 Create ActiveTasksScreen for workers
    - Display tasks where worker's bid was accepted
    - Add "Mark Arrived" button
    - Add "Upload Photo" button with image picker
    - Add "Mark Completed" button
    - Show task progress status
    - _Requirements: 10.1, 10.2, 10.4, 10.6_

  - [x] 11.4 Create TaskProgressScreen for users
    - Display worker arrival status
    - Show completion photo when uploaded
    - Display completion request
    - Add "Approve Task" button
    - Show escrow release status
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.6_

  - [ ]* 11.5 Write unit tests for task execution
    - Test state transitions
    - Test photo upload
    - Test completion approval
    - _Requirements: 10.3, 10.7, 11.5_

- [x] 12. Checkpoint - Ensure bidding and task execution work
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Review and rating module
  - [x] 13.1 Implement ReviewRepository
    - Create review submission
    - Implement worker review queries
    - Implement rating calculation
    - _Requirements: 13.3, 13.4, 13.5_

  - [ ]* 13.2 Write property tests for reviews
    - **Property 20: Review data persistence and rating calculation**
    - **Property 21: Review chronological ordering**
    - **Validates: Requirements 13.3, 13.4, 13.6**

  - [x] 13.3 Create ReviewViewModel
    - Implement review submission logic
    - Load worker reviews and ratings
    - Calculate overall and task-specific ratings
    - _Requirements: 13.3, 13.4, 13.5_

  - [x] 13.4 Build ReviewScreen
    - Create review form with star rating (1-5)
    - Add text review input
    - Add task-specific rating fields (punctuality, quality, etc.)
    - Display task and worker information
    - _Requirements: 13.1, 13.2_

  - [x] 13.5 Update WorkerProfileScreen to display reviews
    - Show overall rating prominently
    - Display total review count
    - Show task-type ratings breakdown
    - List recent reviews with ratings and text
    - _Requirements: 13.5, 13.6_

  - [ ]* 13.6 Write unit tests for reviews
    - Test rating calculation
    - Test review sorting
    - Test review submission
    - _Requirements: 13.4, 13.6_

- [x] 14. Role switching and navigation module
  - [x] 14.1 Implement role switching in AuthRepository
    - Add update role function
    - Handle role state changes
    - _Requirements: 15.1, 15.2_

  - [ ]* 14.2 Write property tests for role switching
    - **Property 22: Role-based dashboard display**
    - **Property 23: Profile data isolation by role**
    - **Property 26: Role switching stability**
    - **Validates: Requirements 15.2, 15.3, 15.4, 15.5, 18.3**

  - [x] 14.3 Create UserDashboardScreen
    - Display user's posted tasks
    - Show task states (Open, In Progress, Completed)
    - Add "Create Task" button
    - Display received bids count per task
    - _Requirements: 15.4_

  - [x] 14.4 Implement role switching UI
    - Add role switcher in app bar or settings
    - Update navigation based on current role
    - Ensure smooth transition between dashboards
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

  - [ ]* 14.5 Write unit tests for role switching
    - Test role update
    - Test dashboard routing
    - Test data isolation
    - _Requirements: 15.5, 18.3_

- [x] 15. Notification system module
  - [x] 15.1 Implement NotificationRepository
    - Create notification creation functions
    - Implement notification queries
    - Add mark as read function
    - _Requirements: 20.1, 20.2, 20.3_

  - [ ]* 15.2 Write property tests for notifications
    - **Property 28: Notification creation for events**
    - **Property 29: Unread notification indication**
    - **Validates: Requirements 20.1, 20.2, 20.3, 20.5**

  - [x] 15.3 Create NotificationViewModel
    - Load user notifications
    - Handle mark as read
    - Track unread count
    - _Requirements: 20.4, 20.5_

  - [x] 15.4 Build NotificationScreen
    - Display notifications in list
    - Show notification type icons
    - Display unread indicator
    - Implement tap to navigate to related task/bid
    - _Requirements: 20.4, 20.5_

  - [x] 15.5 Add notification badge to app bar
    - Show unread count badge
    - Update badge in real-time
    - _Requirements: 20.5_

  - [ ]* 15.6 Write unit tests for notifications
    - Test notification creation
    - Test unread count
    - Test mark as read
    - _Requirements: 20.1, 20.2, 20.3_

- [x] 16. UI polish and error handling
  - [x] 16.1 Implement comprehensive error handling
    - Add error handling to all ViewModels
    - Create error message display composables
    - Implement retry logic for network errors
    - Add loading states to all screens
    - _Requirements: 1.6, 18.5_

  - [ ]* 16.2 Write property tests for error handling and UI
    - **Property 24: Empty state display**
    - **Property 27: Network error handling**
    - **Property 30: Task detail display completeness**
    - **Validates: Requirements 16.4, 1.6, 18.5, 2.4, 3.4, 6.4, 8.2, 19.4**

  - [x] 16.3 Create reusable UI components
    - Build TaskCard composable
    - Build BidCard composable
    - Build ProfileHeader composable
    - Build EmptyState composable
    - Build LoadingIndicator composable
    - Build ErrorMessage composable
    - _Requirements: 16.4_

  - [x] 16.4 Apply Material Design 3 theming
    - Define color scheme
    - Set up typography
    - Configure component styles
    - Ensure proper contrast ratios
    - _Requirements: 16.2, 16.3, 16.7_

  - [x] 16.5 Add animations and transitions
    - Implement screen transitions
    - Add button press animations
    - Add list item animations
    - Add loading animations
    - _Requirements: 16.5_

  - [x] 16.6 Write unit tests for error handling

    - Test network error display
    - Test validation error display
    - Test retry logic
    - _Requirements: 1.6, 18.5_

- [x] 17. Checkpoint - Ensure complete app flow works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 18. Demo optimization and final integration
  - [x] 18.1 Create demo data seeding
    - Add sample users and workers
    - Create sample tasks
    - Add sample bids and reviews
    - _Requirements: 18.1_

  - [x] 18.2 Add demo mode features
    - Create "Use demo data" buttons for forms
    - Add quick role switch floating action button
    - Implement current role indicator
    - _Requirements: 18.1, 18.4_

  - [x] 18.3 Optimize demo flow
    - Test complete flow: signup → create task → switch → bid → switch → accept → complete → review
    - Ensure flow completes in under 2 minutes
    - Fix any navigation issues
    - Ensure all real-time updates work
    - _Requirements: 18.1, 18.2_

  - [ ]* 18.4 Write integration test for complete flow
    - Test end-to-end demo flow
    - Verify all state transitions
    - Verify data persistence
    - _Requirements: 18.1_

  - [x] 18.5 Final polish and bug fixes
    - Test on different screen sizes
    - Fix any UI glitches
    - Ensure all error messages are clear
    - Verify all empty states work
    - Test offline behavior
    - _Requirements: 18.3, 18.5, 18.6_

- [ ] 19. Final checkpoint - Complete demo ready
  - Run complete demo flow multiple times
  - Ensure all tests pass
  - Verify app stability
  - Ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties across randomized inputs
- Unit tests validate specific examples, edge cases, and error conditions
- The implementation prioritizes the core demo flow to ensure a stable 2-minute demonstration
- Firebase Emulator Suite should be used for local development and testing
- All Firestore operations should use real-time listeners for automatic UI updates
- Mock implementations (AI, payments, verification) should be clearly marked in UI
