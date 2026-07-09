package com.taxiplatform.driver.domain.repository

import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.model.DriverStatus
import com.taxiplatform.driver.domain.model.GeoPoint
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.model.RideStatusUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
	suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<AuthSession>
	suspend fun login(email: String, password: String): Result<AuthSession>
	suspend fun currentSession(): AuthSession?
	suspend fun logout()
}

interface DriverRepository {
	suspend fun setStatus(status: DriverStatus): Result<Unit>
	suspend fun updateLocation(point: GeoPoint): Result<Unit>
}

interface RideRepository {
	suspend fun getRide(rideId: String): Result<Ride>
	suspend fun accept(rideId: String): Result<Ride>
	suspend fun reject(rideId: String): Result<Unit>
	suspend fun arrive(rideId: String): Result<Ride>
	suspend fun start(rideId: String): Result<Ride>
	suspend fun complete(rideId: String): Result<Ride>
}

/**
 * Real-time push channel: ride offers and status changes from the backend's
 * STOMP topics. [currentOffer] is a StateFlow (not a plain event stream) so
 * that a screen navigating in *after* the offer arrived (e.g. Home reacting
 * to it and pushing to the Offer screen) still sees it — a cold/no-replay
 * flow would silently drop the offer if nothing was collecting it yet.
 */
interface RideEventsRepository {
	fun connect(driverId: String)
	fun disconnect()
	val currentOffer: StateFlow<RideOffer?>
	fun clearOffer()
	fun observeRideStatus(rideId: String): Flow<RideStatusUpdate>
}
