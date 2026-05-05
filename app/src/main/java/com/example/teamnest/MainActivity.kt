package com.example.teamnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.teamnest.ui.theme.TeamnestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme
            
            TeamnestTheme(darkTheme = isDarkTheme) {
                // Handle all app-wide permissions (Camera, Notifications, Media)
                AppPermissionHandler()
                TeamNestApp(themeViewModel = themeViewModel)
            }
        }
    }
}

@Composable
fun TeamNestApp(
    authViewModel: AuthViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser
    val context = LocalContext.current

    // Start global notification listening when user is logged in
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            notificationViewModel.startGlobalListening(context)
        } else {
            notificationViewModel.stopAllListeners()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "home" else "welcome",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) +
                    fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) +
                    fadeOut(animationSpec = tween(500))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) +
                    fadeIn(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) +
                    fadeOut(animationSpec = tween(500))
        }
    ) {
        composable("welcome") { 
            WelcomeOnboarding(onGetStarted = { navController.navigate("login") }) 
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") { popUpTo("register") { inclusive = true } }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("home") {
            MainScreenWithBottomNav(
                navController = navController, 
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("welcome") { popUpTo("home") { inclusive = true } }
                },
                themeViewModel = themeViewModel
            )
        }
        composable(
            "groupDetail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(groupId, navController)
        }
        composable(
            "taskDetail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(taskId, navController)
        }
    }
}
