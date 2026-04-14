package com.example.smartwastemobile.feature.rewards.data.model

import com.google.gson.annotations.SerializedName

data class RewardsResponse(
    val count: Int,
    val rewards: List<RewardDto>
)

data class RewardDto(
    @SerializedName("reward_id")
    val rewardId: Int,
    val title: String,
    val description: String?,
    @SerializedName("points_cost")
    val pointsCost: Int,
    @SerializedName("is_active")
    val isActive: Boolean
)
