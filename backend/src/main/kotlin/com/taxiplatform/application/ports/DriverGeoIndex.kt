package com.taxiplatform.application.ports

import com.taxiplatform.domain.geo.DriverCandidate
import com.taxiplatform.domain.geo.DriverLocation
import com.taxiplatform.domain.geo.GeoPoint
import java.util.UUID

interface DriverGeoIndex {
	fun updateLocation(driverId: UUID, point: GeoPoint)
	fun removeDriver(driverId: UUID)

	/** Returns online drivers within [radiusKm] of [point], nearest first. */
	fun findNearby(point: GeoPoint, radiusKm: Double): List<DriverCandidate>

	/** All drivers currently in the live geo index (used by the admin live map). */
	fun findAllOnline(): List<DriverLocation>
}
