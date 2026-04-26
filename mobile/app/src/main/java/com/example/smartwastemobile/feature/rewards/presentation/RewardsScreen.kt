package com.example.smartwastemobile.feature.rewards.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RewardsScreen(
    rewards: List<RewardDto>,
    pointsBalance: PointsBalanceDto?,
    isLoading: Boolean,
    errorMessage: String?,
    isSubmittingRedemption: Boolean,
    redeemingRewardId: Int?,
    redemptionFeedbackMessage: String?,
    recentlyRedeemedRewardId: Int?,
    redemptionErrorMessage: String?,
    onRetry: () -> Unit,
    onRedeemReward: (Int) -> Unit,
    onClearRedemptionFeedback: () -> Unit
) {
    val backgroundColor = Color(0xFFFCFCFB)
    val currentPoints = pointsBalance?.currentPointsBalance ?: 0

    when {
        isLoading && rewards.isEmpty() -> {
            RewardsCenteredState(
                title = "Loading Rewards",
                body = "Fetching the latest rewards for you.",
                primaryActionLabel = null,
                onPrimaryAction = null,
                backgroundColor = backgroundColor,
                content = {
                    CircularProgressIndicator(
                        color = Color(0xFF1A4D2E),
                        strokeWidth = 3.dp
                    )
                }
            )
        }

        !errorMessage.isNullOrBlank() && rewards.isEmpty() -> {
            RewardsCenteredState(
                title = "Couldn’t Load Rewards",
                body = errorMessage,
                primaryActionLabel = "Retry",
                onPrimaryAction = onRetry,
                backgroundColor = backgroundColor
            )
        }

        rewards.isEmpty() -> {
            RewardsCenteredState(
                title = "No Rewards Yet",
                body = "No active rewards are available right now.",
                primaryActionLabel = "Refresh",
                onPrimaryAction = onRetry,
                backgroundColor = backgroundColor
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .statusBarsPadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rewards",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF121826)
                                )
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                tonalElevation = 0.dp
                            ) {
                                IconButton(
                                    onClick = onRetry,
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color(0xFF1A4D2E),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = "Refresh rewards",
                                            tint = Color(0xFF667085)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Exchange your eco-points for vouchers and discounts.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF667085)
                            )
                        )
                    }
                }

                item {
                    RewardsBalanceCard(currentPoints = currentPoints)
                }

                if (!errorMessage.isNullOrBlank()) {
                    item {
                        RewardsMessageCard(
                            message = errorMessage,
                            backgroundColor = Color(0xFFFEE2E2),
                            textColor = Color(0xFFB42318)
                        )
                    }
                }

                if (!redemptionErrorMessage.isNullOrBlank()) {
                    item {
                        RewardsMessageCard(
                            message = redemptionErrorMessage,
                            backgroundColor = Color(0xFFFEE2E2),
                            textColor = Color(0xFFB42318)
                        )
                    }
                }

                if (!redemptionFeedbackMessage.isNullOrBlank() && recentlyRedeemedRewardId == null) {
                    item {
                        RewardsMessageCard(
                            message = redemptionFeedbackMessage,
                            backgroundColor = Color(0xFFEAF8EE),
                            textColor = Color(0xFF166534)
                        )
                    }
                }

                items(rewards, key = { it.rewardId }) { reward ->
                    RewardCard(
                        reward = reward,
                        currentPointsBalance = currentPoints,
                        isSubmittingRedemption = isSubmittingRedemption,
                        isRedeemingThisReward = redeemingRewardId == reward.rewardId,
                        isRecentlyRedeemed = recentlyRedeemedRewardId == reward.rewardId,
                        onRedeemReward = onRedeemReward,
                        onClearRedemptionFeedback = onClearRedemptionFeedback
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun RewardsCenteredState(
    title: String,
    body: String,
    primaryActionLabel: String?,
    onPrimaryAction: (() -> Unit)?,
    backgroundColor: Color,
    content: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content?.invoke()
        if (content != null) {
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121826)
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color(0xFF667085)
            )
        )
        if (!primaryActionLabel.isNullOrBlank() && onPrimaryAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onPrimaryAction,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4D2E),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Text(
                    text = primaryActionLabel,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RewardsBalanceCard(currentPoints: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF1F6EF),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFF1A4D2E),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = "YOUR BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF475467),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatPoints(currentPoints)} pts",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF101828),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun RewardsMessageCard(
    message: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun RewardCard(
    reward: RewardDto,
    currentPointsBalance: Int,
    isSubmittingRedemption: Boolean,
    isRedeemingThisReward: Boolean,
    isRecentlyRedeemed: Boolean,
    onRedeemReward: (Int) -> Unit,
    onClearRedemptionFeedback: () -> Unit
) {
    val hasEnoughPoints = currentPointsBalance >= reward.pointsCost
    val isLocked = !hasEnoughPoints && !isRecentlyRedeemed

    val iconContainerColor = if (isLocked) Color(0xFFF2F4F7) else Color(0xFF1A4D2E)
    val iconTint = if (isLocked) Color(0xFFD0D5DD) else Color.White
    val titleColor = if (isLocked) Color(0xFF667085) else Color(0xFF101828)
    val bodyColor = if (isLocked) Color(0xFF98A2B3) else Color(0xFF667085)
    val costColor = if (isLocked) Color(0xFFB0B7C3) else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconContainerColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CardGiftcard,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = titleColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reward.description?.takeIf { it.isNotBlank() }
                            ?: "No description available.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = bodyColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF2F4F7), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = costColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatPoints(reward.pointsCost),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isLocked) Color(0xFF98A2B3) else Color(0xFF101828),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                RewardActionButton(
                    isLocked = isLocked,
                    isSubmittingRedemption = isSubmittingRedemption,
                    isRedeemingThisReward = isRedeemingThisReward,
                    isRecentlyRedeemed = isRecentlyRedeemed,
                    onClick = {
                        onClearRedemptionFeedback()
                        onRedeemReward(reward.rewardId)
                    }
                )
            }
        }
    }
}

@Composable
private fun RewardActionButton(
    isLocked: Boolean,
    isSubmittingRedemption: Boolean,
    isRedeemingThisReward: Boolean,
    isRecentlyRedeemed: Boolean,
    onClick: () -> Unit
) {
    when {
        isRecentlyRedeemed -> {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFEAF8EE),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF12B76A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Redeemed",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF12B76A),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        isLocked -> {
            Button(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF7F7F5),
                    contentColor = Color(0xFFB8C2B0),
                    disabledContainerColor = Color(0xFFF7F7F5),
                    disabledContentColor = Color(0xFFB8C2B0)
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        else -> {
            Button(
                onClick = onClick,
                enabled = !isSubmittingRedemption,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4D2E),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFAAC3B3),
                    disabledContentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                if (isSubmittingRedemption && isRedeemingThisReward) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = "Redeem",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

private fun formatPoints(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}
