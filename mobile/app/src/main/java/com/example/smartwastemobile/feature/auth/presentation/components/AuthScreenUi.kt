package com.example.smartwastemobile.feature.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val AuthBackgroundColor = Color(0xFFFCFCFB)
internal val AuthPrimaryGreen = Color(0xFF1A4D2E)
internal val AuthHeadingColor = Color(0xFF18181B)
internal val AuthSecondaryTextColor = Color(0xFF71717A)
internal val AuthInputBorderColor = Color(0xFFE7E7EB)
internal val AuthFieldLabelColor = Color(0xFF27272A)
internal val AuthTextColor = Color(0xFF18181B)
internal val AuthPlaceholderColor = Color(0xFFA1A1AA)

@Composable
fun AuthScreenContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackgroundColor)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthLogoBadge()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = AuthHeadingColor
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = AuthSecondaryTextColor
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

            content()
        }
    }
}

@Composable
fun AuthLogoBadge() {
    Surface(
        modifier = Modifier.size(68.dp),
        shape = RoundedCornerShape(22.dp),
        color = AuthPrimaryGreen,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = "Eco logo",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = AuthFieldLabelColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = AuthPlaceholderColor
                )
            },
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = AuthInputBorderColor,
                unfocusedBorderColor = AuthInputBorderColor,
                focusedTextColor = AuthTextColor,
                unfocusedTextColor = AuthTextColor,
                cursorColor = AuthPrimaryGreen
            )
        )
    }
}

@Composable
fun AuthMessageBanner(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun AuthPrimaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimaryGreen,
            contentColor = Color.White,
            disabledContainerColor = AuthPrimaryGreen.copy(alpha = 0.65f),
            disabledContentColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}

@Composable
fun AuthFooterLink(
    leadingText: String,
    actionText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leadingText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthSecondaryTextColor
            )
        )
        TextButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = AuthPrimaryGreen
                )
            )
        }
    }
}
