package com.taxiplatform.domain.ride

import com.taxiplatform.domain.geo.GeoPoint
import java.time.Instant
import java.util.UUID

enum class RideStatus {
	REQUESTED,
	SEARCHING,
	ACCEPTED,
	DRIVER_ARRIVED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED,
	NO_DRIVERS_FOUND,
}

data class Ride(
	val id: UUID,
	val passengerId: UUID,
	val driverId: UUID?,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
	val status: RideStatus,
	val requestedAt: Instant,
	val acceptedAt: Instant?,
	val arrivedAt: Instant?,
	val startedAt: Instant?,
	val completedAt: Instant?,
	val cancelledAt: Instant?,
	val cancelledReason: String?,
)
