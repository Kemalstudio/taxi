package com.taxiplatform.api.dto

import com.taxiplatform.domain.ride.Ride
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.time.Instant
import java.util.UUID

data class GeoPointRequest(
	@field:DecimalMin("-90.0") @field:DecimalMax("90.0")
	val lat: Double,

	@field:DecimalMin("-180.0") @field:DecimalMax("180.0")
	val lng: Double,
)

data class RequestRideRequest(
	val pickup: GeoPointRequest,
	val dropoff: GeoPointRequest,
)

data class CancelRideRequest(
	val reason: String?,
)

data class RideResponse(
	val id: UUID,
	val passengerId: UUID,
	val driverId: UUID?,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
	val status: String,
	val requestedAt: Instant,
	val acceptedAt: Instant?,
	val arrivedAt: Instant?,
	val startedAt: Instant?,
	val completedAt: Instant?,
	val cancelledAt: Instant?,
	val cancelledReason: String?,
) {
	companion object {
		fun from(ride: Ride) = RideResponse(
			id = ride.id,
			passengerId = ride.passengerId,
			driverId = ride.driverId,
			pickup = GeoPointDto(ride.pickup.lat, ride.pickup.lng),
			dropoff = GeoPointDto(ride.dropoff.lat, ride.dropoff.lng),
			status = ride.status.name,
			requestedAt = ride.requestedAt,
			acceptedAt = ride.acceptedAt,
			arrivedAt = ride.arrivedAt,
			startedAt = ride.startedAt,
			completedAt = ride.completedAt,
			cancelledAt = ride.cancelledAt,
			cancelledReason = ride.cancelledReason,
		)
	}
}
