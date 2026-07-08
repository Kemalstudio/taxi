package com.taxiplatform.driver.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.taxiplatform.driver.domain.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Caller is responsible for having already obtained ACCESS_FINE_LOCATION before collecting. */
@Singleton
class LocationTracker @Inject constructor(
	@ApplicationContext context: Context,
) {
	private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

	@SuppressLint("MissingPermission")
	fun locationUpdates(intervalMs: Long = 8000L): Flow<GeoPoint> = callbackFlow {
		val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs).build()
		val callback = object : LocationCallback() {
			override fun onLocationResult(result: LocationResult) {
				result.lastLocation?.let { location ->
					trySend(GeoPoint(location.latitude, location.longitude))
				}
			}
		}
		client.requestLocationUpdates(request, callback, null)
		awaitClose { client.removeLocationUpdates(callback) }
	}
}
