package com.example.smartwastemobile.feature.scan.data.remote

import com.example.smartwastemobile.feature.scan.data.model.ScanClaimRequest
import com.example.smartwastemobile.feature.scan.data.model.ScanClaimResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ScanApi {
    @POST("scan/claim")
    suspend fun claimScan(
        @Header("Authorization") authorization: String,
        @Body request: ScanClaimRequest
    ): ScanClaimResponse
}
