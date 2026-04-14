package com.example.smartwastemobile.feature.points.data.model

import com.google.gson.annotations.SerializedName

data class PointsBalanceDto(
    @SerializedName("current_points_balance")
    val currentPointsBalance: Int,
    @SerializedName("total_earned")
    val totalEarned: Int,
    @SerializedName("total_redeemed")
    val totalRedeemed: Int,
    @SerializedName("total_adjusted")
    val totalAdjusted: Int,
    @SerializedName("total_transactions")
    val totalTransactions: Int
)

data class PointsHistoryResponse(
    val count: Int,
    val transactions: List<PointsHistoryItemDto>
)

data class PointsHistoryItemDto(
    @SerializedName("txn_id")
    val txnId: Int,
    val type: String,
    val points: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("scan_id")
    val scanId: Int?,
    @SerializedName("bin_id")
    val binId: Int?,
    @SerializedName("bin_code")
    val binCode: String?,
    @SerializedName("item_count")
    val itemCount: Int?
)
