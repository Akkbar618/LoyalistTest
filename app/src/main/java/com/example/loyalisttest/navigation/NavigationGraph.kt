package com.example.loyalisttest.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.loyalisttest.auth.*
import com.example.loyalisttest.main.*
import com.example.loyalisttest.navigation.NavigationActions.navigateToForgotPassword
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignIn
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignUp
import com.example.loyalisttest.ui.theme.Transitions
import androidx.navigation.NavType

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Авторизация
        composable(
            route = NavigationRoutes.Welcome.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            WelcomeScreen(
                onNavigateToSignIn = { navController.navigateToSignIn() },
                onNavigateToSignUp = { navController.navigateToSignUp() }
            )
        }

        composable(
            route = NavigationRoutes.SignIn.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            SignInScreen(
                onBackClick = { navController.navigateUp() },
                onForgotPasswordClick = { navController.navigateToForgotPassword() },
                onSignInClick = { _, _ ->
                    navController.navigate(NavigationRoutes.Main.route) {
                        popUpTo(NavigationRoutes.Welcome.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigateToSignUp(NavigationRoutes.SignIn.route) }
            )
        }

        composable(
            route = NavigationRoutes.SignUp.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            SignUpScreen(
                onBackClick = { navController.navigateUp() },
                onSignUpClick = { _, _, _ ->
                    navController.navigate(NavigationRoutes.Main.route) {
                        popUpTo(NavigationRoutes.Welcome.route) { inclusive = true }
                    }
                },
                onSignInClick = { navController.navigateToSignIn(NavigationRoutes.SignUp.route) }
            )
        }

        composable(
            route = NavigationRoutes.ForgotPassword.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            ForgotPasswordScreen(
                onBackClick = { navController.navigateUp() },
                onSignInClick = { navController.navigateToSignIn(NavigationRoutes.Welcome.route) }
            )
        }

        // Основной экран
        composable(
            route = NavigationRoutes.Main.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            MainScreen(navController = navController)
        }
    }
}