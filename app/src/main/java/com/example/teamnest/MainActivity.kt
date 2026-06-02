package com.example.teamnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            TeamnestTheme {
                // Handle all app-wide permissions (Camera, Notifications, Media)
                AppPermissionHandler()
                TeamNestApp()
            }
        }
    }
}

@Composable
fun TeamNestApp(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "home" else "welcome"
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
            MainScreenWithBottomNav(navController, onLogout = {
                authViewModel.logout()
                navController.navigate("welcome") { popUpTo("home") { inclusive = true } }
            })
        }
        composable(
            "groupDetail/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(groupId, navController)
        }
    }
}
