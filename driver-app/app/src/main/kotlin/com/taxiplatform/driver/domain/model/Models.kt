package com.taxiplatform.driver.domain.model

enum class DriverStatus {
	OFFLINE,
	ONLINE,
	BUSY,
}

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

data class GeoPoint(
	val lat: Double,
	val lng: Double,
)

data class Ride(
	val id: String,
	val passengerId: String,
	val driverId: String?,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
	val status: RideStatus,
)

data class RideOffer(
	val rideId: String,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
)

data class RideStatusUpdate(
	val rideId: String,
	val status: RideStatus,
	val driverId: String?,
)

data class AuthSession(
	val userId: String,
	val token: String,
)
