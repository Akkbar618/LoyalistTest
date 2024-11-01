package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.main.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val mainNavController = rememberNavController()

    // Получаем текущий route
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
            composable(NavigationRoutes.Home.route) {
                HomeScreen(mainNavController)
            }
            composable(NavigationRoutes.Catalog.route) {
                CatalogScreen()
            }
            composable(NavigationRoutes.Settings.route) {
                SettingsScreen()
            }
            composable(NavigationRoutes.QrCodeFullscreen.route) {
                QrCodeFullscreenScreen(mainNavController)
            }
        }
    }
}