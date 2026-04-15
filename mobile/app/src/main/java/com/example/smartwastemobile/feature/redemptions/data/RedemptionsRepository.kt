package com.example.smartwastemobile.feature.redemptions.data

import com.example.smartwastemobile.core.network.ApiExceptionMapper
import com.example.smartwastemobile.core.session.SessionManager
import com.example.smartwastemobile.feature.redemptions.data.model.RedeemRewardResponseDto
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto
import com.example.smartwastemobile.feature.redemptions.data.remote.RedemptionsApi

class RedemptionsRepository(
    private val redemptionsApi: RedemptionsApi,
    private val sessionManager: SessionManager
) {
    suspend fun redeemReward(rewardId: Int): RedeemRewardResponseDto {
        return runAuthorizedCall {
            redemptionsApi.redeemReward(
                authorization = it,
                rewardId = rewardId
            )
        }
    }

    suspend fun getMyRedemptions(limit: Int = 20): List<RedemptionDto> {
        return runAuthorizedCall {
            redemptionsApi.getMyRedemptions(
                authorization = it,
                limit = limit
            ).redemptions
        }
    }

    private suspend fun <T> runAuthorizedCall(block: suspend (String) -> T): T {
        val accessToken = sessionManager.getAccessToken()
            ?: throw IllegalStateException("You need to sign in first.")

        return try {
            block("Bearer $accessToken")
        } catch (exception: Exception) {
            throw IllegalStateException(ApiExceptionMapper.toUserMessage(exception))
        }
    }
}
