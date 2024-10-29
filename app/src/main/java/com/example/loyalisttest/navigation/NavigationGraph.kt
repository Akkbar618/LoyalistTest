package com.example.loyalisttest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.loyalisttest.auth.*
import com.example.loyalisttest.navigation.NavigationActions.navigateToForgotPassword
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignIn
import com.example.loyalisttest.navigation.NavigationActions.navigateToSignUp

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Welcome.route
    ) {
        composable(route = NavigationRoutes.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigateToSignIn()
                },
                onNavigateToSignUp = {
                    navController.navigateToSignUp()
                }
            )
        }

        composable(route = NavigationRoutes.SignIn.route) {
            SignInScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onForgotPasswordClick = {
                    navController.navigateToForgotPassword()
                },
                onSignInClick = { email, password ->
                    // Будет реализовано позже
                },
                onRegisterClick = {
                    navController.navigateToSignUp(NavigationRoutes.SignIn.route)
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
                    navController.navigateToSignIn(NavigationRoutes.SignUp.route)
                }
            )
        }

        composable(route = NavigationRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onSignInClick = {
                    navController.navigateToSignIn(NavigationRoutes.Welcome.route)
                }
            )
        }
    }
}