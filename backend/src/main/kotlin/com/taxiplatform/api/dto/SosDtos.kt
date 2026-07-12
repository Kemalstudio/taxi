package com.taxiplatform.api.dto

import com.taxiplatform.domain.ride.SosIncident
import java.time.Instant
import java.util.UUID

data class TriggerSosRequest(
	val point: GeoPointRequest,
	val note: String? = null,
)

data class SosIncidentResponse(
	val id: UUID,
	val rideId: UUID,
	val userId: UUID,
	val lat: Double,
	val lng: Double,
	val note: String?,
	val createdAt: Instant,
) {
	companion object {
		fun from(incident: SosIncident) = SosIncidentResponse(
			id = incident.id,
			rideId = incident.rideId,
			userId = incident.userId,
			lat = incident.point.lat,
			lng = incident.point.lng,
			note = incident.note,
			createdAt = incident.createdAt,
		)
	}
}
