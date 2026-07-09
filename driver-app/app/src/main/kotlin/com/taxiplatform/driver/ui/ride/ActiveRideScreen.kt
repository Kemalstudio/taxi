package com.taxiplatform.driver.ui.ride

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxiplatform.driver.domain.model.RideStatus

@Composable
fun ActiveRideScreen(
	onCompleted: () -> Unit,
	viewModel: RideViewModel = hiltViewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(uiState.isCompleted) {
		if (uiState.isCompleted) onCompleted()
	}

	val ride = uiState.ride
	if (ride == null) {
		Column(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			CircularProgressIndicator()
		}
		return
	}

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
	) {
		Text(text = "Ride ${ride.status.name}", style = MaterialTheme.typography.headlineMedium)
		Spacer(modifier = Modifier.height(8.dp))
		Text(text = "Pickup: ${ride.pickup.lat}, ${ride.pickup.lng}")
		Spacer(modifier = Modifier.height(4.dp))
		Text(text = "Dropoff: ${ride.dropoff.lat}, ${ride.dropoff.lng}")

		uiState.error?.let { message ->
			Spacer(modifier = Modifier.height(16.dp))
			Text(text = message, color = MaterialTheme.colorScheme.error)
		}

		Spacer(modifier = Modifier.height(32.dp))
		val actionLabel = when (ride.status) {
			RideStatus.ACCEPTED -> "Arrived at pickup"
			RideStatus.DRIVER_ARRIVED -> "Start ride"
			RideStatus.IN_PROGRESS -> "Complete ride"
			else -> null
		}
		if (actionLabel != null) {
			Button(
				onClick = viewModel::advance,
				enabled = !uiState.isLoading,
				modifier = Modifier.fillMaxWidth(),
			) {
				if (uiState.isLoading) {
					CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
				} else {
					Text(actionLabel)
				}
			}
		}
	}
}
