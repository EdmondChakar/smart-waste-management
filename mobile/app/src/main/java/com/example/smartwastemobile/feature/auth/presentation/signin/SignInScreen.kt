package com.example.smartwastemobile.feature.auth.presentation.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartwastemobile.core.ui.components.AppPrimaryButton
import com.example.smartwastemobile.core.ui.components.AppTextField

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppTextField(
            value = email,
            onValueChange = {
                email = it
                onMessageShown()
            },
            label = "Email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = password,
            onValueChange = {
                password = it
                onMessageShown()
            },
            label = "Password",
            isPassword = true
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!successMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppPrimaryButton(
            text = if (isLoading) "Signing In..." else "Sign In",
            enabled = !isLoading,
            onClick = {
                onSignIn(email.trim(), password)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onNavigateToSignUp,
            enabled = !isLoading
        ) {
            Text(text = "Create a user account")
        }
    }
}
