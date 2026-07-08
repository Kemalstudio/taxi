package com.taxiplatform.driver.domain.repository

import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.model.DriverStatus
import com.taxiplatform.driver.domain.model.GeoPoint
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.model.RideStatusUpdate
import kotlinx.coroutines.flow.Flow

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
	suspend fun accept(rideId: String): Result<Ride>
	suspend fun reject(rideId: String): Result<Unit>
	suspend fun arrive(rideId: String): Result<Ride>
	suspend fun start(rideId: String): Result<Ride>
	suspend fun complete(rideId: String): Result<Ride>
}

/** Real-time push channel: ride offers and status changes from the backend's STOMP topics. */
interface RideEventsRepository {
	fun connect(driverId: String)
	fun disconnect()
	fun observeOffers(): Flow<RideOffer>
	fun observeRideStatus(rideId: String): Flow<RideStatusUpdate>
}
