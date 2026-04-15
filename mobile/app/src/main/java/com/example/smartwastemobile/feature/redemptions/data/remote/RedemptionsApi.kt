package com.example.smartwastemobile.feature.redemptions.data.remote

import com.example.smartwastemobile.feature.redemptions.data.model.RedeemRewardResponseDto
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionHistoryResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RedemptionsApi {
    @POST("rewards/{rewardId}/redeem")
    suspend fun redeemReward(
        @Header("Authorization") authorization: String,
        @Path("rewardId") rewardId: Int
    ): RedeemRewardResponseDto

    @GET("redemptions/me")
    suspend fun getMyRedemptions(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20
    ): RedemptionHistoryResponseDto
}
