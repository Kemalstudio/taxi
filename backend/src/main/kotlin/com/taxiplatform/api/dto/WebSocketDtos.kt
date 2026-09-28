package com.taxiplatform.api.dto

import java.time.Instant
import java.util.UUID

data class GeoPointDto(val lat: Double, val lng: Double)

data class RideOfferMessage(
	val rideId: UUID,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
	/** Everything the driver needs to decide on the offer without a second round-trip — mirrors
	 *  what Yandex Go / Uber push into the incoming-order card. */
	val pickupLabel: String?,
	val dropoffLabel: String?,
	val fare: Int?,
	val tariff: String,
	val paymentMethod: String,
	/** Seconds the driver has to accept before dispatch auto-advances to the next-nearest driver. */
	val expiresInSeconds: Long,
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

/** Sent to the admin-only drivers topic on every GPS ping, regardless of whether the driver
 * has an active ride — lets the live map update without waiting for the next poll. */
data class AdminDriverLocationMessage(
	val driverId: UUID,
	val lat: Double,
	val lng: Double,
)

/** Sent to the admin-only drivers topic whenever a driver goes online/offline/busy, so the
 * live map can add/remove their marker immediately instead of waiting for the next poll. */
data class AdminDriverStatusMessage(
	val driverId: UUID,
	val status: String,
)