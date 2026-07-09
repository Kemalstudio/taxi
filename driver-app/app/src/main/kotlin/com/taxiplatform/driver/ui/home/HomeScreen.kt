package com.taxiplatform.driver.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
	onOfferReceived: () -> Unit,
	viewModel: HomeViewModel = hiltViewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val currentOffer by viewModel.currentOffer.collectAsState()

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestMultiplePermissions(),
	) { granted ->
		if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
			viewModel.setOnline(true)
		}
	}

	LaunchedEffect(currentOffer) {
		if (currentOffer != null) onOfferReceived()
	}

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
	) {
		Text(
			text = if (uiState.isOnline) "You're online" else "You're offline",
			style = MaterialTheme.typography.headlineMedium,
		)
		Spacer(modifier = Modifier.height(24.dp))

		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(text = "Offline")
			Spacer(modifier = Modifier.width(8.dp))
			Switch(
				checked = uiState.isOnline,
				enabled = !uiState.isLoading,
				onCheckedChange = { checked ->
					if (checked) {
						permissionLauncher.launch(
							arrayOf(
								Manifest.permission.ACCESS_FINE_LOCATION,
								Manifest.permission.ACCESS_COARSE_LOCATION,
							),
						)
					} else {
						viewModel.setOnline(false)
					}
				},
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(text = "Online")
		}

		uiState.error?.let { message ->
			Spacer(modifier = Modifier.height(16.dp))
			Text(text = message, color = MaterialTheme.colorScheme.error)
		}
	}
}
