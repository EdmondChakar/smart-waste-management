package com.example.smartwastemobile.feature.auth.presentation.signin

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
fun SignInScreen(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSignIn: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onMessageShown: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScreenContainer(
        title = "Welcome Back",
        subtitle = "Sign in to manage your eco-points"
    ) {
        AuthField(
            value = email,
            onValueChange = {
                email = it
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
                onMessageShown()
            },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            AuthMessageBanner(
                text = errorMessage,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFB91C1C)
            )
        }

        if (!successMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            AuthMessageBanner(
                text = successMessage,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF166534)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        AuthPrimaryActionButton(
            text = if (isLoading) "Signing In..." else "Sign In",
            enabled = !isLoading,
            onClick = {
                onSignIn(email.trim(), password)
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        AuthFooterLink(
            leadingText = "Don't have an account? ",
            actionText = "Create account",
            enabled = !isLoading,
            onClick = onNavigateToSignUp
        )
    }
}
