package com.example.loyalisttest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.loyalisttest.auth.AuthCheck
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.navigation.SetupNavGraph
import com.example.loyalisttest.ui.theme.LoyalistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoyalistTheme {
                val navController = rememberNavController()
                var startDestination by remember {
                    mutableStateOf<String?>(null)
                }

                if (startDestination == null) {
                    AuthCheck(
                        onAuthenticated = { user ->
                            startDestination = NavigationRoutes.Main.route
                        },
                        onNeedAuthentication = {
                            startDestination = NavigationRoutes.Welcome.route
                        }
                    )
                } else {
                    SetupNavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }
}