package com.example.smartwastemobile.feature.account.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.core.ui.components.AppPrimaryButton
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto

@Composable
fun AccountScreen(
    user: UserDto?,
    isLoading: Boolean,
    errorMessage: String?,
    pointsBalance: PointsBalanceDto?,
    pointsHistory: List<PointsHistoryItemDto>,
    isLoadingPoints: Boolean,
    pointsErrorMessage: String?,
    redemptions: List<RedemptionDto>,
    isLoadingRedemptions: Boolean,
    redemptionsErrorMessage: String?,
    onRefreshProfile: () -> Unit,
    onRefreshPoints: () -> Unit,
    onRefreshRedemptions: () -> Unit,
    onSignOut: () -> Unit
) {
    if (isLoading && user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My Account",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (user != null) {
                        Text(text = "Email: ${user.email}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "User ID: ${user.userId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Role ID: ${user.roleId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Status ID: ${user.statusId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Created At: ${user.createdAt}")
                    } else {
                        Text(text = "No signed-in user found.")
                    }
                }
            }
        }

        item {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Points Summary",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when {
                        isLoadingPoints && pointsBalance == null -> {
                            Text(text = "Loading points...")
                        }

                        pointsBalance != null -> {
                            Text(text = "Current Balance: ${pointsBalance.currentPointsBalance}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Total Earned: ${pointsBalance.totalEarned}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Total Redeemed: ${pointsBalance.totalRedeemed}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Total Transactions: ${pointsBalance.totalTransactions}")
                        }

                        else -> {
                            Text(
                                text = pointsErrorMessage ?: "Points data is unavailable.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AppPrimaryButton(
                        text = if (isLoadingPoints) "Refreshing..." else "Refresh Points",
                        enabled = !isLoadingPoints,
                        onClick = onRefreshPoints
                    )
                }
            }
        }

        item {
            Text(
                text = "Recent Points Activity",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (pointsHistory.isEmpty()) {
            item {
                Text(text = "No points activity yet.")
            }
        } else {
            items(pointsHistory, key = { it.txnId }) { item ->
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${item.type} ${item.points}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        item.binCode?.let { binCode ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Bin: $binCode")
                        }
                        item.itemCount?.let { itemCount ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Items: $itemCount")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.createdAt,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "My Redemptions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    when {
                        isLoadingRedemptions && redemptions.isEmpty() -> {
                            Text(text = "Loading redemption history...")
                        }

                        !redemptionsErrorMessage.isNullOrBlank() && redemptions.isEmpty() -> {
                            Text(
                                text = redemptionsErrorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        redemptions.isEmpty() -> {
                            Text(text = "No redemption requests yet.")
                        }

                        else -> {
                            redemptions.forEach { redemption ->
                                Surface(
                                    modifier = Modifier.padding(top = 8.dp),
                                    tonalElevation = 1.dp,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = redemption.rewardTitle,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Status: ${redemption.statusCode}")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Points Spent: ${redemption.pointsSpent}")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Requested At: ${redemption.requestedAt}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        redemption.fulfilledAt?.let { fulfilledAt ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Fulfilled At: $fulfilledAt",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        redemption.voucherCode?.let { voucherCode ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Voucher: $voucherCode")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AppPrimaryButton(
                        text = if (isLoadingRedemptions) "Refreshing..." else "Refresh Redemptions",
                        enabled = !isLoadingRedemptions,
                        onClick = onRefreshRedemptions
                    )
                }
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            AppPrimaryButton(
                text = if (isLoading) "Refreshing..." else "Refresh Profile",
                enabled = !isLoading && user != null,
                onClick = onRefreshProfile
            )
        }

        item {
            AppPrimaryButton(
                text = "Sign Out",
                enabled = !isLoading,
                onClick = onSignOut
            )
        }
    }
}
