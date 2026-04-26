package com.example.smartwastemobile.feature.points.presentation

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun PointsSummaryScreen(
    pointsBalance: PointsBalanceDto?,
    pointsHistory: List<PointsHistoryItemDto>,
    isLoadingPoints: Boolean,
    pointsErrorMessage: String?,
    onBack: () -> Unit,
    onRefreshPoints: () -> Unit
) {
    val backgroundColor = Color(0xFFFCFCFB)
    val primaryGreen = Color(0xFF1A4D2E)
    val mutedText = Color(0xFF667085)

    if (isLoadingPoints && pointsBalance == null && pointsHistory.isEmpty()) {
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularTopIconButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = onBack
                )

                Text(
                    text = "Points Summary",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF101828),
                        fontWeight = FontWeight.Bold
                    )
                )

                CircularTopIconButton(
                    icon = Icons.Outlined.Refresh,
                    onClick = onRefreshPoints
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = primaryGreen,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatPoints(pointsBalance?.currentPointsBalance ?: 0),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "pts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFD8F3DC),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.18f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMetric(
                            label = "Total Earned",
                            value = formatPoints(pointsBalance?.totalEarned ?: 0)
                        )
                        SummaryMetric(
                            label = "Total Redeemed",
                            value = formatPoints(pointsBalance?.totalRedeemed ?: 0)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF101828),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = buildString {
                        val total = pointsBalance?.totalTransactions ?: pointsHistory.size
                        append(total)
                        append(if (total == 1) " transaction" else " transactions")
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = mutedText
                    )
                )
            }
        }

        if (!pointsErrorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = pointsErrorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFB42318)
                    )
                )
            }
        }

        if (pointsHistory.isEmpty()) {
            item {
                Text(
                    text = "No recent activity yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = mutedText
                    )
                )
            }
        } else {
            itemsIndexed(pointsHistory, key = { _, item -> item.txnId }) { index, item ->
                PointsHistoryCard(item = item)

                if (index != pointsHistory.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 2.dp),
                        color = Color(0xFFF2F4F7),
                        thickness = 1.dp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CircularTopIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF344054),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.88f)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun PointsHistoryCard(item: PointsHistoryItemDto) {
    val isRedeem = item.type.equals("REDEEM", ignoreCase = true)
    val iconBackground = if (isRedeem) Color(0xFFFFF6E8) else Color(0xFFEAF8EE)
    val iconTint = if (isRedeem) Color(0xFFF59E0B) else Color(0xFF12B76A)
    val amountColor = if (item.points >= 0) Color(0xFF12B76A) else Color(0xFF101828)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowOutward,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (isRedeem) 0f else 135f)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildPointsTitle(item),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF101828),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatHistoryDate(item.createdAt),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF98A2B3)
                )
            )
        }

        Text(
            text = formatSignedPoints(item.points),
            style = MaterialTheme.typography.titleMedium.copy(
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

private fun buildPointsTitle(item: PointsHistoryItemDto): String {
    return when {
        item.type.equals("REDEEM", ignoreCase = true) -> "Redeemed Reward"
        item.binCode != null && item.itemCount != null -> "Recycled ${item.itemCount} item(s) (${item.binCode})"
        item.binCode != null -> "Recycled at ${item.binCode}"
        item.type.equals("ADJUST", ignoreCase = true) -> "Adjusted Points"
        else -> item.type.replace('_', ' ').lowercase()
            .replaceFirstChar { char -> char.uppercase() }
    }
}

private fun formatSignedPoints(value: Int): String {
    return if (value > 0) {
        "+${formatPoints(value)}"
    } else {
        "-${formatPoints(kotlin.math.abs(value))}"
    }
}

private fun formatPoints(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

private fun formatHistoryDate(value: String): String {
    return try {
        val parsed = OffsetDateTime.parse(value)
        parsed.format(DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US))
    } catch (_: DateTimeParseException) {
        value
    }
}
