package com.taxiplatform.domain.ride

import java.time.Instant
import java.util.UUID

enum class RideOfferStatus {
	PENDING,
	ACCEPTED,
	REJECTED,
	EXPIRED,
	CANCELLED,
}

data class RideOffer(
	val id: UUID,
	val rideId: UUID,
	val driverId: UUID,
	val status: RideOfferStatus,
	val offeredAt: Instant,
	val expiresAt: Instant,
	val respondedAt: Instant?,
)
