package com.taxiplatform.api.dto

import com.taxiplatform.domain.driver.DriverStatus
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class DriverStatusRequest(
	val status: DriverStatus,
)

data class DriverLocationRequest(
	@field:DecimalMin("-90.0") @field:DecimalMax("90.0")
	val lat: Double,

	@field:DecimalMin("-180.0") @field:DecimalMax("180.0")
	val lng: Double,
)
