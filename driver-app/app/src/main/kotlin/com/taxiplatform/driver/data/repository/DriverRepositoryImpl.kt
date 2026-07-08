package com.taxiplatform.driver.data.repository

import com.taxiplatform.driver.data.remote.DriverApi
import com.taxiplatform.driver.data.remote.dto.DriverLocationRequestDto
import com.taxiplatform.driver.data.remote.dto.DriverStatusRequestDto
import com.taxiplatform.driver.domain.model.DriverStatus
import com.taxiplatform.driver.domain.model.GeoPoint
import com.taxiplatform.driver.domain.repository.DriverRepository
import javax.inject.Inject

class DriverRepositoryImpl @Inject constructor(
	private val driverApi: DriverApi,
) : DriverRepository {

	override suspend fun setStatus(status: DriverStatus): Result<Unit> = runCatching {
		driverApi.setStatus(DriverStatusRequestDto(status = status.name))
		Unit
	}

	override suspend fun updateLocation(point: GeoPoint): Result<Unit> = runCatching {
		driverApi.updateLocation(DriverLocationRequestDto(lat = point.lat, lng = point.lng))
		Unit
	}
}
