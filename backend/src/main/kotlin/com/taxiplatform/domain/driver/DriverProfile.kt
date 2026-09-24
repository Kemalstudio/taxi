package com.taxiplatform.domain.driver

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class DriverStatus {
	OFFLINE,
	ONLINE,
	BUSY,
}

/** Ops review of a driver's license/vehicle documents — gates whether they can go online. */
enum class VerificationStatus {
	PENDING,
	APPROVED,
	REJECTED,
}

data class DriverProfile(
	val userId: UUID,
	val status: DriverStatus,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: BigDecimal,
	val updatedAt: Instant,
	val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
	val rejectionReason: String? = null,
	val licenseDocPath: String? = null,
	val vehicleDocPath: String? = null,
)
