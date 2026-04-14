package com.example.smartwastemobile.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwastemobile.feature.auth.data.AuthRepository
import com.example.smartwastemobile.feature.auth.data.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val currentUser: UserDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password are required.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            try {
                val user = authRepository.signIn(email = email, password = password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        errorMessage = null,
                        successMessage = null
                    )
                }
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password are required.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            try {
                authRepository.signUp(email = email, password = password)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        successMessage = "Account created successfully. Please sign in."
                    )
                }
                onSuccess()
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }

    fun refreshProfile() {
        if (!authRepository.hasStoredSession()) {
            _uiState.update {
                it.copy(currentUser = null, errorMessage = "No saved session found.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                val user = authRepository.refreshCurrentUser()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        errorMessage = null
                    )
                }
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState(isInitializing = false)
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }

    private fun restoreSession() {
        val currentUser = if (authRepository.hasStoredSession()) {
            authRepository.getStoredUser()
        } else {
            null
        }

        _uiState.value = AuthUiState(
            isInitializing = false,
            currentUser = currentUser
        )
    }
}
