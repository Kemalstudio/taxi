package com.taxiplatform.application.driver

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class UpdateDriverLocationUseCaseTest {

	private val driverProfileRepository = mockk<DriverProfileRepository>()
	private val driverGeoIndex = mockk<DriverGeoIndex>(relaxed = true)
	private val rideRepository = mockk<RideRepository>()
	private val rideEventsPublisher = mockk<RideEventsPublisher>(relaxed = true)
	private val useCase = UpdateDriverLocationUseCase(
		driverProfileRepository, driverGeoIndex, rideRepository, rideEventsPublisher,
	)

	private val driverId = UUID.randomUUID()
	private val point = GeoPoint(37.95, 58.38)

	private fun profile(status: DriverStatus) = DriverProfile(
		userId = driverId, status = status, vehicleMake = null, vehicleModel = null,
		plateNumber = null, rating = BigDecimal("5.00"), updatedAt = Instant.now(),
	)

	private fun activeRide() = Ride(
		id = UUID.randomUUID(), passengerId = UUID.randomUUID(), driverId = driverId,
		pickup = point, dropoff = GeoPoint(37.98, 58.36), status = RideStatus.IN_PROGRESS,
		requestedAt = Instant.now(), scheduledAt = null, acceptedAt = Instant.now(), arrivedAt = null,
		startedAt = Instant.now(), completedAt = null, cancelledAt = null, cancelledReason = null,
	)

	@Test
	fun `streams the driver location to the passenger ride topic while on a ride`() {
		val ride = activeRide()
		every { driverProfileRepository.findByUserId(driverId) } returns profile(DriverStatus.BUSY)
		every { rideRepository.findActiveByDriver(driverId) } returns ride

		useCase.execute(driverId, point)

		verify { rideEventsPublisher.driverLocation(ride.id, driverId, point) }
		// BUSY drivers are not put back into the dispatch geo-index.
		verify(exactly = 0) { driverGeoIndex.updateLocation(any(), any()) }
	}

	@Test
	fun `an idle online driver updates the geo-index and broadcasts nothing`() {
		every { driverProfileRepository.findByUserId(driverId) } returns profile(DriverStatus.ONLINE)
		every { rideRepository.findActiveByDriver(driverId) } returns null

		useCase.execute(driverId, point)

		verify { driverGeoIndex.updateLocation(driverId, point) }
		verify(exactly = 0) { rideEventsPublisher.driverLocation(any(), any(), any()) }
	}
}
