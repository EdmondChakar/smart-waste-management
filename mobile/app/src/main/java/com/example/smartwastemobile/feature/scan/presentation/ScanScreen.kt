package com.example.smartwastemobile.feature.scan.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.core.ui.components.AppPrimaryButton
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Scan QR",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan the QR code shown on the smart bin OLED screen to claim points.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppPrimaryButton(
            text = if (isSubmittingScan) "Submitting..." else "Start QR Scan",
            enabled = !isSubmittingScan,
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
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when {
                    isSubmittingScan -> {
                        Text(
                            text = "Submitting scanned QR code...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    lastScanResult != null && lastScanResult.isValid -> {
                        Text(
                            text = "Scan successful",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Bin: ${lastScanResult.binCode}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Items counted: ${lastScanResult.itemCount}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Points awarded: ${lastScanResult.pointsAwarded}")
                    }

                    lastScanResult != null && !lastScanResult.isValid -> {
                        Text(
                            text = "Scan rejected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastScanResult.invalidReason ?: "The QR code could not be claimed.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    !scanErrorMessage.isNullOrBlank() -> {
                        Text(
                            text = "Scan failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanErrorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {
                        Text(
                            text = "No scan yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a scan and the app will submit the QR payload to the backend.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
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
