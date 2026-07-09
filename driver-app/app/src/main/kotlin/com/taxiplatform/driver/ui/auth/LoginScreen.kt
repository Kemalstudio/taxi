package com.taxiplatform.driver.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
	onLoggedIn: () -> Unit,
	onNavigateToRegister: () -> Unit,
	viewModel: AuthViewModel = hiltViewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }

	LaunchedEffect(uiState.isAuthenticated) {
		if (uiState.isAuthenticated) onLoggedIn()
	}

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
	) {
		Text(text = "Taxi Driver", style = MaterialTheme.typography.headlineMedium)
		Spacer(modifier = Modifier.height(32.dp))

		OutlinedTextField(
			value = email,
			onValueChange = { email = it },
			label = { Text("Email") },
			singleLine = true,
			modifier = Modifier.fillMaxWidth(),
		)
		Spacer(modifier = Modifier.height(16.dp))
		OutlinedTextField(
			value = password,
			onValueChange = { password = it },
			label = { Text("Password") },
			singleLine = true,
			visualTransformation = PasswordVisualTransformation(),
			modifier = Modifier.fillMaxWidth(),
		)

		uiState.error?.let { message ->
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = message, color = MaterialTheme.colorScheme.error)
		}

		Spacer(modifier = Modifier.height(24.dp))
		Button(
			onClick = { viewModel.login(email, password) },
			enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
			modifier = Modifier.fillMaxWidth(),
		) {
			if (uiState.isLoading) {
				CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
			} else {
				Text("Log in")
			}
		}

		TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
			Text("Don't have an account? Register")
		}
	}
}
