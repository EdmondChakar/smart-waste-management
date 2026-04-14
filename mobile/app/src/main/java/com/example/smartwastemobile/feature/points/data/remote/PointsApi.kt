package com.example.smartwastemobile.feature.points.data.remote

import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PointsApi {
    @GET("points/balance")
    suspend fun getPointsBalance(
        @Header("Authorization") authorization: String
    ): PointsBalanceDto

    @GET("points/history")
    suspend fun getPointsHistory(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 10
    ): PointsHistoryResponse
}
