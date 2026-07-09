package com.taxiplatform.driver.ui.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import com.taxiplatform.driver.domain.usecase.AcceptRideUseCase
import com.taxiplatform.driver.domain.usecase.RejectRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Matches taxi.dispatch.offer-timeout-seconds on the backend. */
private const val OFFER_TIMEOUT_SECONDS = 15

data class OfferUiState(
	val offer: RideOffer? = null,
	val secondsRemaining: Int = OFFER_TIMEOUT_SECONDS,
	val isResponding: Boolean = false,
	val error: String? = null,
	val acceptedRide: Ride? = null,
	val dismissed: Boolean = false,
)

@HiltViewModel
class OfferViewModel @Inject constructor(
	private val rideEventsRepository: RideEventsRepository,
	private val acceptRideUseCase: AcceptRideUseCase,
	private val rejectRideUseCase: RejectRideUseCase,
) : ViewModel() {

	private val _uiState = MutableStateFlow(OfferUiState(offer = rideEventsRepository.currentOffer.value))
	val uiState: StateFlow<OfferUiState> = _uiState.asStateFlow()

	init {
		viewModelScope.launch {
			for (remaining in OFFER_TIMEOUT_SECONDS downTo 0) {
				_uiState.value = _uiState.value.copy(secondsRemaining = remaining)
				if (remaining == 0) {
					// Window elapsed; the backend's own timeout sweep reassigns the ride, we just
					// stop showing this offer.
					rideEventsRepository.clearOffer()
					_uiState.value = _uiState.value.copy(dismissed = true)
					return@launch
				}
				delay(1000)
			}
		}
	}

	fun accept() {
		val rideId = _uiState.value.offer?.rideId ?: return
		_uiState.value = _uiState.value.copy(isResponding = true)
		viewModelScope.launch {
			acceptRideUseCase(rideId)
				.onSuccess {
					rideEventsRepository.clearOffer()
					_uiState.value = _uiState.value.copy(isResponding = false, acceptedRide = it)
				}
				.onFailure {
					rideEventsRepository.clearOffer()
					_uiState.value = _uiState.value.copy(isResponding = false, error = it.message, dismissed = true)
				}
		}
	}

	fun reject() {
		val rideId = _uiState.value.offer?.rideId ?: return
		_uiState.value = _uiState.value.copy(isResponding = true)
		viewModelScope.launch {
			rejectRideUseCase(rideId)
			rideEventsRepository.clearOffer()
			_uiState.value = _uiState.value.copy(isResponding = false, dismissed = true)
		}
	}
}
