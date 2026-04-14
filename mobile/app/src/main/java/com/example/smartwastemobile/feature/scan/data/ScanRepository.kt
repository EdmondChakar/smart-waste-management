package com.example.smartwastemobile.feature.scan.data

import com.example.smartwastemobile.core.network.ApiExceptionMapper
import com.example.smartwastemobile.core.session.SessionManager
import com.example.smartwastemobile.feature.scan.data.model.ScanClaimRequest
import com.example.smartwastemobile.feature.scan.data.model.ScanClaimResponse
import com.example.smartwastemobile.feature.scan.data.remote.ScanApi

class ScanRepository(
    private val scanApi: ScanApi,
    private val sessionManager: SessionManager
) {
    suspend fun claimScan(qrRaw: String): ScanClaimResponse {
        val accessToken = sessionManager.getAccessToken()
            ?: throw IllegalStateException("You need to sign in first.")

        return try {
            scanApi.claimScan(
                authorization = "Bearer $accessToken",
                request = ScanClaimRequest(qrRaw = qrRaw)
            )
        } catch (exception: Exception) {
            throw IllegalStateException(ApiExceptionMapper.toUserMessage(exception))
        }
    }
}
