package com.example.smartwastemobile.feature.auth.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    val user: UserDto
)

data class UserCreateResponse(
    val message: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("user_id")
    val userId: Int,
    val email: String,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("status_id")
    val statusId: Int,
    @SerializedName("created_at")
    val createdAt: String
)
