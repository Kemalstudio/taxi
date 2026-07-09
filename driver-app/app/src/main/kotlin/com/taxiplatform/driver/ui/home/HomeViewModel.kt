package com.taxiplatform.driver.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiplatform.driver.data.location.LocationTracker
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.repository.AuthRepository
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import com.taxiplatform.driver.domain.usecase.ToggleOnlineUseCase
import com.taxiplatform.driver.domain.usecase.UpdateLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
	val isOnline: Boolean = false,
	val isLoading: Boolean = false,
	val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
	private val authRepository: AuthRepository,
	private val toggleOnlineUseCase: ToggleOnlineUseCase,
	private val updateLocationUseCase: UpdateLocationUseCase,
	private val rideEventsRepository: RideEventsRepository,
	private val locationTracker: LocationTracker,
) : ViewModel() {

	private val _uiState = MutableStateFlow(HomeUiState())
	val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

	val currentOffer: StateFlow<RideOffer?> = rideEventsRepository.currentOffer

	private var locationJob: Job? = null

	fun setOnline(online: Boolean) {
		_uiState.value = _uiState.value.copy(isLoading = true, error = null)
		viewModelScope.launch {
			val session = authRepository.currentSession()
			if (session == null) {
				_uiState.value = HomeUiState(error = "Not logged in")
				return@launch
			}
			toggleOnlineUseCase(session, online)
				.onSuccess {
					_uiState.value = HomeUiState(isOnline = online)
					if (online) startLocationUpdates() else stopLocationUpdates()
				}
				.onFailure {
					_uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Failed to update status")
				}
		}
	}

	private fun startLocationUpdates() {
		locationJob?.cancel()
		locationJob = viewModelScope.launch {
			locationTracker.locationUpdates().collect { point -> updateLocationUseCase(point) }
		}
	}

	private fun stopLocationUpdates() {
		locationJob?.cancel()
		locationJob = null
	}

	override fun onCleared() {
		stopLocationUpdates()
	}
}
