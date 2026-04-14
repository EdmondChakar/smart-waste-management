package com.example.smartwastemobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartwastemobile.core.network.ApiClient
import com.example.smartwastemobile.core.session.SessionManager
import com.example.smartwastemobile.feature.auth.data.AuthRepository
import com.example.smartwastemobile.feature.auth.presentation.AuthViewModel
import com.example.smartwastemobile.feature.auth.presentation.AuthViewModelFactory
import com.example.smartwastemobile.feature.auth.presentation.signin.SignInScreen
import com.example.smartwastemobile.feature.auth.presentation.signup.SignUpScreen
import com.example.smartwastemobile.feature.main.presentation.AppShellScreen
import com.example.smartwastemobile.feature.main.presentation.LoadingScreen
import com.example.smartwastemobile.feature.main.presentation.MainViewModel
import com.example.smartwastemobile.feature.main.presentation.MainViewModelFactory
import com.example.smartwastemobile.feature.points.data.PointsRepository
import com.example.smartwastemobile.feature.rewards.data.RewardsRepository
import com.example.smartwastemobile.feature.scan.data.ScanRepository

@Composable
fun AppNavGraph() {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember {
        AuthRepository(
            authApi = ApiClient.authApi,
            sessionManager = sessionManager
        )
    }
    val rewardsRepository = remember {
        RewardsRepository(
            rewardsApi = ApiClient.rewardsApi
        )
    }
    val pointsRepository = remember {
        PointsRepository(
            pointsApi = ApiClient.pointsApi,
            sessionManager = sessionManager
        )
    }
    val scanRepository = remember {
        ScanRepository(
            scanApi = ApiClient.scanApi,
            sessionManager = sessionManager
        )
    }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            rewardsRepository = rewardsRepository,
            pointsRepository = pointsRepository,
            scanRepository = scanRepository
        )
    )
    val uiState by authViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.currentUser?.userId) {
        if (uiState.currentUser != null) {
            mainViewModel.refreshAuthenticatedData()
        }
    }

    if (uiState.isInitializing) {
        LoadingScreen()
        return
    }

    if (uiState.currentUser != null) {
        AppShellScreen(
            user = uiState.currentUser,
            isAuthLoading = uiState.isLoading,
            authErrorMessage = uiState.errorMessage,
            rewards = mainUiState.rewards,
            isLoadingRewards = mainUiState.isLoadingRewards,
            rewardsErrorMessage = mainUiState.rewardsErrorMessage,
            pointsBalance = mainUiState.pointsBalance,
            pointsHistory = mainUiState.pointsHistory,
            isLoadingPoints = mainUiState.isLoadingPoints,
            pointsErrorMessage = mainUiState.pointsErrorMessage,
            isSubmittingScan = mainUiState.isSubmittingScan,
            scanErrorMessage = mainUiState.scanErrorMessage,
            lastScanResult = mainUiState.lastScanResult,
            onRefreshProfile = authViewModel::refreshProfile,
            onSignOut = authViewModel::signOut,
            onRefreshRewards = mainViewModel::refreshRewards,
            onRefreshPoints = mainViewModel::refreshPointsData,
            onSubmitScan = mainViewModel::submitScan,
            onClearScanFeedback = mainViewModel::clearScanFeedback,
            onScanCancelled = mainViewModel::markScanCancelled,
            onScannerError = mainViewModel::setScanError
        )
        return
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SIGN_IN
    ) {
        composable(route = AppRoutes.SIGN_IN) {
            SignInScreen(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                successMessage = uiState.successMessage,
                onSignIn = authViewModel::signIn,
                onNavigateToSignUp = {
                    authViewModel.clearMessages()
                    navController.navigate(AppRoutes.SIGN_UP)
                },
                onMessageShown = authViewModel::clearMessages
            )
        }

        composable(route = AppRoutes.SIGN_UP) {
            SignUpScreen(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onSignUp = { email, password ->
                    authViewModel.signUp(email, password) {
                        navController.popBackStack()
                    }
                },
                onNavigateToSignIn = {
                    authViewModel.clearMessages()
                    navController.popBackStack()
                },
                onMessageShown = authViewModel::clearMessages
            )
        }
    }
}
