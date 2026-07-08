package com.taxiplatform.domain.driver

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class DriverStatus {
	OFFLINE,
	ONLINE,
	BUSY,
}

data class DriverProfile(
	val userId: UUID,
	val status: DriverStatus,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: BigDecimal,
	val updatedAt: Instant,
)
