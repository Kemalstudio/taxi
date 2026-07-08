package com.taxiplatform.domain.geo

data class GeoPoint(
	val lat: Double,
	val lng: Double,
)

data class DriverCandidate(
	val driverId: java.util.UUID,
	val distanceKm: Double,
)
