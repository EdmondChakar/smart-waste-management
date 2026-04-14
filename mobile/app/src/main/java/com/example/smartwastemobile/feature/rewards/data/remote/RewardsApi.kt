package com.example.smartwastemobile.feature.rewards.data.remote

import com.example.smartwastemobile.feature.rewards.data.model.RewardsResponse
import retrofit2.http.GET

interface RewardsApi {
    @GET("rewards")
    suspend fun getRewards(): RewardsResponse
}
