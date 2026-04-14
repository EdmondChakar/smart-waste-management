package com.example.smartwastemobile.feature.points.data

import com.example.smartwastemobile.core.network.ApiExceptionMapper
import com.example.smartwastemobile.core.session.SessionManager
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import com.example.smartwastemobile.feature.points.data.remote.PointsApi

class PointsRepository(
    private val pointsApi: PointsApi,
    private val sessionManager: SessionManager
) {
    suspend fun getPointsBalance(): PointsBalanceDto {
        return runAuthorizedCall {
            pointsApi.getPointsBalance(authorization = it)
        }
    }

    suspend fun getPointsHistory(limit: Int = 10): List<PointsHistoryItemDto> {
        return runAuthorizedCall {
            pointsApi.getPointsHistory(
                authorization = it,
                limit = limit
            ).transactions
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
