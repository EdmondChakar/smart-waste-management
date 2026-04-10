package com.example.smartwastemobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartwastemobile.feature.auth.presentation.signin.SignInScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SIGN_IN
    ) {
        composable(route = AppRoutes.SIGN_IN) {
            SignInScreen()
        }
    }
}