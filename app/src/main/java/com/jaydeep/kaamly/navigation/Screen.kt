package com.jaydeep.kaamly.navigation

/**
 * Sealed class representing all navigation screens in the app
 */
sealed class Screen(val route: String) {
    
    /**
     * Authentication screens
     */
    sealed class Auth(route: String) : Screen(route) {
        data object Login : Auth("auth/login")
        data object Signup : Auth("auth/signup")
        data object RoleSelection : Auth("auth/role_selection")
    }
    
    /**
     * User role screens
     */
    sealed class User(route: String) : Screen(route) {
        data object Dashboard : User("user/dashboard")
        data object CreateTask : User("user/create_task")
        data object TaskDetail : User("user/task_detail/{taskId}") {
            fun createRoute(taskId: String) = "user/task_detail/$taskId"
        }
        data object BidList : User("user/bid_list/{taskId}") {
            fun createRoute(taskId: String) = "user/bid_list/$taskId"
        }
        data object WorkerProfile : User("user/worker_profile/{workerId}") {
            fun createRoute(workerId: String) = "user/worker_profile/$workerId"
        }
        data object Payment : User("user/payment/{taskId}") {
            fun createRoute(taskId: String) = "user/payment/$taskId"
        }
        data object Review : User("user/review/{taskId}/{workerId}") {
            fun createRoute(taskId: String, workerId: String) = "user/review/$taskId/$workerId"
        }
        data object TaskProgress : User("user/task_progress/{taskId}") {
            fun createRoute(taskId: String) = "user/task_progress/$taskId"
        }
    }
    
    /**
     * Worker role screens
     */
    sealed class Worker(route: String) : Screen(route) {
        data object Dashboard : Worker("worker/dashboard")
        data object TaskFeed : Worker("worker/task_feed")
        data object TaskDetail : Worker("worker/task_detail/{taskId}") {
            fun createRoute(taskId: String) = "worker/task_detail/$taskId"
        }
        data object PlaceBid : Worker("worker/place_bid/{taskId}") {
            fun createRoute(taskId: String) = "worker/place_bid/$taskId"
        }
        data object ActiveTasks : Worker("worker/active_tasks")
        data object Verification : Worker("worker/verification")
    }
    
    /**
     * Shared screens (accessible from both roles)
     */
    sealed class Shared(route: String) : Screen(route) {
        data object UserProfile : Shared("shared/user_profile/{userId}") {
            fun createRoute(userId: String) = "shared/user_profile/$userId"
        }
        data object WorkerProfile : Shared("shared/worker_profile/{workerId}") {
            fun createRoute(workerId: String) = "shared/worker_profile/$workerId"
        }
        data object Verification : Shared("shared/verification/{workerId}") {
            fun createRoute(workerId: String) = "shared/verification/$workerId"
        }
        data object Settings : Shared("shared/settings")
        data object Notifications : Shared("shared/notifications")
    }
}
