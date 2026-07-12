package com.taxiplatform.api.dto

import com.taxiplatform.domain.ride.RideRating
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.Instant
import java.util.UUID

data class RateRideRequest(
	@field:Min(1) @field:Max(5)
	val stars: Int,
	val comment: String? = null,
)

data class RatingResponse(
	val id: UUID,
	val rideId: UUID,
	val rateeId: UUID,
	val stars: Int,
	val comment: String?,
	val createdAt: Instant,
) {
	companion object {
		fun from(rating: RideRating) = RatingResponse(
			id = rating.id,
			rideId = rating.rideId,
			rateeId = rating.rateeId,
			stars = rating.stars,
			comment = rating.comment,
			createdAt = rating.createdAt,
		)
	}
}
