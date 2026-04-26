package com.example.smartwastemobile.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import java.util.Locale

@Composable
fun HomeScreen(
    user: UserDto?,
    rewards: List<RewardDto>,
    pointsBalance: PointsBalanceDto?,
    isLoadingPoints: Boolean,
    isLoadingRewards: Boolean,
    pointsErrorMessage: String?,
    onRefreshHomeData: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenRewards: () -> Unit
) {
    val displayName = user?.email?.substringBefore("@")?.ifBlank { null } ?: "user"
    val explicitlyFeaturedRewards = rewards.filter { it.isActive && it.isFeatured }
    val featuredRewards = rewards
        .filter { it.isActive && it.isFeatured }
        .ifEmpty { rewards.filter { it.isActive } }
    val currentPoints = pointsBalance?.currentPointsBalance ?: 0
    val totalEarned = pointsBalance?.totalEarned ?: 0
    val totalRedeemed = pointsBalance?.totalRedeemed ?: 0
    val availableValue = currentPoints / 100.0
    val isRefreshingHomeData = isLoadingPoints || isLoadingRewards

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Good Morning,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF71717A),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF18181B)
                    )
                )
            }

            Surface(
                onClick = onRefreshHomeData,
                enabled = !isRefreshingHomeData,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh home data",
                        tint = if (isRefreshingHomeData) Color(0xFF1A4D2E) else Color(0xFF71717A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF174F2F), Color(0xFF2A6A45))
                    )
                )
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Column {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Points",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                when {
                    isLoadingPoints && pointsBalance == null -> {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    pointsBalance != null -> {
                        Text(
                            text = String.format(Locale.US, "%,d", currentPoints),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Equals approx $${String.format(Locale.US, "%.2f", availableValue)} value",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFE7F4EA)
                            )
                        )
                    }

                    else -> {
                        Text(
                            text = "Unavailable",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pointsErrorMessage ?: "Points could not be loaded.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFE7F4EA)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFE8EDE7),
                iconContainerColor = Color(0xFF1A4D2E),
                icon = Icons.Outlined.Eco,
                value = String.format(Locale.US, "%,d", totalEarned),
                label = "Total Earned"
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFF5EFE6),
                iconContainerColor = Color(0xFFD4A373),
                icon = Icons.Outlined.WaterDrop,
                value = String.format(Locale.US, "%,d", totalRedeemed),
                label = "Total Redeemed"
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (explicitlyFeaturedRewards.isNotEmpty()) {
                    "Featured Rewards (${explicitlyFeaturedRewards.size})"
                } else {
                    "Featured Rewards"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF18181B)
                )
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenRewards)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF1A4D2E),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF1A4D2E),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (featuredRewards.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (rewards.isEmpty()) "No rewards available yet." else "No active rewards available right now.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF71717A)
                    ),
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            featuredRewards.forEach { reward ->
                RewardPreviewCard(reward = reward)
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4D2E),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "Scan to Earn Points",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    containerColor: Color,
    iconContainerColor: Color,
    icon: ImageVector,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF18181B)
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF52525B),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun RewardPreviewCard(reward: RewardDto) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAF8))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF18181B)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reward.pointsCost} points",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF71717A)
                    )
                )
            }
        }
    }
}
