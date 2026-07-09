package com.taxiplatform.driver.ui.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxiplatform.driver.domain.model.Ride

@Composable
fun IncomingOfferScreen(
	onAccepted: (Ride) -> Unit,
	onDismissed: () -> Unit,
	viewModel: OfferViewModel = hiltViewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(uiState.acceptedRide) {
		uiState.acceptedRide?.let(onAccepted)
	}
	LaunchedEffect(uiState.dismissed) {
		if (uiState.dismissed && uiState.acceptedRide == null) onDismissed()
	}

	val offer = uiState.offer
	if (offer == null) {
		LaunchedEffect(Unit) { onDismissed() }
		return
	}

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
	) {
		Text(text = "New ride request", style = MaterialTheme.typography.headlineMedium)
		Spacer(modifier = Modifier.height(8.dp))
		Text(text = "Responds in ${uiState.secondsRemaining}s")
		Spacer(modifier = Modifier.height(24.dp))
		Text(text = "Pickup: ${offer.pickup.lat}, ${offer.pickup.lng}")
		Spacer(modifier = Modifier.height(8.dp))
		Text(text = "Dropoff: ${offer.dropoff.lat}, ${offer.dropoff.lng}")

		uiState.error?.let { message ->
			Spacer(modifier = Modifier.height(16.dp))
			Text(text = message, color = MaterialTheme.colorScheme.error)
		}

		Spacer(modifier = Modifier.height(32.dp))
		Row(modifier = Modifier.fillMaxWidth()) {
			OutlinedButton(
				onClick = viewModel::reject,
				enabled = !uiState.isResponding,
				modifier = Modifier.weight(1f),
			) {
				Text("Reject")
			}
			Spacer(modifier = Modifier.width(12.dp))
			Button(
				onClick = viewModel::accept,
				enabled = !uiState.isResponding,
				modifier = Modifier.weight(1f),
			) {
				Text("Accept")
			}
		}
	}
}
