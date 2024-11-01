package com.example.loyalisttest.navigation

import androidx.navigation.NavHostController

object NavigationActions {
    fun NavHostController.navigateToSignIn(popUpToRoute: String? = null) {
        navigate(NavigationRoutes.SignIn.route) {
            popUpToRoute?.let {
                popUpTo(it) { inclusive = true }
            }
        }
    }

    fun NavHostController.navigateToSignUp(popUpToRoute: String? = null) {
        navigate(NavigationRoutes.SignUp.route) {
            popUpToRoute?.let {
                popUpTo(it) { inclusive = true }
            }
        }
    }

    fun NavHostController.navigateToForgotPassword() {
        navigate(NavigationRoutes.ForgotPassword.route)
    }

    fun NavHostController.navigateToMain() {
        navigate(NavigationRoutes.Main.route) {
            popUpTo(NavigationRoutes.Welcome.route) { inclusive = true }
        }
    }
}