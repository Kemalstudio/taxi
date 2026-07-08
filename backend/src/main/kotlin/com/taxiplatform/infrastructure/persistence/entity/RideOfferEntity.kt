package com.taxiplatform.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_offers")
class RideOfferEntity(
	@Id
	val id: UUID,

	@Column(name = "ride_id", nullable = false)
	val rideId: UUID,

	@Column(name = "driver_id", nullable = false)
	val driverId: UUID,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	val status: RideOfferStatusEntity,

	@Column(name = "offered_at", nullable = false)
	val offeredAt: Instant,

	@Column(name = "expires_at", nullable = false)
	val expiresAt: Instant,

	@Column(name = "responded_at")
	val respondedAt: Instant?,
)

enum class RideOfferStatusEntity {
	PENDING,
	ACCEPTED,
	REJECTED,
	EXPIRED,
	CANCELLED,
}
