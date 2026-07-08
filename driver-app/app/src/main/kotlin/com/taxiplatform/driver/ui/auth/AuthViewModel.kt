package com.taxiplatform.driver.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiplatform.driver.domain.usecase.LoginUseCase
import com.taxiplatform.driver.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
	val isLoading: Boolean = false,
	val error: String? = null,
	val isAuthenticated: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
	private val loginUseCase: LoginUseCase,
	private val registerUseCase: RegisterUseCase,
) : ViewModel() {

	private val _uiState = MutableStateFlow(AuthUiState())
	val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

	fun login(email: String, password: String) {
		_uiState.value = AuthUiState(isLoading = true)
		viewModelScope.launch {
			loginUseCase(email, password)
				.onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
				.onFailure { _uiState.value = AuthUiState(error = it.message ?: "Login failed") }
		}
	}

	fun register(email: String, password: String, fullName: String, phone: String?) {
		_uiState.value = AuthUiState(isLoading = true)
		viewModelScope.launch {
			registerUseCase(email, password, fullName, phone)
				.onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
				.onFailure { _uiState.value = AuthUiState(error = it.message ?: "Registration failed") }
		}
	}
}
