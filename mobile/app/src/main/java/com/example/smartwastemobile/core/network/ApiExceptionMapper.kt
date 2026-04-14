package com.example.smartwastemobile.core.network

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

object ApiExceptionMapper {
    fun toUserMessage(exception: Throwable): String {
        return when (exception) {
            is HttpException -> parseHttpError(exception)
            is IOException -> "Unable to reach the backend. Check ApiConfig.BASE_URL and make sure FastAPI is running."
            else -> exception.message ?: "Something went wrong."
        }
    }

    private fun parseHttpError(exception: HttpException): String {
        val errorBody = exception.response()?.errorBody()?.string()

        if (errorBody.isNullOrBlank()) {
            return "Request failed with status ${exception.code()}."
        }

        return try {
            val payload = JSONObject(errorBody)
            payload.optString("detail")
                .ifBlank { payload.optString("message") }
                .ifBlank { "Request failed with status ${exception.code()}." }
        } catch (_: Exception) {
            "Request failed with status ${exception.code()}."
        }
    }
}
