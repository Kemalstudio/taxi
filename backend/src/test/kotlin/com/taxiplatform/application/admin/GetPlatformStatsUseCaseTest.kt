package com.taxiplatform.application.admin

import com.taxiplatform.application.ports.AdminReadRepository
import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.domain.geo.DriverLocation
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class GetPlatformStatsUseCaseTest {

	private val adminReadRepository = mockk<AdminReadRepository>()
	private val driverGeoIndex = mockk<DriverGeoIndex>()
	private val useCase = GetPlatformStatsUseCase(adminReadRepository, driverGeoIndex)

	@Test
	fun `aggregates counts and sums active rides across in-flight statuses`() {
		every { adminReadRepository.countUsers() } returns 10
		every { adminReadRepository.countByRole(Role.DRIVER) } returns 4
		every { adminReadRepository.countByRole(Role.PASSENGER) } returns 6
		every { adminReadRepository.totalRides() } returns 20
		every { adminReadRepository.countRidesByStatus(any()) } returns 0
		every { adminReadRepository.countRidesByStatus(RideStatus.SEARCHING) } returns 2
		every { adminReadRepository.countRidesByStatus(RideStatus.ACCEPTED) } returns 1
		every { adminReadRepository.countRidesByStatus(RideStatus.IN_PROGRESS) } returns 3
		every { adminReadRepository.countRidesByStatus(RideStatus.COMPLETED) } returns 14
		every { driverGeoIndex.findAllOnline() } returns listOf(
			DriverLocation(UUID.randomUUID(), GeoPoint(52.5, 13.4)),
			DriverLocation(UUID.randomUUID(), GeoPoint(52.6, 13.5)),
		)

		val stats = useCase.execute()

		assertEquals(10, stats.totalUsers)
		assertEquals(4, stats.totalDrivers)
		assertEquals(6, stats.totalPassengers)
		assertEquals(20, stats.totalRides)
		assertEquals(2, stats.driversOnline)
		// SEARCHING(2) + ACCEPTED(1) + DRIVER_ARRIVED(0) + IN_PROGRESS(3)
		assertEquals(6, stats.activeRides)
		assertEquals(14, stats.ridesByStatus[RideStatus.COMPLETED])
	}
}
