package com.example.smartwastemobile.feature.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.account.presentation.AccountScreen
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import com.example.smartwastemobile.feature.home.presentation.HomeScreen
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import com.example.smartwastemobile.feature.points.presentation.PointsSummaryScreen
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto
import com.example.smartwastemobile.feature.redemptions.presentation.RedemptionsScreen
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import com.example.smartwastemobile.feature.rewards.presentation.RewardsScreen
import com.example.smartwastemobile.feature.scan.data.model.ScanResultDto
import com.example.smartwastemobile.feature.scan.presentation.ScanScreen

private enum class MainTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    SCAN("Scan", Icons.Outlined.CropFree),
    REWARDS("Rewards", Icons.Outlined.CardGiftcard),
    ACCOUNT("Account", Icons.Outlined.AccountCircle)
}

private enum class AccountDetailScreen {
    POINTS_SUMMARY,
    REDEMPTIONS
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
    recentlyRedeemedRewardId: Int?,
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
    var accountDetailScreen by rememberSaveable { mutableStateOf<AccountDetailScreen?>(null) }

    Scaffold(
        bottomBar = {
            if (accountDetailScreen == null) {
                Surface(
                    color = Color.White,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        MainTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = {
                                    selectedTab = tab
                                    accountDetailScreen = null
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (selectedTab == tab) FontWeight.Medium else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1A4D2E),
                                    selectedTextColor = Color(0xFF1A4D2E),
                                    indicatorColor = Color(0xFFE8F0E9),
                                    unselectedIconColor = Color(0xFFA1A1AA),
                                    unselectedTextColor = Color(0xFF8E8EA0)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    user = user,
                    rewards = rewards,
                    pointsBalance = pointsBalance,
                    isLoadingPoints = isLoadingPoints,
                    isLoadingRewards = isLoadingRewards,
                    pointsErrorMessage = pointsErrorMessage,
                    onRefreshHomeData = {
                        onRefreshPoints()
                        onRefreshRewards()
                    },
                    onOpenScan = { selectedTab = MainTab.SCAN },
                    onOpenRewards = { selectedTab = MainTab.REWARDS }
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
                    recentlyRedeemedRewardId = recentlyRedeemedRewardId,
                    redemptionErrorMessage = redemptionsErrorMessage,
                    onRetry = onRefreshRewards,
                    onRedeemReward = onRedeemReward,
                    onClearRedemptionFeedback = onClearRedemptionFeedback
                )

                MainTab.ACCOUNT -> {
                    when (accountDetailScreen) {
                        AccountDetailScreen.POINTS_SUMMARY -> PointsSummaryScreen(
                            pointsBalance = pointsBalance,
                            pointsHistory = pointsHistory,
                            isLoadingPoints = isLoadingPoints,
                            pointsErrorMessage = pointsErrorMessage,
                            onBack = { accountDetailScreen = null },
                            onRefreshPoints = onRefreshPoints
                        )

                        AccountDetailScreen.REDEMPTIONS -> RedemptionsScreen(
                            redemptions = redemptions,
                            isLoadingRedemptions = isLoadingRedemptions,
                            redemptionsErrorMessage = redemptionsErrorMessage,
                            onBack = { accountDetailScreen = null },
                            onRefreshRedemptions = onRefreshRedemptions
                        )

                        null -> AccountScreen(
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
                            onOpenPointsSummary = {
                                accountDetailScreen = AccountDetailScreen.POINTS_SUMMARY
                            },
                            onOpenRedemptions = {
                                accountDetailScreen = AccountDetailScreen.REDEMPTIONS
                            },
                            onRefreshProfile = onRefreshProfile,
                            onRefreshPoints = onRefreshPoints,
                            onRefreshRedemptions = onRefreshRedemptions,
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }
}
