package com.speedskateleague.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType

/** Android equivalent of LoggedOutHome in ContentView.swift. */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoggedIn: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SslSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("SPEED SKATE LEAGUE", style = SslType.title)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.xl))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = sslFieldColors(),
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.md))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = sslFieldColors(),
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.lg))

            if (state.errorMessage != null) {
                Text(state.errorMessage!!, color = SslColors.Urgent, style = SslType.body)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
            }

            SslPrimaryButton(
                text = "Sign In",
                loading = state.isLoading,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                onClick = { viewModel.signIn(onLoggedIn) },
            )
        }
    }
}

@Composable
private fun sslFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SslColors.GlassBorder,
    unfocusedBorderColor = SslColors.GlassBorder,
    focusedLabelColor = SslColors.TextSecondary,
    unfocusedLabelColor = SslColors.TextSecondary,
    cursorColor = SslColors.Blue,
)
