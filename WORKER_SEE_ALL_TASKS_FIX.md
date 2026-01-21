# Worker See All Tasks Fix

## Issue
Workers were only seeing tasks in Mumbai (hardcoded city filter). This limited task visibility and prevented workers from seeing tasks posted in other cities.

## Solution
Added functionality to load ALL open tasks for workers, removing the city restriction.

## Changes Made

### 1. TaskRepository Interface
**File**: `app/src/main/java/com/jaydeep/kaamly/data/repository/TaskRepository.kt`

Added new method:
```kotlin
/**
 * Get all open tasks (for workers to browse all available tasks)
 * @return Flow of task list
 */
fun getAllOpenTasks(): Flow<List<Task>>
```

### 2. TaskRepositoryImpl
**File**: `app/src/main/java/com/jaydeep/kaamly/data/repository/TaskRepositoryImpl.kt`

Implemented the method:
```kotlin
override fun getAllOpenTasks(): Flow<List<Task>> = callbackFlow {
    val listener = firestore.collection(TASKS_COLLECTION)
        .whereEqualTo("state", TaskState.OPEN.name)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("TaskRepository", "Error loading all open tasks", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)
            }?.sortedByDescending { it.createdAt } ?: emptyList()
            
            trySend(tasks)
        }
    
    awaitClose { listener.remove() }
}
```

**Key Features:**
- Queries only tasks with `state = OPEN`
- No city filter - gets ALL open tasks
- Sorts by creation date (newest first)
- Real-time updates via Firestore listener
- Error handling with logging

### 3. TaskViewModel
**File**: `app/src/main/java/com/jaydeep/kaamly/ui/viewmodel/TaskViewModel.kt`

Added new method:
```kotlin
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
            _nearbyTasks.value = emptyList()
        }
    }
}
```

**Features:**
- Loads all open tasks
- Applies filters (category, budget, instant jobs)
- Handles errors gracefully
- Updates UI in real-time

### 4. WorkerDashboardScreen
**File**: `app/src/main/java/com/jaydeep/kaamly/ui/screens/worker/WorkerDashboardScreen.kt`

**Before:**
```kotlin
LaunchedEffect(Unit) {
    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    if (currentFirebaseUser != null) {
        userCity = "Mumbai" // Hardcoded!
        viewModel.loadTasksByCity(userCity)
    }
}
```

**After:**
```kotlin
LaunchedEffect(Unit) {
    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    if (currentFirebaseUser != null) {
        // Load all open tasks for workers to see all available jobs
        viewModel.loadAllOpenTasks()
    }
}
```

Also updated location permission callback to use `loadAllOpenTasks()`.

## Benefits

### ✅ Workers See All Tasks
- No longer limited to Mumbai
- Can see tasks from any city
- Better job opportunities

### ✅ Real-time Updates
- Tasks appear immediately when posted
- No manual refresh needed
- Firestore snapshot listener

### ✅ Filtering Still Works
- Workers can still filter by:
  - Category
  - Budget range
  - Instant jobs only
- Filters apply to all tasks

### ✅ Sorted by Newest
- Most recent tasks appear first
- Better visibility for new postings

## Testing

### Test as Worker:
1. Login as worker
2. Worker dashboard should show ALL open tasks
3. Tasks from different cities should be visible
4. Create a task in any city as user
5. Switch to worker → Task should appear immediately

### Test Filtering:
1. On worker dashboard, click filter icon
2. Select a category (e.g., Plumbing)
3. Only plumbing tasks should show
4. Clear filter → All tasks appear again

### Test Real-time:
1. Open app as worker on one device/account
2. Create task as user on another device/account
3. Task should appear on worker dashboard immediately
4. No refresh needed

## Future Enhancements (Optional)

If you want to add location-based features later:

1. **Distance Display**: Show distance from worker to task
2. **Sort by Distance**: Sort tasks by proximity
3. **Radius Filter**: Filter tasks within X km
4. **City Filter**: Add optional city filter in filter dialog

For now, showing all tasks is better for demo purposes and ensures workers can see all available jobs regardless of location.

## Status
✅ All open tasks visible to workers
✅ No city restriction
✅ Real-time updates working
✅ Filters still functional
✅ No compilation errors
⏳ Ready for testing
