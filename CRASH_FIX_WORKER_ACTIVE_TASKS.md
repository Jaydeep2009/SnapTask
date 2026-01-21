# Crash Fix: Worker Active Tasks

## Issue
App was crashing on startup due to Firestore composite index requirement.

## Root Cause
The `getWorkerActiveTasks()` method in `TaskRepositoryImpl` was using:
```kotlin
.whereEqualTo("assignedWorkerId", workerId)
.whereEqualTo("state", TaskState.IN_PROGRESS.name)
.orderBy("updatedAt", Query.Direction.DESCENDING)
```

This combination of `whereEqualTo` + `orderBy` requires a composite index in Firestore, which causes the app to crash if the index doesn't exist.

## Solution
Applied the same fix pattern used throughout the app:
1. Removed `.orderBy()` from the Firestore query
2. Sort results in memory using `.sortedByDescending { it.updatedAt }`
3. Changed error handling to not close the flow, just send empty list

## Files Modified

### TaskRepositoryImpl.kt
**Before:**
```kotlin
override fun getWorkerActiveTasks(workerId: String): Flow<List<Task>> = callbackFlow {
    val listener = firestore.collection(TASKS_COLLECTION)
        .whereEqualTo("assignedWorkerId", workerId)
        .whereEqualTo("state", TaskState.IN_PROGRESS.name)
        .orderBy("updatedAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            } ?: emptyList()
            
            trySend(tasks)
        }
    
    awaitClose { listener.remove() }
}
```

**After:**
```kotlin
override fun getWorkerActiveTasks(workerId: String): Flow<List<Task>> = callbackFlow {
    val listener = firestore.collection(TASKS_COLLECTION)
        .whereEqualTo("assignedWorkerId", workerId)
        .whereEqualTo("state", TaskState.IN_PROGRESS.name)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("TaskRepository", "Error loading worker active tasks", error)
                // Don't close the flow, just send empty list
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }?.sortedByDescending { it.updatedAt } ?: emptyList()
            
            trySend(tasks)
        }
    
    awaitClose { listener.remove() }
}
```

### WorkerDashboardScreen.kt
Added missing import:
```kotlin
import androidx.compose.ui.text.style.TextAlign
```

## Pattern Applied
This is the same pattern we've used throughout the app to avoid Firestore composite index requirements:
- Remove `orderBy` from queries
- Sort in memory instead
- Graceful error handling

## Testing
- App should now start without crashing
- Worker dashboard should load properly
- Approved jobs tab should work correctly
- Wallet balance should display

## Related Fixes
This is consistent with previous fixes in:
- `NotificationRepositoryImpl.kt`
- `TaskRepositoryImpl.getUserTasks()`
- `TaskRepositoryImpl.getTasksByCity()`
- `BidRepositoryImpl.getBidsForTask()`
- `BidRepositoryImpl.getWorkerBids()`
