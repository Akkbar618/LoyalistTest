package com.example.loyalisttest.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.loyalisttest.auth.*
import com.example.loyalisttest.main.*
import com.example.loyalisttest.navigation.NavigationActions.navigateToForgotPassword
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignIn
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignUp
import com.example.loyalisttest.ui.theme.Transitions

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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

        composable(
            route = NavigationRoutes.Main.route,
            enterTransition = { Transitions.authEnterTransition() },
            exitTransition = { Transitions.authExitTransition() }
        ) {
            MainScreen(navController = navController)
        }

        // Новые маршруты для админа
        composable(
            route = NavigationRoutes.AdminQrScanner.route,
            enterTransition = { Transitions.enterScale() },
            exitTransition = { Transitions.exitScale() }
        ) {
            AdminQrScannerScreen(navController = navController)
        }

        composable(
            route = NavigationRoutes.AdminPointsHistory.route,
            enterTransition = { Transitions.enterScale() },
            exitTransition = { Transitions.exitScale() }
        ) {
            // Здесь будет экран истории начисления баллов
            // PointsHistoryScreen(navController = navController)
        }

        // Существующие маршруты остаются без изменений
        composable(
            route = NavigationRoutes.QrCodeFullscreen.route,
            enterTransition = { Transitions.enterScale() },
            exitTransition = { Transitions.exitScale() }
        ) {
            QrCodeFullscreenScreen(navController = navController)
        }

        composable(
            route = NavigationRoutes.QrScanner.route,
            enterTransition = { Transitions.enterScale() },
            exitTransition = { Transitions.exitScale() }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            QrScannerScreen(
                navController = navController,
                productId = productId ?: ""
            )
        }
    }
}