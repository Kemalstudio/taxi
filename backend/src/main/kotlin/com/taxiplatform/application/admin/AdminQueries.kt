package com.taxiplatform.application.admin

import com.taxiplatform.application.ports.AdminReadRepository
import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.domain.geo.DriverLocation
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import org.springframework.stereotype.Service

data class PlatformStats(
	val totalUsers: Long,
	val totalDrivers: Long,
	val totalPassengers: Long,
	val totalRides: Long,
	val ridesByStatus: Map<RideStatus, Long>,
	val driversOnline: Int,
	val activeRides: Long,
)

@Service
class GetPlatformStatsUseCase(
	private val adminReadRepository: AdminReadRepository,
	private val driverGeoIndex: DriverGeoIndex,
) {
	fun execute(): PlatformStats {
		val ridesByStatus = RideStatus.entries.associateWith { adminReadRepository.countRidesByStatus(it) }
		val activeRides = ACTIVE_STATUSES.sumOf { ridesByStatus[it] ?: 0L }
		return PlatformStats(
			totalUsers = adminReadRepository.countUsers(),
			totalDrivers = adminReadRepository.countByRole(Role.DRIVER),
			totalPassengers = adminReadRepository.countByRole(Role.PASSENGER),
			totalRides = adminReadRepository.totalRides(),
			ridesByStatus = ridesByStatus,
			driversOnline = driverGeoIndex.findAllOnline().size,
			activeRides = activeRides,
		)
	}

	companion object {
		private val ACTIVE_STATUSES = listOf(
			RideStatus.SEARCHING,
			RideStatus.ACCEPTED,
			RideStatus.DRIVER_ARRIVED,
			RideStatus.IN_PROGRESS,
		)
	}
}

@Service
class ListRidesUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(status: RideStatus?, limit: Int): List<Ride> =
		adminReadRepository.listRides(status, limit.coerceIn(1, 200))
}

@Service
class ListDriversUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(): List<DriverWithUser> = adminReadRepository.listDrivers()
}

@Service
class ListOnlineDriversUseCase(
	private val driverGeoIndex: DriverGeoIndex,
) {
	fun execute(): List<DriverLocation> = driverGeoIndex.findAllOnline()
}
