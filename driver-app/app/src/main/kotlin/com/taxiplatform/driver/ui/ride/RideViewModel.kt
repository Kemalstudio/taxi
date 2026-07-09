package com.taxiplatform.driver.ui.ride

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideStatus
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import com.taxiplatform.driver.domain.repository.RideRepository
import com.taxiplatform.driver.domain.usecase.AdvanceRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideUiState(
	val ride: Ride? = null,
	val isLoading: Boolean = false,
	val error: String? = null,
	val isCompleted: Boolean = false,
)

@HiltViewModel
class RideViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val rideRepository: RideRepository,
	private val rideEventsRepository: RideEventsRepository,
	private val advanceRideUseCase: AdvanceRideUseCase,
) : ViewModel() {

	private val rideId: String = checkNotNull(savedStateHandle["rideId"])

	private val _uiState = MutableStateFlow(RideUiState(isLoading = true))
	val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

	init {
		viewModelScope.launch {
			rideRepository.getRide(rideId)
				.onSuccess { _uiState.value = RideUiState(ride = it) }
				.onFailure { _uiState.value = RideUiState(error = it.message ?: "Failed to load ride") }
		}
		viewModelScope.launch {
			rideEventsRepository.observeRideStatus(rideId).collect { update ->
				val current = _uiState.value.ride ?: return@collect
				_uiState.value = _uiState.value.copy(ride = current.copy(status = update.status))
			}
		}
	}

	fun advance() {
		val ride = _uiState.value.ride ?: return
		_uiState.value = _uiState.value.copy(isLoading = true, error = null)
		viewModelScope.launch {
			advanceRideUseCase(ride)
				.onSuccess { updated ->
					_uiState.value = RideUiState(ride = updated, isCompleted = updated.status == RideStatus.COMPLETED)
				}
				.onFailure {
					_uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "Action failed")
				}
		}
	}
}
