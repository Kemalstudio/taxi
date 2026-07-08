package com.taxiplatform.application.ports

import com.taxiplatform.domain.geo.DriverCandidate
import com.taxiplatform.domain.geo.GeoPoint
import java.util.UUID

interface DriverGeoIndex {
	fun updateLocation(driverId: UUID, point: GeoPoint)
	fun removeDriver(driverId: UUID)

	/** Returns online drivers within [radiusKm] of [point], nearest first. */
	fun findNearby(point: GeoPoint, radiusKm: Double): List<DriverCandidate>
}
