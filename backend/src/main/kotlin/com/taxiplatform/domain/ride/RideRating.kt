package com.taxiplatform.domain.ride

import java.time.Instant
import java.util.UUID

data class RideRating(
	val id: UUID,
	val rideId: UUID,
	val raterId: UUID,
	val rateeId: UUID,
	val stars: Int,
	val comment: String?,
	val createdAt: Instant,
)
