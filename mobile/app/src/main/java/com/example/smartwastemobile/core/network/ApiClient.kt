package com.example.smartwastemobile.core.network

import com.example.smartwastemobile.feature.auth.data.remote.AuthApi
import com.example.smartwastemobile.feature.points.data.remote.PointsApi
import com.example.smartwastemobile.feature.rewards.data.remote.RewardsApi
import com.example.smartwastemobile.feature.scan.data.remote.ScanApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val rewardsApi: RewardsApi by lazy {
        retrofit.create(RewardsApi::class.java)
    }

    val pointsApi: PointsApi by lazy {
        retrofit.create(PointsApi::class.java)
    }

    val scanApi: ScanApi by lazy {
        retrofit.create(ScanApi::class.java)
    }
}
