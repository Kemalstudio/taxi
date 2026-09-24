package com.taxiplatform.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "driver_profiles")
class DriverProfileEntity(
	@Id
	@Column(name = "user_id")
	val userId: UUID,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	val status: DriverStatusEntity,

	@Column(name = "vehicle_make")
	val vehicleMake: String?,

	@Column(name = "vehicle_model")
	val vehicleModel: String?,

	@Column(name = "plate_number")
	val plateNumber: String?,

	@Column(nullable = false)
	val rating: BigDecimal,

	@Column(name = "updated_at", nullable = false)
	val updatedAt: Instant,

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", nullable = false)
	val verificationStatus: VerificationStatusEntity = VerificationStatusEntity.PENDING,

	@Column(name = "rejection_reason")
	val rejectionReason: String? = null,

	@Column(name = "license_doc_path")
	val licenseDocPath: String? = null,

	@Column(name = "vehicle_doc_path")
	val vehicleDocPath: String? = null,
)

enum class DriverStatusEntity {
	OFFLINE,
	ONLINE,
	BUSY,
}

enum class VerificationStatusEntity {
	PENDING,
	APPROVED,
	REJECTED,
}
