package com.example.loyalisttest.navigation

sealed class NavigationRoutes(val route: String) {
    data object Welcome : NavigationRoutes("welcome")
    data object SignIn : NavigationRoutes("sign_in")
    data object SignUp : NavigationRoutes("sign_up")
    data object ForgotPassword : NavigationRoutes("forgot_password")
    data object Main : NavigationRoutes("main")
    data object Home : NavigationRoutes("main/home")
    data object Catalog : NavigationRoutes("main/catalog")
    data object Settings : NavigationRoutes("main/settings")
    data object QrCodeFullscreen : NavigationRoutes("main/home/qr_fullscreen")

    companion object {
        fun fromRoute(route: String?): NavigationRoutes {
            return when(route) {
                Welcome.route -> Welcome
                SignIn.route -> SignIn
                SignUp.route -> SignUp
                ForgotPassword.route -> ForgotPassword
                Main.route -> Main
                Home.route -> Home
                Catalog.route -> Catalog
                Settings.route -> Settings
                QrCodeFullscreen.route -> QrCodeFullscreen
                else -> Welcome
            }
        }
    }
}