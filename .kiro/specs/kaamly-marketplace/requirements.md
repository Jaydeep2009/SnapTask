# Requirements Document

## Introduction

Kaamly is a hyperlocal on-demand micro-task marketplace Android application that connects users who need small jobs done with nearby workers who want to earn money by completing tasks. The app features dual dashboards (user and worker), a bidding system, AI-powered task creation, mock trust verification, and mock payment systems. Built for a 2-day hackathon MVP, the app prioritizes working flows, polished UI, and demo stability.

## Glossary

- **User**: A person who posts tasks and needs work done
- **Worker**: A person who completes tasks for payment
- **Task**: A job posted by a user that needs to be completed
- **Bid**: A worker's offer to complete a task at a specific price
- **Escrow**: Mock payment holding system that locks funds until task completion
- **AI_Task_Assistant**: AI-powered feature that generates task details from brief descriptions
- **Aadhaar_Verification**: Mock identity verification system (UI only)
- **Hyperlocal**: Tasks and workers within the same city or nearby radius
- **Instant_Job**: Ultra-short duration tasks (10-60 minutes)
- **Task_State**: Current status of a task (Open, In Progress, Completed)
- **Platform**: The Kaamly application system
- **Firebase_Auth**: Firebase Authentication service for user management
- **Firestore**: Firebase Firestore database for data storage
- **Jetpack_Compose**: Modern Android UI toolkit

## Requirements

### Requirement 1: User Authentication and Role Management

**User Story:** As a new user, I want to sign up and choose my role (User or Worker), so that I can access the appropriate features for posting or completing tasks.

#### Acceptance Criteria

1. WHEN a new user opens the app, THE Platform SHALL display a login/signup screen
2. WHEN a user completes signup, THE Platform SHALL display a role selection screen with options "I want to post tasks" and "I want to work"
3. WHEN a user selects a role, THE Platform SHALL store the role in Firestore associated with their account
4. WHEN a user logs in, THE Platform SHALL retrieve their role from Firestore and display the appropriate dashboard
5. THE Platform SHALL use Firebase_Auth for authentication operations
6. WHEN authentication fails, THE Platform SHALL display appropriate error messages to the user

### Requirement 2: User Profile Management

**User Story:** As a user, I want to create and manage my profile, so that others can identify me and trust my account.

#### Acceptance Criteria

1. WHEN a user creates their profile, THE Platform SHALL collect name, city, and profile photo
2. WHEN a user uploads a profile photo, THE Platform SHALL store it in Firebase_Storage
3. THE Platform SHALL display Aadhaar verification status (mock) on user profiles
4. WHEN viewing a profile, THE Platform SHALL display all profile information clearly
5. WHEN a user updates their profile, THE Platform SHALL persist changes to Firestore immediately

### Requirement 3: Worker Profile Management

**User Story:** As a worker, I want to create a detailed profile with my skills and experience, so that users can evaluate my qualifications for their tasks.

#### Acceptance Criteria

1. WHEN a worker creates their profile, THE Platform SHALL collect name, city, profile photo, skills, and bio
2. THE Platform SHALL display overall rating and task-specific ratings on worker profiles
3. THE Platform SHALL display an Aadhaar verified badge when isVerified equals true
4. WHEN viewing a worker profile, THE Platform SHALL display all profile information, ratings, and reviews
5. WHEN a worker updates their profile, THE Platform SHALL persist changes to Firestore immediately

### Requirement 4: Task Creation

**User Story:** As a user, I want to create tasks with detailed information, so that workers understand what needs to be done and can bid appropriately.

#### Acceptance Criteria

1. WHEN a user creates a task, THE Platform SHALL collect title, description, category, required equipment, location, date/time, and budget
2. WHEN selecting location, THE Platform SHALL provide a map picker or city selection interface
3. THE Platform SHALL provide a toggle for marking tasks as "Instant/ultra-short job (10-60 mins)"
4. WHEN a user submits a task, THE Platform SHALL store it in Firestore with state "Open"
5. WHEN a task is created, THE Platform SHALL associate it with the user's ID
6. THE Platform SHALL validate that all required fields are filled before allowing task submission

### Requirement 5: AI Task Assistant

**User Story:** As a user, I want AI assistance to create professional task descriptions, so that I can quickly post well-structured tasks without writing everything manually.

#### Acceptance Criteria

1. WHEN a user clicks "Create task with AI", THE Platform SHALL display an input field for a brief task description
2. WHEN a user enters a brief description and submits, THE AI_Task_Assistant SHALL generate a full professional task description, required equipment, estimated duration, suggested price range, and task category
3. WHEN AI generation completes, THE Platform SHALL autofill the task form fields with generated content
4. WHEN task fields are autofilled, THE Platform SHALL allow the user to edit any field before posting
5. IF AI generation fails, THEN THE Platform SHALL display an error message and allow manual task creation

### Requirement 6: Worker Task Discovery

**User Story:** As a worker, I want to view nearby tasks and filter them, so that I can find jobs that match my location, skills, and availability.

#### Acceptance Criteria

1. WHEN a worker opens their dashboard, THE Platform SHALL display tasks with state "Open" from the same city or within a specified radius
2. THE Platform SHALL provide filters for distance, city, and instant jobs
3. WHEN a worker applies filters, THE Platform SHALL update the task list to show only matching tasks
4. WHEN a worker taps a task, THE Platform SHALL display full task details including budget, description, equipment needs, and location
5. THE Platform SHALL display tasks in a clear, organized list with relevant summary information

### Requirement 7: Bidding System

**User Story:** As a worker, I want to submit bids on tasks with my proposed price and message, so that I can compete for jobs and explain my qualifications.

#### Acceptance Criteria

1. WHEN viewing a task, THE Platform SHALL provide a "Send Request" or "Place Bid" button for workers
2. WHEN a worker places a bid, THE Platform SHALL collect bid amount and a short message
3. WHEN a bid is submitted, THE Platform SHALL store it in Firestore associated with the task and worker
4. THE Platform SHALL allow multiple workers to bid on the same task
5. WHEN a bid is submitted, THE Platform SHALL notify the task owner (mock notification acceptable)

### Requirement 8: Bid Review and Worker Selection

**User Story:** As a user, I want to review all bids on my task and see worker profiles, so that I can choose the best worker for my job.

#### Acceptance Criteria

1. WHEN a user views their posted task, THE Platform SHALL display all received bids
2. FOR each bid, THE Platform SHALL display worker profile, ratings, task-specific experience, bid amount, and message
3. WHEN a user taps a worker's bid, THE Platform SHALL display the full worker profile
4. WHEN a user accepts a bid, THE Platform SHALL update the task state to "In Progress"
5. WHEN a bid is accepted, THE Platform SHALL associate the selected worker with the task
6. WHEN a bid is accepted, THE Platform SHALL prevent other workers from bidding on that task

### Requirement 9: Mock Escrow Payment System

**User Story:** As a user, I want to see a payment flow when accepting a worker, so that the demo shows how funds would be secured until task completion.

#### Acceptance Criteria

1. WHEN a user accepts a worker, THE Platform SHALL display a payment screen showing bid amount, platform fee, and total
2. WHEN payment is confirmed, THE Platform SHALL display status "Amount locked in escrow (mock)"
3. THE Platform SHALL store escrow status in Firestore associated with the task
4. WHEN a user approves task completion, THE Platform SHALL display status "Escrow released to worker (mock)"
5. THE Platform SHALL clearly indicate that no real payments are processed

### Requirement 10: Task Execution Flow - Worker Side

**User Story:** As a worker, I want to update task status and upload proof of completion, so that users know I've arrived and completed the work.

#### Acceptance Criteria

1. WHEN a worker's bid is accepted, THE Platform SHALL display the task in the worker's "Active Tasks" section
2. THE Platform SHALL provide a "Mark Arrived" button for workers
3. WHEN a worker marks arrived, THE Platform SHALL update the task status in Firestore
4. THE Platform SHALL provide an "Upload After-Work Photo" feature
5. WHEN a worker uploads a photo, THE Platform SHALL store it in Firebase_Storage
6. THE Platform SHALL provide a "Mark Task Completed" button
7. WHEN a worker marks task completed, THE Platform SHALL update task status and notify the user

### Requirement 11: Task Execution Flow - User Side

**User Story:** As a user, I want to see task progress and approve completion, so that I can verify work is done before releasing payment.

#### Acceptance Criteria

1. WHEN a worker marks arrived, THE Platform SHALL display arrival status to the user
2. WHEN a worker uploads completion photo, THE Platform SHALL display the photo to the user
3. WHEN a worker marks task completed, THE Platform SHALL display a completion request to the user
4. THE Platform SHALL provide an "Approve Task" button for users
5. WHEN a user approves task completion, THE Platform SHALL update task state to "Completed"
6. WHEN task is approved, THE Platform SHALL trigger mock escrow release

### Requirement 12: Mock Aadhaar Verification

**User Story:** As a worker, I want to verify my identity through a mock Aadhaar process, so that users can trust my profile.

#### Acceptance Criteria

1. WHEN a worker accesses their profile, THE Platform SHALL display a "Verify Aadhaar" button if not verified
2. WHEN a worker clicks verify, THE Platform SHALL provide an image upload interface
3. WHEN a worker uploads an Aadhaar image, THE Platform SHALL display a loading animation
4. WHEN upload completes, THE Platform SHALL set isVerified to true in Firestore
5. WHEN verification is complete, THE Platform SHALL display a ✅ Verified badge on the worker profile
6. THE Platform SHALL clearly indicate this is UI/demo only with no real verification

### Requirement 13: Ratings and Reviews

**User Story:** As a user, I want to rate and review workers after task completion, so that other users can make informed decisions.

#### Acceptance Criteria

1. WHEN a task is completed, THE Platform SHALL prompt the user to provide a rating and review
2. THE Platform SHALL collect star rating (1-5), text review, and task-specific rating
3. WHEN a review is submitted, THE Platform SHALL store it in Firestore associated with the worker
4. THE Platform SHALL calculate and update the worker's overall rating
5. WHEN viewing a worker profile, THE Platform SHALL display overall rating, reviews list, and task-type ratings
6. THE Platform SHALL display reviews in chronological order with most recent first

### Requirement 14: Location-Based Task Discovery

**User Story:** As a worker, I want to see tasks near my location, so that I can find jobs I can physically reach.

#### Acceptance Criteria

1. THE Platform SHALL determine worker location based on city or current GPS coordinates
2. WHEN displaying tasks to workers, THE Platform SHALL filter by city match or radius from current location
3. THE Platform SHALL display distance or location information for each task
4. WHEN location permissions are denied, THE Platform SHALL fall back to city-based filtering
5. THE Platform SHALL allow workers to manually set their preferred work location

### Requirement 15: Role Switching

**User Story:** As a user, I want to switch between User and Worker roles, so that I can both post tasks and complete tasks for others.

#### Acceptance Criteria

1. THE Platform SHALL provide a role switching interface in settings or profile
2. WHEN a user switches roles, THE Platform SHALL update the displayed dashboard immediately
3. WHEN switching to Worker role, THE Platform SHALL display worker-specific features and task feed
4. WHEN switching to User role, THE Platform SHALL display user-specific features and posted tasks
5. THE Platform SHALL maintain separate profile data for User and Worker roles

### Requirement 16: User Interface and Design

**User Story:** As a user, I want a modern, professional interface, so that the app feels trustworthy and easy to use.

#### Acceptance Criteria

1. THE Platform SHALL use Jetpack_Compose for all UI components
2. THE Platform SHALL implement a modern SaaS-style design with clean cards and smooth navigation
3. THE Platform SHALL use professional colors and clear status indicators
4. THE Platform SHALL display proper empty states when no data is available
5. THE Platform SHALL provide smooth animations and transitions between screens
6. THE Platform SHALL follow Material Design 3 guidelines
7. THE Platform SHALL ensure all text is readable with appropriate contrast ratios

### Requirement 17: Data Persistence and State Management

**User Story:** As a developer, I want proper data architecture, so that the app is maintainable and scalable.

#### Acceptance Criteria

1. THE Platform SHALL use MVVM architecture pattern
2. THE Platform SHALL store user data in Firestore collection "users"
3. THE Platform SHALL store worker data in Firestore collection "workers"
4. THE Platform SHALL store tasks in Firestore collection "tasks"
5. THE Platform SHALL store bids in Firestore collection "bids"
6. THE Platform SHALL store reviews in Firestore collection "reviews"
7. THE Platform SHALL maintain task states: Open, In Progress, Completed
8. WHEN data changes in Firestore, THE Platform SHALL update UI in real-time using Firestore listeners

### Requirement 18: Demo Flow Stability

**User Story:** As a hackathon judge, I want to see a complete working demo, so that I can evaluate the product's functionality.

#### Acceptance Criteria

1. THE Platform SHALL support a complete flow: sign up → create task → switch role → view tasks → place bid → switch role → accept worker → complete task → release escrow → leave review
2. THE Platform SHALL complete the demo flow within 2 minutes
3. THE Platform SHALL handle role switching without crashes or data loss
4. THE Platform SHALL display clear visual feedback for all user actions
5. THE Platform SHALL gracefully handle network errors with appropriate messages
6. THE Platform SHALL maintain stable performance throughout the demo flow

### Requirement 19: Task Categories and Equipment Management

**User Story:** As a user, I want to specify task categories and required equipment, so that workers know what skills and tools they need.

#### Acceptance Criteria

1. THE Platform SHALL provide predefined task categories (e.g., Cleaning, Repair, Delivery, Assembly, etc.)
2. WHEN creating a task, THE Platform SHALL allow users to select a category
3. THE Platform SHALL allow users to specify required equipment with options: "User provides" or "Worker provides"
4. WHEN viewing a task, THE Platform SHALL clearly display category and equipment requirements
5. THE Platform SHALL allow workers to filter tasks by category

### Requirement 20: Notification System (Mock)

**User Story:** As a user or worker, I want to receive notifications about important events, so that I stay informed about my tasks and bids.

#### Acceptance Criteria

1. WHEN a worker places a bid, THE Platform SHALL create a notification record for the task owner
2. WHEN a user accepts a bid, THE Platform SHALL create a notification record for the worker
3. WHEN a worker marks task completed, THE Platform SHALL create a notification record for the user
4. THE Platform SHALL display notifications in an in-app notification center
5. THE Platform SHALL indicate unread notifications with a badge or indicator
