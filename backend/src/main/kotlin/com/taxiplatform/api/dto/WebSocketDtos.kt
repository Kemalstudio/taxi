package com.taxiplatform.api.dto

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