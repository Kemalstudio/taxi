package com.taxiplatform.application.rating

import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideRatingRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ride.AlreadyRatedException
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class RateRideUseCaseTest {

	private val rideRepository = mockk<RideRepository>()
	private val rideRatingRepository = mockk<RideRatingRepository>()
	private val driverProfileRepository = mockk<DriverProfileRepository>()
	private val useCase = RateRideUseCase(rideRepository, rideRatingRepository, driverProfileRepository)

	private val passengerId = UUID.randomUUID()
	private val driverId = UUID.randomUUID()

	private fun completedRide() = Ride(
		id = UUID.randomUUID(),
		passengerId = passengerId,
		driverId = driverId,
		pickup = GeoPoint(37.94, 58.38),
		dropoff = GeoPoint(37.98, 58.36),
		status = RideStatus.COMPLETED,
		requestedAt = Instant.now(),
		scheduledAt = null,
		acceptedAt = Instant.now(),
		arrivedAt = Instant.now(),
		startedAt = Instant.now(),
		completedAt = Instant.now(),
		cancelledAt = null,
		cancelledReason = null,
	)

	@Test
	fun `rates a completed ride and recomputes the driver's average rating`() {
		val ride = completedRide()
		every { rideRepository.findById(ride.id) } returns ride
		every { rideRatingRepository.existsByRideIdAndRaterId(ride.id, passengerId) } returns false
		every { rideRatingRepository.save(any()) } answers { firstArg() }
		every { rideRatingRepository.averageForRatee(driverId) } returns BigDecimal("4.50")
		every { driverProfileRepository.findByUserId(driverId) } returns DriverProfile(
			userId = driverId,
			status = DriverStatus.ONLINE,
			vehicleMake = null,
			vehicleModel = null,
			plateNumber = null,
			rating = BigDecimal("5.00"),
			updatedAt = Instant.now(),
		)
		every { driverProfileRepository.save(any()) } answers { firstArg() }

		val rating = useCase.execute(RateRideCommand(ride.id, passengerId, 5, "great ride"))

		assertEquals(driverId, rating.rateeId)
		assertEquals(5, rating.stars)
		val savedProfile = slot<DriverProfile>()
		verify { driverProfileRepository.save(capture(savedProfile)) }
		assertEquals(BigDecimal("4.50"), savedProfile.captured.rating)
	}

	@Test
	fun `rejects a rating from someone who isn't the ride's passenger`() {
		val ride = completedRide()
		every { rideRepository.findById(ride.id) } returns ride

		assertThrows(RideAccessDeniedException::class.java) {
			useCase.execute(RateRideCommand(ride.id, UUID.randomUUID(), 5, null))
		}
	}

	@Test
	fun `rejects rating a ride that isn't completed yet`() {
		val ride = completedRide().copy(status = RideStatus.IN_PROGRESS)
		every { rideRepository.findById(ride.id) } returns ride

		assertThrows(InvalidRideStateException::class.java) {
			useCase.execute(RateRideCommand(ride.id, passengerId, 5, null))
		}
	}

	@Test
	fun `rejects a second rating for the same ride`() {
		val ride = completedRide()
		every { rideRepository.findById(ride.id) } returns ride
		every { rideRatingRepository.existsByRideIdAndRaterId(ride.id, passengerId) } returns true

		assertThrows(AlreadyRatedException::class.java) {
			useCase.execute(RateRideCommand(ride.id, passengerId, 5, null))
		}
	}
}
