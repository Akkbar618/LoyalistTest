package com.example.loyalisttest.navigation

import androidx.navigation.NavHostController

sealed class NavigationRoutes(val route: String) {
    data object Welcome : NavigationRoutes("welcome")
    data object SignIn : NavigationRoutes("sign_in")
    data object SignUp : NavigationRoutes("sign_up")
    data object ForgotPassword : NavigationRoutes("forgot_password")

    companion object {
        fun fromRoute(route: String?): NavigationRoutes {
            return when(route) {
                Welcome.route -> Welcome
                SignIn.route -> SignIn
                SignUp.route -> SignUp
                ForgotPassword.route -> ForgotPassword
                else -> Welcome
            }
        }
    }
}

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
}