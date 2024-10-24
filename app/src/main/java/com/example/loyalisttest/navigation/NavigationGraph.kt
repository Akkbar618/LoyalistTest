package com.example.loyalisttest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.loyalisttest.auth.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Welcome.route
    ) {
        composable(route = NavigationRoutes.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate(NavigationRoutes.SignIn.route)
                },
                onNavigateToSignUp = {
                    navController.navigate(NavigationRoutes.SignUp.route)
                }
            )
        }

        composable(route = NavigationRoutes.SignIn.route) {
            SignInScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onForgotPasswordClick = {
                    navController.navigate(NavigationRoutes.ForgotPassword.route)
                },
                onSignInClick = { email, password ->
                    // Будет реализовано позже
                },
                onRegisterClick = {
                    navController.navigate(NavigationRoutes.SignUp.route) {
                        popUpTo(NavigationRoutes.SignIn.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = NavigationRoutes.SignUp.route) {
            SignUpScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onSignUpClick = { name, email, password ->
                    // Будет реализовано позже
                },
                onSignInClick = {
                    navController.navigate(NavigationRoutes.SignIn.route) {
                        popUpTo(NavigationRoutes.SignUp.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = NavigationRoutes.ForgotPassword.route) {
            // Будет реализовано позже
        }

        composable(route = NavigationRoutes.ResetPassword.route) {
            // Будет реализовано позже
        }

        composable(route = NavigationRoutes.PasswordChanged.route) {
            // Будет реализовано позже
        }

        composable(route = NavigationRoutes.VerificationCode.route) {
            // Будет реализовано позже
        }
    }
}