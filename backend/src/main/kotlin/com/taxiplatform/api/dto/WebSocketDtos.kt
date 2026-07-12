package com.taxiplatform.api.dto

import java.time.Instant
import java.util.UUID

data class GeoPointDto(val lat: Double, val lng: Double)

data class RideOfferMessage(
	val rideId: UUID,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
)

data class RideStatusMessage(
	val rideId: UUID,
	val status: String,
	val driverId: UUID?,
)

data class RideLocationMessage(
	val rideId: UUID,
	val driverId: UUID,
	val lat: Double,
	val lng: Double,
)

/** Sent over the ride's own topic — distinguished on the client by the presence of `body`. */
data class RideChatMessage(
	val rideId: UUID,
	val senderId: UUID,
	val senderRole: String,
	val body: String,
	val createdAt: Instant,
)

/** Sent to the admin-only SOS topic so the dashboard can raise a live alert. */
data class SosAlertMessage(
	val incidentId: UUID,
	val rideId: UUID,
	val userId: UUID,
	val lat: Double,
	val lng: Double,
	val createdAt: Instant,
)