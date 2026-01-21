package com.jaydeep.kaamly.util

import com.google.firebase.firestore.FirebaseFirestore
import com.jaydeep.kaamly.data.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for seeding demo data into Firestore
 * Used for hackathon demo purposes
 */
@Singleton
class DemoDataSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_PROFILES_COLLECTION = "userProfiles"
        private const val WORKER_PROFILES_COLLECTION = "workerProfiles"
        private const val TASKS_COLLECTION = "tasks"
        private const val BIDS_COLLECTION = "bids"
        private const val REVIEWS_COLLECTION = "reviews"
        
        // Demo user IDs
        const val DEMO_USER_1_ID = "demo_user_1"
        const val DEMO_USER_2_ID = "demo_user_2"
        const val DEMO_WORKER_1_ID = "demo_worker_1"
        const val DEMO_WORKER_2_ID = "demo_worker_2"
        const val DEMO_WORKER_3_ID = "demo_worker_3"
    }
    
    /**
     * Seed all demo data
     */
    suspend fun seedAllDemoData(): Result<Unit> {
        return try {
            seedDemoUsers()
            seedDemoWorkers()
            seedDemoTasks()
            seedDemoBids()
            seedDemoReviews()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Seed demo users
     */
    private suspend fun seedDemoUsers() {
        val users = listOf(
            User(
                id = DEMO_USER_1_ID,
                email = "user1@demo.com",
                name = "Priya Sharma",
                role = UserRole.USER,
                createdAt = System.currentTimeMillis()
            ),
            User(
                id = DEMO_USER_2_ID,
                email = "user2@demo.com",
                name = "Rahul Verma",
                role = UserRole.USER,
                createdAt = System.currentTimeMillis()
            )
        )
        
        val userProfiles = listOf(
            UserProfile(
                userId = DEMO_USER_1_ID,
                name = "Priya Sharma",
                city = "Mumbai",
                profilePhotoUrl = null,
                aadhaarVerified = true,
                phoneNumber = "+91 98765 43210",
                createdAt = System.currentTimeMillis()
            ),
            UserProfile(
                userId = DEMO_USER_2_ID,
                name = "Rahul Verma",
                city = "Bangalore",
                profilePhotoUrl = null,
                aadhaarVerified = true,
                phoneNumber = "+91 98765 43211",
                createdAt = System.currentTimeMillis()
            )
        )
        
        users.forEach { user ->
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()
        }
        
        userProfiles.forEach { profile ->
            firestore.collection(USER_PROFILES_COLLECTION)
                .document(profile.userId)
                .set(profile)
                .await()
        }
    }
    
    /**
     * Seed demo workers
     */
    private suspend fun seedDemoWorkers() {
        val workers = listOf(
            User(
                id = DEMO_WORKER_1_ID,
                email = "worker1@demo.com",
                name = "Amit Kumar",
                role = UserRole.WORKER,
                createdAt = System.currentTimeMillis()
            ),
            User(
                id = DEMO_WORKER_2_ID,
                email = "worker2@demo.com",
                name = "Sunita Devi",
                role = UserRole.WORKER,
                createdAt = System.currentTimeMillis()
            ),
            User(
                id = DEMO_WORKER_3_ID,
                email = "worker3@demo.com",
                name = "Rajesh Singh",
                role = UserRole.WORKER,
                createdAt = System.currentTimeMillis()
            )
        )
        
        val workerProfiles = listOf(
            WorkerProfile(
                workerId = DEMO_WORKER_1_ID,
                name = "Amit Kumar",
                city = "Mumbai",
                profilePhotoUrl = null,
                skills = listOf("Plumbing", "Electrical", "Repair"),
                bio = "Experienced handyman with 5+ years in home repairs. Quick, reliable, and professional.",
                overallRating = 4.8,
                totalReviews = 47,
                taskTypeRatings = mapOf(
                    TaskCategory.REPAIR to 4.9,
                    TaskCategory.PLUMBING to 4.8,
                    TaskCategory.ELECTRICAL to 4.7
                ),
                aadhaarVerified = true,
                phoneNumber = "+91 98765 11111",
                createdAt = System.currentTimeMillis()
            ),
            WorkerProfile(
                workerId = DEMO_WORKER_2_ID,
                name = "Sunita Devi",
                city = "Mumbai",
                profilePhotoUrl = null,
                skills = listOf("Cleaning", "Cooking", "Gardening"),
                bio = "Professional cleaner and cook. Specializing in deep cleaning and home organization.",
                overallRating = 4.9,
                totalReviews = 63,
                taskTypeRatings = mapOf(
                    TaskCategory.CLEANING to 5.0,
                    TaskCategory.GARDENING to 4.8
                ),
                aadhaarVerified = true,
                phoneNumber = "+91 98765 22222",
                createdAt = System.currentTimeMillis()
            ),
            WorkerProfile(
                workerId = DEMO_WORKER_3_ID,
                name = "Rajesh Singh",
                city = "Bangalore",
                profilePhotoUrl = null,
                skills = listOf("Delivery", "Moving", "Assembly"),
                bio = "Fast and reliable delivery service. Available for instant jobs and same-day tasks.",
                overallRating = 4.6,
                totalReviews = 32,
                taskTypeRatings = mapOf(
                    TaskCategory.DELIVERY to 4.7,
                    TaskCategory.MOVING to 4.5,
                    TaskCategory.ASSEMBLY to 4.6
                ),
                aadhaarVerified = true,
                phoneNumber = "+91 98765 33333",
                createdAt = System.currentTimeMillis()
            )
        )
        
        workers.forEach { worker ->
            firestore.collection(USERS_COLLECTION)
                .document(worker.id)
                .set(worker)
                .await()
        }
        
        workerProfiles.forEach { profile ->
            firestore.collection(WORKER_PROFILES_COLLECTION)
                .document(profile.workerId)
                .set(profile)
                .await()
        }
    }
    
    /**
     * Seed demo tasks
     */
    private suspend fun seedDemoTasks() {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        val tasks = listOf(
            Task(
                id = "demo_task_1",
                userId = DEMO_USER_1_ID,
                title = "Fix Leaking Kitchen Tap",
                description = "My kitchen tap has been leaking for a few days. Need someone to fix it urgently. The tap is a standard mixer tap.",
                category = TaskCategory.PLUMBING,
                requiredEquipment = listOf(
                    Equipment("Wrench", EquipmentProvider.WORKER),
                    Equipment("Plumber's tape", EquipmentProvider.WORKER)
                ),
                location = Location(
                    latitude = 19.0760,
                    longitude = 72.8777,
                    city = "Mumbai",
                    address = "Andheri West, Mumbai"
                ),
                scheduledDate = currentTime + oneDayMillis,
                scheduledTime = "10:00 AM",
                budget = 500.0,
                isInstantJob = false,
                state = TaskState.OPEN,
                assignedWorkerId = null,
                completionPhotoUrl = null,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            Task(
                id = "demo_task_2",
                userId = DEMO_USER_1_ID,
                title = "Deep Clean 2BHK Apartment",
                description = "Need thorough cleaning of my 2BHK apartment including kitchen, bathrooms, and all rooms. Dusting, mopping, and bathroom cleaning required.",
                category = TaskCategory.CLEANING,
                requiredEquipment = listOf(
                    Equipment("Cleaning supplies", EquipmentProvider.WORKER),
                    Equipment("Vacuum cleaner", EquipmentProvider.USER)
                ),
                location = Location(
                    latitude = 19.0760,
                    longitude = 72.8777,
                    city = "Mumbai",
                    address = "Bandra East, Mumbai"
                ),
                scheduledDate = currentTime + (2 * oneDayMillis),
                scheduledTime = "9:00 AM",
                budget = 1500.0,
                isInstantJob = false,
                state = TaskState.OPEN,
                assignedWorkerId = null,
                completionPhotoUrl = null,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            Task(
                id = "demo_task_3",
                userId = DEMO_USER_2_ID,
                title = "Deliver Package Across City",
                description = "Need urgent delivery of important documents from Koramangala to Whitefield. Package is small and lightweight.",
                category = TaskCategory.DELIVERY,
                requiredEquipment = listOf(
                    Equipment("Two-wheeler", EquipmentProvider.WORKER)
                ),
                location = Location(
                    latitude = 12.9352,
                    longitude = 77.6245,
                    city = "Bangalore",
                    address = "Koramangala, Bangalore"
                ),
                scheduledDate = currentTime,
                scheduledTime = "Now",
                budget = 300.0,
                isInstantJob = true,
                state = TaskState.OPEN,
                assignedWorkerId = null,
                completionPhotoUrl = null,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            Task(
                id = "demo_task_4",
                userId = DEMO_USER_2_ID,
                title = "Install Ceiling Fan",
                description = "Purchased a new ceiling fan and need help with installation. All mounting hardware is included.",
                category = TaskCategory.ELECTRICAL,
                requiredEquipment = listOf(
                    Equipment("Ladder", EquipmentProvider.USER),
                    Equipment("Electrical tools", EquipmentProvider.WORKER)
                ),
                location = Location(
                    latitude = 12.9716,
                    longitude = 77.5946,
                    city = "Bangalore",
                    address = "Indiranagar, Bangalore"
                ),
                scheduledDate = currentTime + oneDayMillis,
                scheduledTime = "3:00 PM",
                budget = 800.0,
                isInstantJob = false,
                state = TaskState.OPEN,
                assignedWorkerId = null,
                completionPhotoUrl = null,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            Task(
                id = "demo_task_5",
                userId = DEMO_USER_1_ID,
                title = "Assemble IKEA Furniture",
                description = "Need help assembling a wardrobe and study table from IKEA. All parts and instructions are available.",
                category = TaskCategory.ASSEMBLY,
                requiredEquipment = listOf(
                    Equipment("Allen keys", EquipmentProvider.WORKER),
                    Equipment("Screwdriver set", EquipmentProvider.WORKER)
                ),
                location = Location(
                    latitude = 19.0760,
                    longitude = 72.8777,
                    city = "Mumbai",
                    address = "Powai, Mumbai"
                ),
                scheduledDate = currentTime + (3 * oneDayMillis),
                scheduledTime = "11:00 AM",
                budget = 1000.0,
                isInstantJob = false,
                state = TaskState.OPEN,
                assignedWorkerId = null,
                completionPhotoUrl = null,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        )
        
        tasks.forEach { task ->
            firestore.collection(TASKS_COLLECTION)
                .document(task.id)
                .set(task)
                .await()
        }
    }
    
    /**
     * Seed demo bids
     */
    private suspend fun seedDemoBids() {
        val currentTime = System.currentTimeMillis()
        
        val bids = listOf(
            Bid(
                id = "demo_bid_1",
                taskId = "demo_task_1",
                workerId = DEMO_WORKER_1_ID,
                amount = 450.0,
                message = "I can fix this today! I have 5+ years experience with plumbing repairs. Will bring all necessary tools.",
                status = BidStatus.PENDING,
                createdAt = currentTime
            ),
            Bid(
                id = "demo_bid_2",
                taskId = "demo_task_2",
                workerId = DEMO_WORKER_2_ID,
                amount = 1400.0,
                message = "I specialize in deep cleaning. Will make your apartment spotless! Can start early morning.",
                status = BidStatus.PENDING,
                createdAt = currentTime
            ),
            Bid(
                id = "demo_bid_3",
                taskId = "demo_task_2",
                workerId = DEMO_WORKER_1_ID,
                amount = 1600.0,
                message = "Professional cleaning service available. Will bring all cleaning supplies.",
                status = BidStatus.PENDING,
                createdAt = currentTime
            ),
            Bid(
                id = "demo_bid_4",
                taskId = "demo_task_3",
                workerId = DEMO_WORKER_3_ID,
                amount = 250.0,
                message = "I can deliver this right away! I'm currently in Koramangala area. Fast and reliable.",
                status = BidStatus.PENDING,
                createdAt = currentTime
            )
        )
        
        bids.forEach { bid ->
            firestore.collection(BIDS_COLLECTION)
                .document(bid.id)
                .set(bid)
                .await()
        }
    }
    
    /**
     * Seed demo reviews
     */
    private suspend fun seedDemoReviews() {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        val reviews = listOf(
            Review(
                id = "demo_review_1",
                taskId = "completed_task_1",
                workerId = DEMO_WORKER_1_ID,
                userId = DEMO_USER_1_ID,
                starRating = 5,
                textReview = "Excellent work! Fixed the tap quickly and professionally. Very satisfied with the service.",
                taskSpecificRating = mapOf(
                    "punctuality" to 5,
                    "quality" to 5,
                    "communication" to 5
                ),
                timestamp = currentTime - (5 * oneDayMillis)
            ),
            Review(
                id = "demo_review_2",
                taskId = "completed_task_2",
                workerId = DEMO_WORKER_2_ID,
                userId = DEMO_USER_2_ID,
                starRating = 5,
                textReview = "Amazing cleaning service! The apartment looks brand new. Highly recommend Sunita!",
                taskSpecificRating = mapOf(
                    "punctuality" to 5,
                    "quality" to 5,
                    "communication" to 5
                ),
                timestamp = currentTime - (3 * oneDayMillis)
            ),
            Review(
                id = "demo_review_3",
                taskId = "completed_task_3",
                workerId = DEMO_WORKER_1_ID,
                userId = DEMO_USER_1_ID,
                starRating = 4,
                textReview = "Good work on the electrical installation. Arrived on time and completed the job efficiently.",
                taskSpecificRating = mapOf(
                    "punctuality" to 5,
                    "quality" to 4,
                    "communication" to 4
                ),
                timestamp = currentTime - (7 * oneDayMillis)
            ),
            Review(
                id = "demo_review_4",
                taskId = "completed_task_4",
                workerId = DEMO_WORKER_3_ID,
                userId = DEMO_USER_2_ID,
                starRating = 5,
                textReview = "Super fast delivery! Package reached safely and on time. Will definitely hire again.",
                taskSpecificRating = mapOf(
                    "punctuality" to 5,
                    "quality" to 5,
                    "communication" to 5
                ),
                timestamp = currentTime - (2 * oneDayMillis)
            )
        )
        
        reviews.forEach { review ->
            firestore.collection(REVIEWS_COLLECTION)
                .document(review.id)
                .set(review)
                .await()
        }
    }
    
    /**
     * Clear all demo data
     */
    suspend fun clearDemoData(): Result<Unit> {
        return try {
            // Delete demo users
            listOf(DEMO_USER_1_ID, DEMO_USER_2_ID, DEMO_WORKER_1_ID, DEMO_WORKER_2_ID, DEMO_WORKER_3_ID).forEach { id ->
                firestore.collection(USERS_COLLECTION).document(id).delete().await()
                firestore.collection(USER_PROFILES_COLLECTION).document(id).delete().await()
                firestore.collection(WORKER_PROFILES_COLLECTION).document(id).delete().await()
            }
            
            // Delete demo tasks
            val tasks = firestore.collection(TASKS_COLLECTION)
                .whereIn("id", listOf("demo_task_1", "demo_task_2", "demo_task_3", "demo_task_4", "demo_task_5"))
                .get()
                .await()
            tasks.documents.forEach { it.reference.delete().await() }
            
            // Delete demo bids
            val bids = firestore.collection(BIDS_COLLECTION)
                .whereIn("id", listOf("demo_bid_1", "demo_bid_2", "demo_bid_3", "demo_bid_4"))
                .get()
                .await()
            bids.documents.forEach { it.reference.delete().await() }
            
            // Delete demo reviews
            val reviews = firestore.collection(REVIEWS_COLLECTION)
                .whereIn("id", listOf("demo_review_1", "demo_review_2", "demo_review_3", "demo_review_4"))
                .get()
                .await()
            reviews.documents.forEach { it.reference.delete().await() }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
