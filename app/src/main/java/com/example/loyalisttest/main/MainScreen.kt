package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.ui.theme.Transitions
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val mainNavController = rememberNavController()

    DisposableEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null) {
                navController.navigate(NavigationRoutes.Welcome.route) {
                    popUpTo(NavigationRoutes.Main.route) { inclusive = true }
                }
            }
        }

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)

        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        }
    }

    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(NavigationRoutes.Home.route, "Главная", Icons.Default.Home),
                    Triple(NavigationRoutes.Catalog.route, "Каталог", Icons.Default.List),
                    Triple(NavigationRoutes.Settings.route, "Настройки", Icons.Default.Settings)
                ).forEach { (route, title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = currentRoute == route,
                        onClick = {
                            if (currentRoute != route) {
                                mainNavController.navigate(route) {
                                    popUpTo(mainNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = NavigationRoutes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = NavigationRoutes.Home.route,
                enterTransition = {
                    Transitions.bottomNavEnterTransition(
                        initialState,
                        targetState
                    )
                },
                exitTransition = { Transitions.bottomNavExitTransition(initialState, targetState) }
            ) {
                HomeScreen(mainNavController)
            }

            composable(
                route = NavigationRoutes.Catalog.route,
                enterTransition = {
                    Transitions.bottomNavEnterTransition(
                        initialState,
                        targetState
                    )
                },
                exitTransition = { Transitions.bottomNavExitTransition(initialState, targetState) }
            ) {
                CatalogScreen(mainNavController)
            }

            composable(
                route = NavigationRoutes.Settings.route,
                enterTransition = {
                    Transitions.bottomNavEnterTransition(
                        initialState,
                        targetState
                    )
                },
                exitTransition = { Transitions.bottomNavExitTransition(initialState, targetState) }
            ) {
                SettingsScreen()
            }

            composable(
                route = NavigationRoutes.QrCodeFullscreen.route,
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) {
                QrCodeFullscreenScreen(mainNavController)
            }

            // Добавляем маршрут для добавления товара
            composable(
                route = NavigationRoutes.AddProduct.route,
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) {
                AddProductScreen(mainNavController)
            }

            // Добавляем новый маршрут для сканера QR-кода
            composable(
                route = NavigationRoutes.QrScanner.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.StringType
                    }
                ),
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                    ?: return@composable
                QrScannerScreen(
                    navController = mainNavController,
                    productId = productId
                )
            }
        }
    }
}