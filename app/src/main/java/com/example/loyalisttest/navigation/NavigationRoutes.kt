package com.example.loyalisttest.navigation

sealed class Screen(val route: String)

object NavigationRoutes {
    data object Welcome : Screen("welcome_screen")
    data object SignIn : Screen("sign_in_screen")
    data object SignUp : Screen("sign_up_screen")
    data object ForgotPassword : Screen("forgot_password_screen")
    data object ResetPassword : Screen("reset_password_screen")
    data object PasswordChanged : Screen("password_changed_screen")
    data object VerificationCode : Screen("verification_code_screen")
}