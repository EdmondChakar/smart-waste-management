package com.example.smartwastemobile.feature.auth.data

import com.example.smartwastemobile.core.network.ApiExceptionMapper
import com.example.smartwastemobile.core.session.SessionManager
import com.example.smartwastemobile.feature.auth.data.model.LoginRequest
import com.example.smartwastemobile.feature.auth.data.model.SignUpRequest
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import com.example.smartwastemobile.feature.auth.data.remote.AuthApi

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun signIn(email: String, password: String): UserDto {
        return runApiCall {
            val response = authApi.login(LoginRequest(email = email, password = password))
            sessionManager.saveSession(response.accessToken, response.user)
            response.user
        }
    }

    suspend fun signUp(email: String, password: String) {
        runApiCall {
            authApi.signUp(SignUpRequest(email = email, password = password))
            Unit
        }
    }

    suspend fun refreshCurrentUser(): UserDto {
        val accessToken = sessionManager.getAccessToken()
            ?: throw IllegalStateException("You need to sign in first.")

        return runApiCall {
            val user = authApi.getCurrentUser("Bearer $accessToken")
            sessionManager.saveUser(user)
            user
        }
    }

    fun getStoredUser(): UserDto? = sessionManager.getStoredUser()

    fun hasStoredSession(): Boolean = !sessionManager.getAccessToken().isNullOrBlank()

    fun signOut() {
        sessionManager.clearSession()
    }

    private suspend fun <T> runApiCall(block: suspend () -> T): T {
        return try {
            block()
        } catch (exception: Exception) {
            throw IllegalStateException(ApiExceptionMapper.toUserMessage(exception))
        }
    }
}
