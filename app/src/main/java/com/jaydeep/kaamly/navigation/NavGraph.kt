package com.jaydeep.kaamly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jaydeep.kaamly.data.model.UserRole
import com.jaydeep.kaamly.ui.screens.auth.LoginScreen
import com.jaydeep.kaamly.ui.screens.auth.RoleSelectionScreen
import com.jaydeep.kaamly.ui.screens.auth.SignupScreen
import com.jaydeep.kaamly.ui.screens.notification.NotificationScreen
import com.jaydeep.kaamly.ui.screens.payment.PaymentScreen
import com.jaydeep.kaamly.ui.screens.task.CreateTaskScreen
import com.jaydeep.kaamly.ui.screens.user.BidListScreen
import com.jaydeep.kaamly.ui.screens.user.UserDashboardScreen
import com.jaydeep.kaamly.ui.screens.worker.PlaceBidScreen
import com.jaydeep.kaamly.ui.screens.worker.TaskDetailScreen
import com.jaydeep.kaamly.ui.screens.worker.WorkerDashboardScreen
import com.jaydeep.kaamly.ui.viewmodel.AuthViewModel

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Auth.Login.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth navigation graph
        composable(Screen.Auth.Login.route) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Auth.Signup.route) {
                        popUpTo(Screen.Auth.Login.route) { inclusive = false }
                    }
                },
                onLoginSuccess = { user ->
                    // Navigate based on user role
                    val destination = when (user.role) {
                        UserRole.USER -> Screen.User.Dashboard.route
                        UserRole.WORKER -> Screen.Worker.Dashboard.route
                        UserRole.BOTH -> Screen.User.Dashboard.route // Default to user dashboard
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Auth.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Auth.Signup.route) {
            SignupScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(Screen.Auth.Signup.route) { inclusive = true }
                    }
                },
                onSignupSuccess = {
                    navController.navigate(Screen.Auth.RoleSelection.route) {
                        popUpTo(Screen.Auth.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Auth.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    // Navigate based on selected role
                    val destination = when (role) {
                        UserRole.USER -> Screen.User.Dashboard.route
                        UserRole.WORKER -> Screen.Worker.Dashboard.route
                        UserRole.BOTH -> Screen.User.Dashboard.route // Default to user dashboard
                    }
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true } // Clear entire back stack
                    }
                }
            )
        }
        
        // User navigation graph
        composable(Screen.User.Dashboard.route) {
            UserDashboardScreen(
                onNavigateToCreateTask = {
                    navController.navigate(Screen.User.CreateTask.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.User.TaskDetail.createRoute(taskId))
                },
                onNavigateToBidList = { taskId ->
                    navController.navigate(Screen.User.BidList.createRoute(taskId))
                },
                onNavigateToWorkerDashboard = {
                    navController.navigate(Screen.Worker.Dashboard.route) {
                        popUpTo(Screen.User.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Shared.Notifications.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.User.CreateTask.route) {
            CreateTaskScreen(
                onTaskCreated = {
                    // Navigate back to dashboard after task is created
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Worker navigation graph
        composable(Screen.Worker.Dashboard.route) {
            WorkerDashboardScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.Worker.TaskDetail.createRoute(taskId))
                },
                onNavigateToProfile = {
                    // Will be implemented later
                },
                onNavigateToUserDashboard = {
                    navController.navigate(Screen.User.Dashboard.route) {
                        popUpTo(Screen.Worker.Dashboard.route) { inclusive = false }
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Shared.Notifications.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Worker.TaskDetail.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlaceBid = { taskId ->
                    navController.navigate(Screen.Worker.PlaceBid.createRoute(taskId))
                }
            )
        }
        
        composable(Screen.Worker.PlaceBid.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            PlaceBidScreen(
                taskId = taskId,
                onBidPlaced = {
                    // Navigate back to worker dashboard
                    navController.popBackStack(Screen.Worker.Dashboard.route, inclusive = false)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.User.BidList.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            BidListScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewWorkerProfile = { workerId ->
                    // Will be implemented later
                },
                onNavigateToPayment = { bidId, amount ->
                    // Navigate to payment screen with bidId and amount
                    navController.navigate("user/payment/$taskId/$bidId/$amount")
                },
                onBidAccepted = {
                    // Navigate back to dashboard
                    navController.popBackStack(Screen.User.Dashboard.route, inclusive = false)
                }
            )
        }
        
        composable("user/payment/{taskId}/{bidId}/{amount}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val bidId = backStackEntry.arguments?.getString("bidId") ?: return@composable
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: return@composable
            
            PaymentScreen(
                taskId = taskId,
                bidId = bidId,
                bidAmount = amount,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPaymentConfirmed = {
                    // Navigate back to dashboard after payment
                    navController.popBackStack(Screen.User.Dashboard.route, inclusive = false)
                }
            )
        }
        
        // Shared navigation graph
        composable(Screen.Shared.Notifications.route) {
            NotificationScreen(
                onNavigateToTask = { taskId ->
                    // Navigate to task detail based on current role
                    // For now, just go back
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
