package com.taxiplatform.driver.data.repository

import com.taxiplatform.driver.data.remote.RideApi
import com.taxiplatform.driver.data.remote.dto.toDomain
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.repository.RideRepository
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor(
	private val rideApi: RideApi,
) : RideRepository {

	override suspend fun accept(rideId: String): Result<Ride> = runCatching { rideApi.accept(rideId).toDomain() }

	override suspend fun reject(rideId: String): Result<Unit> = runCatching {
		rideApi.reject(rideId)
		Unit
	}

	override suspend fun arrive(rideId: String): Result<Ride> = runCatching { rideApi.arrive(rideId).toDomain() }

	override suspend fun start(rideId: String): Result<Ride> = runCatching { rideApi.start(rideId).toDomain() }

	override suspend fun complete(rideId: String): Result<Ride> = runCatching { rideApi.complete(rideId).toDomain() }
}