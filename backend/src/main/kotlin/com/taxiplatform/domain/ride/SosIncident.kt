package com.taxiplatform.domain.ride

import com.taxiplatform.domain.geo.GeoPoint
import java.time.Instant
import java.util.UUID

data class SosIncident(
	val id: UUID,
	val rideId: UUID,
	val userId: UUID,
	val point: GeoPoint,
	val note: String?,
	val createdAt: Instant,
)
