package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.loyalisttest.R
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.ui.theme.Transitions
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val mainNavController = rememberNavController()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

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

    // Bottom navigation items
    val navItems = listOf(
        Triple(
            NavigationRoutes.Catalog.route,
            stringResource(R.string.nav_catalog),
            Icons.Default.List
        ),
        Triple(
            NavigationRoutes.Settings.route,
            stringResource(R.string.nav_settings),
            Icons.Default.Settings
        )
    )

    Scaffold(
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    navItems.forEach { (route, title, icon) ->
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
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                NavigationRail {
                    navItems.forEach { (route, title, icon) ->
                        NavigationRailItem(
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

            NavHost(
                navController = mainNavController,
                startDestination = NavigationRoutes.Catalog.route,
                modifier = Modifier.padding(paddingValues).weight(1f)
            ) {
            // Base screens
            composable(
                route = NavigationRoutes.Catalog.route,
                enterTransition = { Transitions.bottomNavEnterTransition(initialState, targetState) },
                exitTransition = { Transitions.bottomNavExitTransition(initialState, targetState) }
            ) {
                CatalogScreen(mainNavController)
            }

            composable(
                route = NavigationRoutes.Settings.route,
                enterTransition = { Transitions.bottomNavEnterTransition(initialState, targetState) },
                exitTransition = { Transitions.bottomNavExitTransition(initialState, targetState) }
            ) {
                SettingsScreen()
            }

            // Points history
            composable(
                route = NavigationRoutes.PointsHistory.route,
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) {
                PointsHistoryScreen(mainNavController)
            }

            // Adding cafe and products
            composable(
                route = NavigationRoutes.AddCafe.route,
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) {
                AddCafeScreen(mainNavController)
            }

            composable(
                route = NavigationRoutes.AddProduct.route,
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) {
                AddProductScreen(mainNavController)
            }

            // QR scanning
            composable(
                route = NavigationRoutes.QrScanner.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType }
                ),
                enterTransition = { Transitions.enterScale() },
                exitTransition = { Transitions.exitScale() }
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                QrScannerScreen(
                    navController = mainNavController,
                    productId = productId
                )
            }
        }
    }
}