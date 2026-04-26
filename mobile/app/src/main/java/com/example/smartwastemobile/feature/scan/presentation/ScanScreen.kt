package com.example.smartwastemobile.feature.scan.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.scan.data.model.ScanResultDto
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun ScanScreen(
    isSubmittingScan: Boolean,
    scanErrorMessage: String?,
    lastScanResult: ScanResultDto?,
    onSubmitScan: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onScanCancelled: () -> Unit,
    onScannerError: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scanner = remember(activity) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(activity, options)
    }

    val backgroundColor = Color(0xFFFCFCFB)
    val primaryGreen = Color(0xFF1A4D2E)
    val secondaryTextColor = Color(0xFF71717A)
    val stateCard = buildScanStateCard(
        isSubmittingScan = isSubmittingScan,
        scanErrorMessage = scanErrorMessage,
        lastScanResult = lastScanResult
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.size(168.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFFF8FCF8),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = Color(0xFFEAF8EE),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CropFree,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Scan Smart Bin",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF18181B),
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Find the QR code on the Smart Bin OLED screen after depositing your items.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = secondaryTextColor
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.82f)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                onClearFeedback()

                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val rawValue = barcode.rawValue.orEmpty()
                        onSubmitScan(rawValue)
                    }
                    .addOnCanceledListener {
                        onScanCancelled()
                    }
                    .addOnFailureListener { exception ->
                        onScannerError(
                            exception.message ?: "The QR scanner could not be opened."
                        )
                    }
            },
            enabled = !isSubmittingScan,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryGreen,
                contentColor = Color.White,
                disabledContainerColor = primaryGreen.copy(alpha = 0.65f),
                disabledContentColor = Color.White.copy(alpha = 0.92f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text(
                text = if (isSubmittingScan) "Submitting..." else "Start Camera",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        if (stateCard != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ScanStateCard(state = stateCard)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class ScanStateUi(
    val title: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconContainerColor: Color,
    val titleColor: Color,
    val bodyColor: Color,
    val details: List<String> = emptyList()
)

@Composable
private fun ScanStateCard(state: ScanStateUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(state.iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = state.icon,
                    contentDescription = null,
                    tint = state.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = state.titleColor,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = state.bodyColor
                ),
                textAlign = TextAlign.Center
            )

            if (state.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                state.details.forEach { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF3F3F46),
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun buildScanStateCard(
    isSubmittingScan: Boolean,
    scanErrorMessage: String?,
    lastScanResult: ScanResultDto?
): ScanStateUi? {
    return when {
        isSubmittingScan -> {
            ScanStateUi(
                title = "Submitting...",
                body = "Submitting scanned QR code to the backend.",
                icon = Icons.Outlined.HourglassTop,
                iconColor = Color(0xFF1A4D2E),
                iconContainerColor = Color(0xFFEAF8EE),
                titleColor = Color(0xFF18181B),
                bodyColor = Color(0xFF71717A)
            )
        }

        lastScanResult != null && lastScanResult.isValid -> {
            ScanStateUi(
                title = "Scan Successful",
                body = "Your scan was accepted and points were added to your account.",
                icon = Icons.Outlined.CheckCircle,
                iconColor = Color(0xFF15803D),
                iconContainerColor = Color(0xFFDCFCE7),
                titleColor = Color(0xFF166534),
                bodyColor = Color(0xFF3F3F46),
                details = listOf(
                    "Bin: ${lastScanResult.binCode}",
                    "Items counted: ${lastScanResult.itemCount}",
                    "Points awarded: ${lastScanResult.pointsAwarded}"
                )
            )
        }

        lastScanResult != null && !lastScanResult.isValid -> {
            ScanStateUi(
                title = "Scan Rejected",
                body = lastScanResult.invalidReason ?: "The QR code could not be claimed.",
                icon = Icons.Outlined.ErrorOutline,
                iconColor = Color(0xFFD97706),
                iconContainerColor = Color(0xFFFEF3C7),
                titleColor = Color(0xFFB45309),
                bodyColor = Color(0xFF92400E)
            )
        }

        !scanErrorMessage.isNullOrBlank() -> {
            ScanStateUi(
                title = "Scan Failed",
                body = scanErrorMessage,
                icon = Icons.Outlined.Close,
                iconColor = Color(0xFFB91C1C),
                iconContainerColor = Color(0xFFFEE2E2),
                titleColor = Color(0xFFB91C1C),
                bodyColor = Color(0xFF991B1B)
            )
        }

        else -> null
    }
}

private fun Context.findActivity(): Activity {
    var currentContext = this

    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }

        currentContext = currentContext.baseContext
    }

    error("Unable to find Activity from context")
}
