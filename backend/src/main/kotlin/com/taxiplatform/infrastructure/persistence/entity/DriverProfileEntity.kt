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
)

enum class DriverStatusEntity {
	OFFLINE,
	ONLINE,
	BUSY,
}
