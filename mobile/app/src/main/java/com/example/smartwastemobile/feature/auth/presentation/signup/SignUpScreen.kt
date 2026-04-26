package com.example.smartwastemobile.feature.auth.presentation.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.feature.auth.presentation.components.AuthField
import com.example.smartwastemobile.feature.auth.presentation.components.AuthFooterLink
import com.example.smartwastemobile.feature.auth.presentation.components.AuthMessageBanner
import com.example.smartwastemobile.feature.auth.presentation.components.AuthPrimaryActionButton
import com.example.smartwastemobile.feature.auth.presentation.components.AuthScreenContainer

@Composable
fun SignUpScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignUp: (String, String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onMessageShown: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AuthScreenContainer(
        title = "Join Us",
        subtitle = "Start earning points for recycling"
    ) {
        AuthField(
            value = email,
            onValueChange = {
                email = it
                validationMessage = null
                onMessageShown()
            },
            label = "Email Address",
            placeholder = "name@example.com",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthField(
            value = password,
            onValueChange = {
                password = it
                validationMessage = null
                onMessageShown()
            },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                validationMessage = null
                onMessageShown()
            },
            label = "Confirm Password",
            placeholder = "••••••••",
            isPassword = true
        )

        val messageToShow = validationMessage ?: errorMessage
        if (!messageToShow.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            AuthMessageBanner(
                text = messageToShow,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFB91C1C)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        AuthPrimaryActionButton(
            text = if (isLoading) "Creating Account..." else "Create Account",
            enabled = !isLoading,
            onClick = {
                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        validationMessage = "All fields are required."
                    }

                    password != confirmPassword -> {
                        validationMessage = "Passwords do not match."
                    }

                    else -> {
                        validationMessage = null
                        onSignUp(email.trim(), password)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        AuthFooterLink(
            leadingText = "Already have an account? ",
            actionText = "Sign In",
            enabled = !isLoading,
            onClick = onNavigateToSignIn
        )
    }
}
