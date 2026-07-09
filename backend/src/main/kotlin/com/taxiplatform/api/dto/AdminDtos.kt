package com.taxiplatform.api.dto

import com.taxiplatform.application.admin.PlatformStats
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.domain.geo.DriverLocation
import java.util.UUID

data class PlatformStatsResponse(
	val totalUsers: Long,
	val totalDrivers: Long,
	val totalPassengers: Long,
	val totalRides: Long,
	val ridesByStatus: Map<String, Long>,
	val driversOnline: Int,
	val activeRides: Long,
) {
	companion object {
		fun from(stats: PlatformStats) = PlatformStatsResponse(
			totalUsers = stats.totalUsers,
			totalDrivers = stats.totalDrivers,
			totalPassengers = stats.totalPassengers,
			totalRides = stats.totalRides,
			ridesByStatus = stats.ridesByStatus.mapKeys { it.key.name },
			driversOnline = stats.driversOnline,
			activeRides = stats.activeRides,
		)
	}
}

data class AdminDriverResponse(
	val userId: UUID,
	val fullName: String,
	val email: String,
	val phone: String?,
	val status: String,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: String,
) {
	companion object {
		fun from(driver: DriverWithUser) = AdminDriverResponse(
			userId = driver.user.id,
			fullName = driver.user.fullName,
			email = driver.user.email,
			phone = driver.user.phone,
			status = driver.profile.status.name,
			vehicleMake = driver.profile.vehicleMake,
			vehicleModel = driver.profile.vehicleModel,
			plateNumber = driver.profile.plateNumber,
			rating = driver.profile.rating.toPlainString(),
		)
	}
}

data class OnlineDriverResponse(
	val driverId: UUID,
	val lat: Double,
	val lng: Double,
) {
	companion object {
		fun from(location: DriverLocation) = OnlineDriverResponse(
			driverId = location.driverId,
			lat = location.point.lat,
			lng = location.point.lng,
		)
	}
}
