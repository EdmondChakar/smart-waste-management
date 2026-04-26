package com.example.smartwastemobile.feature.redemptions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ConfirmationNumber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun RedemptionsScreen(
    redemptions: List<RedemptionDto>,
    isLoadingRedemptions: Boolean,
    redemptionsErrorMessage: String?,
    onBack: () -> Unit,
    onRefreshRedemptions: () -> Unit
) {
    val backgroundColor = Color(0xFFFCFCFB)
    val primaryGreen = Color(0xFF1A4D2E)
    val mutedText = Color(0xFF667085)

    if (isLoadingRedemptions && redemptions.isEmpty()) {
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
                CircularRedemptionTopButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = onBack
                )

                Text(
                    text = "Redemptions",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF101828),
                        fontWeight = FontWeight.Bold
                    )
                )

                CircularRedemptionTopButton(
                    icon = Icons.Outlined.Refresh,
                    onClick = onRefreshRedemptions
                )
            }
        }

        if (!redemptionsErrorMessage.isNullOrBlank()) {
            item {
                Text(
                    text = redemptionsErrorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFB42318)
                    )
                )
            }
        }

        if (redemptions.isEmpty()) {
            item {
                Text(
                    text = "No redemption requests yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = mutedText
                    )
                )
            }
        } else {
            items(redemptions, key = { it.redemptionId }) { redemption ->
                RedemptionCard(redemption = redemption)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CircularRedemptionTopButton(
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
private fun RedemptionCard(redemption: RedemptionDto) {
    val statusStyle = statusStyleFor(redemption.statusCode)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = redemption.rewardTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF101828),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatPoints(redemption.pointsSpent)} PTS SPENT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF667085),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusStyle.backgroundColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = statusStyle.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = statusStyle.textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9FAFB),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    DetailRow(
                        label = "Requested",
                        value = formatRedemptionDate(redemption.requestedAt)
                    )

                    if (!redemption.fulfilledAt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailRow(
                            label = if (redemption.statusCode.equals("CANCELLED", ignoreCase = true)) {
                                "Rejected"
                            } else {
                                "Fulfilled"
                            },
                            value = formatRedemptionDate(redemption.fulfilledAt)
                        )
                    }

                    if (!redemption.voucherCode.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFEAECF0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = Color(0xFF98A2B3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voucher Code",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF667085)
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = redemption.voucherCode,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1A4D2E),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF667085)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF101828),
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private data class RedemptionStatusStyle(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color
)

private fun statusStyleFor(statusCode: String): RedemptionStatusStyle {
    return when (statusCode.uppercase(Locale.US)) {
        "FULFILLED" -> RedemptionStatusStyle(
            label = "Fulfilled",
            backgroundColor = Color(0xFFDDFBE6),
            textColor = Color(0xFF087443)
        )

        "CANCELLED" -> RedemptionStatusStyle(
            label = "Rejected",
            backgroundColor = Color(0xFFFEE4E2),
            textColor = Color(0xFFB42318)
        )

        else -> RedemptionStatusStyle(
            label = "Pending",
            backgroundColor = Color(0xFFFEF0C7),
            textColor = Color(0xFFB54708)
        )
    }
}

private fun formatPoints(value: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

private fun formatRedemptionDate(value: String): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

    return parseRedemptionDateTime(value)?.format(formatter) ?: value
}

private fun parseRedemptionDateTime(value: String): java.time.ZonedDateTime? {
    return try {
        OffsetDateTime.parse(value).toZonedDateTime()
    } catch (_: DateTimeParseException) {
        try {
            Instant.parse(value).atZone(ZoneId.systemDefault())
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(value).atZone(ZoneId.systemDefault())
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
