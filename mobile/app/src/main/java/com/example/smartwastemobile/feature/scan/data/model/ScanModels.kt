package com.example.smartwastemobile.feature.scan.data.model

import com.google.gson.annotations.SerializedName

data class ScanClaimRequest(
    @SerializedName("qr_raw")
    val qrRaw: String
)

data class ScanClaimResponse(
    val message: String,
    val scan: ScanResultDto,
    @SerializedName("current_points_balance")
    val currentPointsBalance: Int
)

data class ScanResultDto(
    @SerializedName("scan_id")
    val scanId: Int,
    @SerializedName("bin_id")
    val binId: Int,
    @SerializedName("bin_code")
    val binCode: String,
    @SerializedName("item_count")
    val itemCount: Int,
    @SerializedName("points_awarded")
    val pointsAwarded: Int,
    @SerializedName("scan_at")
    val scanAt: String,
    @SerializedName("is_valid")
    val isValid: Boolean,
    @SerializedName("invalid_reason")
    val invalidReason: String?
)
