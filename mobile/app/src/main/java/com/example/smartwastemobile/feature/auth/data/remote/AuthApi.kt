package com.example.smartwastemobile.feature.auth.data.remote

import com.example.smartwastemobile.feature.auth.data.model.LoginRequest
import com.example.smartwastemobile.feature.auth.data.model.LoginResponse
import com.example.smartwastemobile.feature.auth.data.model.SignUpRequest
import com.example.smartwastemobile.feature.auth.data.model.UserCreateResponse
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users")
    suspend fun signUp(@Body request: SignUpRequest): UserCreateResponse

    @GET("me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): UserDto
}
