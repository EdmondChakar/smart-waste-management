package com.example.smartwastemobile.feature.redemptions.data.model

import com.google.gson.annotations.SerializedName

data class RedemptionHistoryResponseDto(
    val count: Int,
    val redemptions: List<RedemptionDto>
)

data class RedeemRewardResponseDto(
    val message: String,
    val redemption: RedemptionDto,
    @SerializedName("current_points_balance")
    val currentPointsBalance: Int
)

data class RedemptionDto(
    @SerializedName("redemption_id")
    val redemptionId: Int,
    @SerializedName("reward_id")
    val rewardId: Int,
    @SerializedName("reward_title")
    val rewardTitle: String,
    @SerializedName("reward_description")
    val rewardDescription: String?,
    @SerializedName("points_spent")
    val pointsSpent: Int,
    @SerializedName("status_id")
    val statusId: Int,
    @SerializedName("status_code")
    val statusCode: String,
    @SerializedName("voucher_code")
    val voucherCode: String?,
    @SerializedName("requested_at")
    val requestedAt: String,
    @SerializedName("fulfilled_at")
    val fulfilledAt: String?
)
