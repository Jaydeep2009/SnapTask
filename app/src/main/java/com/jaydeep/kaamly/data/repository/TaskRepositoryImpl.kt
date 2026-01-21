package com.jaydeep.kaamly.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.jaydeep.kaamly.data.model.Location
import com.jaydeep.kaamly.data.model.Task
import com.jaydeep.kaamly.data.model.TaskState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Implementation of TaskRepository using Firebase Firestore
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : TaskRepository {

    companion object {
        private const val TASKS_COLLECTION = "tasks"
        private const val TASK_PHOTOS_PATH = "task_photos"
        private const val EARTH_RADIUS_KM = 6371.0
    }

    override suspend fun createTask(task: Task): BaseRepository.Result<String> {
        return try {
            val taskRef = firestore.collection(TASKS_COLLECTION).document()
            val taskWithId = task.copy(
                id = taskRef.id,
                state = TaskState.OPEN,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            taskRef.set(taskWithId).await()
            BaseRepository.Result.Success(taskRef.id)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun getTask(taskId: String): BaseRepository.Result<Task> {
        return try {
            val snapshot = firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .get()
                .await()
            
            val task = snapshot.toObject(Task::class.java)
            if (task != null) {
                BaseRepository.Result.Success(task)
            } else {
                BaseRepository.Result.Error(Exception("Task not found"))
            }
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override fun getUserTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TaskRepository", "Error loading user tasks", error)
                    // Don't close the flow, just send empty list
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

    override fun getNearbyTasks(location: Location, radius: Double): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo("state", TaskState.OPEN.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TaskRepository", "Error loading nearby tasks", error)
                    // Don't close the flow, just send empty list
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)
                }?.filter { task ->
                    // Filter by distance
                    val distance = calculateDistance(
                        location.latitude,
                        location.longitude,
                        task.location.latitude,
                        task.location.longitude
                    )
                    distance <= radius
                } ?: emptyList()
                
                trySend(tasks)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getTasksByCity(city: String): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo("state", TaskState.OPEN.name)
            .whereEqualTo("location.city", city)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TaskRepository", "Error loading tasks by city", error)
                    // Don't close the flow, just send empty list
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

    override fun getAllOpenTasks(): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo("state", TaskState.OPEN.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("TaskRepository", "Error loading all open tasks", error)
                    // Don't close the flow, just send empty list
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

    override suspend fun updateTaskState(taskId: String, state: TaskState): BaseRepository.Result<Unit> {
        return try {
            // Validate state transition
            val currentTask = getTask(taskId)
            if (currentTask is BaseRepository.Result.Success) {
                val task = currentTask.data
                if (!isValidStateTransition(task.state, state)) {
                    return BaseRepository.Result.Error(
                        IllegalStateException("Invalid state transition from ${task.state} to $state")
                    )
                }
            }
            
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "state" to state.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun assignWorker(taskId: String, workerId: String): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "assignedWorkerId" to workerId,
                        "state" to TaskState.IN_PROGRESS.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun uploadTaskPhoto(taskId: String, imageUri: Uri): BaseRepository.Result<String> {
        return try {
            val fileName = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(TASK_PHOTOS_PATH)
                .child(taskId)
                .child(fileName)
            
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Update task with photo URL
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "completionPhotoUrl" to downloadUrl,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            BaseRepository.Result.Success(downloadUrl)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun markArrived(taskId: String): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "workerArrived" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun markCompleted(taskId: String): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "completionRequested" to true,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun approveCompletion(taskId: String): BaseRepository.Result<Unit> {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .update(
                    mapOf(
                        "state" to TaskState.COMPLETED.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            BaseRepository.Result.Success(Unit)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

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

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @return Distance in kilometers
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_KM * c
    }

    /**
     * Validate if a state transition is allowed
     */
    private fun isValidStateTransition(currentState: TaskState, newState: TaskState): Boolean {
        return when (currentState) {
            TaskState.OPEN -> newState == TaskState.IN_PROGRESS || newState == TaskState.CANCELLED
            TaskState.IN_PROGRESS -> newState == TaskState.COMPLETED || newState == TaskState.CANCELLED
            TaskState.COMPLETED -> false // Cannot transition from completed
            TaskState.CANCELLED -> false // Cannot transition from cancelled
        }
    }
}
