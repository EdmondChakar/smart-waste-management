package com.example.smartwastemobile.feature.rewards.data

import com.example.smartwastemobile.core.network.ApiExceptionMapper
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import com.example.smartwastemobile.feature.rewards.data.remote.RewardsApi

class RewardsRepository(
    private val rewardsApi: RewardsApi
) {
    suspend fun getRewards(): List<RewardDto> {
        return try {
            rewardsApi.getRewards().rewards
        } catch (exception: Exception) {
            throw IllegalStateException(ApiExceptionMapper.toUserMessage(exception))
        }
    }
}
