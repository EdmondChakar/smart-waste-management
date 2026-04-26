package com.example.smartwastemobile.feature.account.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onOpenPointsSummary: () -> Unit,
    onOpenRedemptions: () -> Unit,
    onRefreshProfile: () -> Unit,
    onRefreshPoints: () -> Unit,
    onRefreshRedemptions: () -> Unit,
    onSignOut: () -> Unit
) {
    val backgroundColor = Color(0xFFFCFCFB)
    val primaryGreen = Color(0xFF1A4D2E)
    val displayName = user?.email
        ?.substringBefore("@")
        ?.replaceFirstChar { char -> char.uppercase() }
        ?.ifBlank { "User" }
        ?: "Guest"

    if (isLoading && user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryGreen, strokeWidth = 3.dp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = primaryGreen,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user?.email ?: "No signed-in user found.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        )
                    }
                }
            }
        }

        item {
            AccountSectionLabel(text = "ACTIVITY & HISTORY")
        }

        item {
            AccountMenuCard(
                icon = Icons.Outlined.History,
                iconContainerColor = Color(0xFFEAF8EE),
                iconTint = primaryGreen,
                title = "Points Summary",
                subtitle = "View your earning history",
                onClick = onOpenPointsSummary
            )
        }

        item {
            AccountMenuCard(
                icon = Icons.Outlined.CardGiftcard,
                iconContainerColor = Color(0xFFFFF1E8),
                iconTint = Color(0xFFF59E0B),
                title = "Redemptions",
                subtitle = "Track your claimed rewards",
                onClick = onOpenRedemptions
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                AccountInfoMessage(
                    message = errorMessage,
                    backgroundColor = Color(0xFFFEE2E2),
                    textColor = Color(0xFFB42318),
                    icon = Icons.Outlined.ErrorOutline
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onRefreshProfile,
                enabled = !isLoading && user != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF475467),
                    disabledContentColor = Color(0xFF98A2B3)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading && user != null)
            ) {
                Text(
                    text = if (isLoading) "Refreshing..." else "Refresh Profile",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF3B30),
                    disabledContentColor = Color(0xFFF5A5A0)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFD2CC))
                )
            ) {
                Text(
                    text = "Sign Out",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AccountSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = Color(0xFF98A2B3),
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun AccountMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color(0xFF101828),
    subtitleColor: Color = Color(0xFF667085),
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = onClick != null) {
                    onClick?.invoke()
                }
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = titleColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = subtitleColor
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD0D5DD),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AccountInfoMessage(
    message: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
