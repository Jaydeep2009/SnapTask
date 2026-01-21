# Task Persistence Fix

## Issue
Tasks were being created successfully but disappeared after a few microseconds. The task would flash briefly on the dashboard then vanish, and the create task screen would reappear.

## Root Cause
The same Firestore index issue we encountered with notifications. The `getUserTasks()` query was using:
```kotlin
.whereEqualTo("userId", userId)
.orderBy("createdAt", Query.Direction.DESCENDING)
```

This combination requires a composite Firestore index. When the query failed due to the missing index, the Flow would close with an error, causing:
1. Task to appear briefly (from optimistic UI update)
2. Query to fail silently
3. Empty list to be shown
4. User to be redirected back

## Solution
Applied the same fix as the notification issue - removed `orderBy` from Firestore queries and sort results in memory instead.

## Changes Made

### File: `app/src/main/java/com/jaydeep/kaamly/data/repository/TaskRepositoryImpl.kt`

**1. Fixed `getUserTasks()` query:**
```kotlin
// BEFORE (requires index):
.whereEqualTo("userId", userId)
.orderBy("createdAt", Query.Direction.DESCENDING)

// AFTER (no index needed):
.whereEqualTo("userId", userId)
// Sort in memory:
.sortedByDescending { it.createdAt }
```

**2. Fixed `getTasksByCity()` query:**
```kotlin
// BEFORE (requires index):
.whereEqualTo("state", TaskState.OPEN.name)
.whereEqualTo("location.city", city)
.orderBy("createdAt", Query.Direction.DESCENDING)

// AFTER (no index needed):
.whereEqualTo("state", TaskState.OPEN.name)
.whereEqualTo("location.city", city)
// Sort in memory:
.sortedByDescending { it.createdAt }
```

**3. Improved error handling:**
- Changed from `close(error)` to `trySend(emptyList())`
- Added logging: `android.util.Log.e("TaskRepository", "Error loading...", error)`
- Flow stays open even on errors, preventing crashes
- Empty list shown instead of closing the connection

## Why This Happens

Firestore requires composite indexes when you:
1. Use multiple `whereEqualTo` clauses, OR
2. Combine `whereEqualTo` with `orderBy` on a different field

Our queries were doing both, requiring indexes like:
- `userId` + `createdAt` (for getUserTasks)
- `state` + `location.city` + `createdAt` (for getTasksByCity)

## Alternative Solution (Not Recommended for MVP)

You could create the Firestore indexes by:
1. Clicking the index creation link in the error logs
2. Waiting 5-10 minutes for indexes to build
3. Testing again

However, for an MVP/hackathon demo, sorting in memory is:
- ✅ Faster to implement
- ✅ No waiting for index creation
- ✅ Works immediately
- ✅ Sufficient for small datasets
- ✅ No additional Firebase configuration

## Expected Behavior Now

1. **Create Task:**
   - User fills form and clicks "Create Task"
   - Task is saved to Firestore
   - User navigates back to dashboard

2. **View Tasks:**
   - Dashboard loads user's tasks via `getUserTasks()`
   - Query succeeds (no index required)
   - Tasks appear in list, sorted by creation date (newest first)
   - Tasks persist and don't disappear

3. **Worker Discovery:**
   - Workers can see tasks via `getTasksByCity()`
   - Query succeeds (no index required)
   - Tasks appear sorted by creation date

## Testing the Fix

1. **Login as User**
2. **Create a task:**
   - Click "Create Task"
   - Fill in details (or use "Use Demo Data")
   - Submit
3. **Verify task persists:**
   - Should navigate back to dashboard
   - Task should appear in the list
   - Task should NOT disappear
   - Task should stay visible
4. **Create another task:**
   - Repeat steps 2-3
   - Both tasks should be visible
   - Sorted by newest first
5. **Switch to Worker role:**
   - Tasks should appear in worker dashboard (if same city)

## Status
✅ Removed orderBy from getUserTasks query
✅ Removed orderBy from getTasksByCity query
✅ Added in-memory sorting
✅ Improved error handling with logging
✅ No compilation errors
⏳ Ready for testing
