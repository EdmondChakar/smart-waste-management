package com.example.smartwastemobile.feature.rewards.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.core.ui.components.AppPrimaryButton
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto

@Composable
fun RewardsScreen(
    rewards: List<RewardDto>,
    pointsBalance: PointsBalanceDto?,
    isLoading: Boolean,
    errorMessage: String?,
    isSubmittingRedemption: Boolean,
    redeemingRewardId: Int?,
    redemptionFeedbackMessage: String?,
    redemptionErrorMessage: String?,
    onRetry: () -> Unit,
    onRedeemReward: (Int) -> Unit,
    onClearRedemptionFeedback: () -> Unit
) {
    when {
        isLoading && rewards.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Loading rewards...")
            }
        }

        !errorMessage.isNullOrBlank() && rewards.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppPrimaryButton(
                    text = "Retry",
                    onClick = onRetry
                )
            }
        }

        rewards.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No active rewards are available yet.")
                Spacer(modifier = Modifier.height(16.dp))
                AppPrimaryButton(
                    text = "Refresh",
                    onClick = onRetry
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    item {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    Text(
                        text = "Active Rewards",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                pointsBalance?.let { balance ->
                    item {
                        Text(
                            text = "Current points: ${balance.currentPointsBalance}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (!redemptionFeedbackMessage.isNullOrBlank()) {
                    item {
                        Text(text = redemptionFeedbackMessage)
                    }
                }

                if (!redemptionErrorMessage.isNullOrBlank()) {
                    item {
                        Text(
                            text = redemptionErrorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                items(rewards, key = { it.rewardId }) { reward ->
                    RewardCard(
                        reward = reward,
                        currentPointsBalance = pointsBalance?.currentPointsBalance,
                        isSubmittingRedemption = isSubmittingRedemption,
                        isRedeemingThisReward = redeemingRewardId == reward.rewardId,
                        onRedeemReward = onRedeemReward,
                        onClearRedemptionFeedback = onClearRedemptionFeedback
                    )
                }

                item {
                    AppPrimaryButton(
                        text = if (isLoading) "Refreshing..." else "Refresh Rewards",
                        enabled = !isLoading,
                        onClick = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardCard(
    reward: RewardDto,
    currentPointsBalance: Int?,
    isSubmittingRedemption: Boolean,
    isRedeemingThisReward: Boolean,
    onRedeemReward: (Int) -> Unit,
    onClearRedemptionFeedback: () -> Unit
) {
    val hasEnoughPoints = currentPointsBalance == null || currentPointsBalance >= reward.pointsCost

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reward.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reward.description?.takeIf { it.isNotBlank() } ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${reward.pointsCost} points",
                style = MaterialTheme.typography.labelLarge
            )
            if (!hasEnoughPoints) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Not enough points yet for this reward.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AppPrimaryButton(
                text = if (isRedeemingThisReward && isSubmittingRedemption) {
                    "Redeeming..."
                } else {
                    "Redeem Reward"
                },
                enabled = !isSubmittingRedemption && hasEnoughPoints,
                onClick = {
                    onClearRedemptionFeedback()
                    onRedeemReward(reward.rewardId)
                }
            )
        }
    }
}
