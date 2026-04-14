package com.example.smartwastemobile.core.session

import android.content.Context
import com.example.smartwastemobile.feature.auth.data.model.UserDto

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences(
        SESSION_PREFERENCES,
        Context.MODE_PRIVATE
    )

    fun saveSession(accessToken: String, user: UserDto) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putInt(KEY_USER_ID, user.userId)
            .putString(KEY_EMAIL, user.email)
            .putInt(KEY_ROLE_ID, user.roleId)
            .putInt(KEY_STATUS_ID, user.statusId)
            .putString(KEY_CREATED_AT, user.createdAt)
            .apply()
    }

    fun saveUser(user: UserDto) {
        val accessToken = getAccessToken() ?: return
        saveSession(accessToken, user)
    }

    fun getAccessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun getStoredUser(): UserDto? {
        val accessToken = getAccessToken() ?: return null
        if (accessToken.isBlank()) {
            return null
        }

        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        val createdAt = preferences.getString(KEY_CREATED_AT, null) ?: return null

        return UserDto(
            userId = preferences.getInt(KEY_USER_ID, -1),
            email = email,
            roleId = preferences.getInt(KEY_ROLE_ID, 1),
            statusId = preferences.getInt(KEY_STATUS_ID, 1),
            createdAt = createdAt
        )
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val SESSION_PREFERENCES = "smart_waste_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE_ID = "role_id"
        const val KEY_STATUS_ID = "status_id"
        const val KEY_CREATED_AT = "created_at"
    }
}
