package com.example.smartwastemobile.feature.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.smartwastemobile.feature.account.presentation.AccountScreen
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import com.example.smartwastemobile.feature.home.presentation.HomeScreen
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import com.example.smartwastemobile.feature.rewards.presentation.RewardsScreen
import com.example.smartwastemobile.feature.scan.data.model.ScanResultDto
import com.example.smartwastemobile.feature.scan.presentation.ScanScreen

private enum class MainTab(val label: String) {
    HOME("Home"),
    SCAN("Scan"),
    REWARDS("Rewards"),
    ACCOUNT("Account")
}

@Composable
fun AppShellScreen(
    user: UserDto?,
    isAuthLoading: Boolean,
    authErrorMessage: String?,
    rewards: List<RewardDto>,
    isLoadingRewards: Boolean,
    rewardsErrorMessage: String?,
    pointsBalance: PointsBalanceDto?,
    pointsHistory: List<PointsHistoryItemDto>,
    isLoadingPoints: Boolean,
    pointsErrorMessage: String?,
    redemptions: List<RedemptionDto>,
    isLoadingRedemptions: Boolean,
    redemptionsErrorMessage: String?,
    isSubmittingRedemption: Boolean,
    redeemingRewardId: Int?,
    redemptionFeedbackMessage: String?,
    isSubmittingScan: Boolean,
    scanErrorMessage: String?,
    lastScanResult: ScanResultDto?,
    onRefreshProfile: () -> Unit,
    onSignOut: () -> Unit,
    onRefreshRewards: () -> Unit,
    onRefreshPoints: () -> Unit,
    onRefreshRedemptions: () -> Unit,
    onRedeemReward: (Int) -> Unit,
    onClearRedemptionFeedback: () -> Unit,
    onSubmitScan: (String) -> Unit,
    onClearScanFeedback: () -> Unit,
    onScanCancelled: () -> Unit,
    onScannerError: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.label.take(1)) },
                        label = { Text(text = tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    user = user,
                    rewardsCount = rewards.size,
                    pointsBalance = pointsBalance,
                    isLoadingPoints = isLoadingPoints,
                    pointsErrorMessage = pointsErrorMessage,
                    onRefreshPoints = onRefreshPoints
                )

                MainTab.SCAN -> ScanScreen(
                    isSubmittingScan = isSubmittingScan,
                    scanErrorMessage = scanErrorMessage,
                    lastScanResult = lastScanResult,
                    onSubmitScan = onSubmitScan,
                    onClearFeedback = onClearScanFeedback,
                    onScanCancelled = onScanCancelled,
                    onScannerError = onScannerError
                )

                MainTab.REWARDS -> RewardsScreen(
                    rewards = rewards,
                    pointsBalance = pointsBalance,
                    isLoading = isLoadingRewards,
                    errorMessage = rewardsErrorMessage,
                    isSubmittingRedemption = isSubmittingRedemption,
                    redeemingRewardId = redeemingRewardId,
                    redemptionFeedbackMessage = redemptionFeedbackMessage,
                    redemptionErrorMessage = redemptionsErrorMessage,
                    onRetry = onRefreshRewards,
                    onRedeemReward = onRedeemReward,
                    onClearRedemptionFeedback = onClearRedemptionFeedback
                )

                MainTab.ACCOUNT -> AccountScreen(
                    user = user,
                    isLoading = isAuthLoading,
                    errorMessage = authErrorMessage,
                    pointsBalance = pointsBalance,
                    pointsHistory = pointsHistory,
                    isLoadingPoints = isLoadingPoints,
                    pointsErrorMessage = pointsErrorMessage,
                    redemptions = redemptions,
                    isLoadingRedemptions = isLoadingRedemptions,
                    redemptionsErrorMessage = redemptionsErrorMessage,
                    onRefreshProfile = onRefreshProfile,
                    onRefreshPoints = onRefreshPoints,
                    onRefreshRedemptions = onRefreshRedemptions,
                    onSignOut = onSignOut
                )
            }
        }
    }
}
