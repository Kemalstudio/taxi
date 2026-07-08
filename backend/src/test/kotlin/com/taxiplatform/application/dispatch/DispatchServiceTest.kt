package com.taxiplatform.application.dispatch

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideOfferRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.DriverCandidate
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class DispatchServiceTest {

	private val rideRepository = mockk<RideRepository>()
	private val rideOfferRepository = mockk<RideOfferRepository>()
	private val driverProfileRepository = mockk<DriverProfileRepository>()
	private val driverGeoIndex = mockk<DriverGeoIndex>()
	private val rideEventsPublisher = mockk<RideEventsPublisher>(relaxed = true)

	private lateinit var dispatchService: DispatchService

	private val pickup = GeoPoint(52.52, 13.405)
	private val dropoff = GeoPoint(52.53, 13.41)
	private val passengerId = UUID.randomUUID()
	private val driverA = UUID.randomUUID()
	private val driverB = UUID.randomUUID()

	private fun newRide(status: RideStatus = RideStatus.REQUESTED) = Ride(
		id = UUID.randomUUID(),
		passengerId = passengerId,
		driverId = null,
		pickup = pickup,
		dropoff = dropoff,
		status = status,
		requestedAt = Instant.now(),
		acceptedAt = null,
		arrivedAt = null,
		startedAt = null,
		completedAt = null,
		cancelledAt = null,
		cancelledReason = null,
	)

	@BeforeEach
	fun setUp() {
		dispatchService = DispatchService(
			rideRepository = rideRepository,
			rideOfferRepository = rideOfferRepository,
			driverProfileRepository = driverProfileRepository,
			driverGeoIndex = driverGeoIndex,
			rideEventsPublisher = rideEventsPublisher,
			searchRadiusKm = 5.0,
			offerTimeoutSeconds = 15,
		)
		every { rideRepository.save(any()) } answers { firstArg() }
		every { rideOfferRepository.save(any()) } answers { firstArg() }
	}

	@Test
	fun `offers ride to the nearest candidate and marks it SEARCHING`() {
		val ride = newRide()
		every { rideOfferRepository.findByRideId(ride.id) } returns emptyList()
		every { driverGeoIndex.findNearby(pickup, 5.0) } returns listOf(
			DriverCandidate(driverA, 1.2),
			DriverCandidate(driverB, 3.4),
		)

		val result = dispatchService.startDispatch(ride)

		assertEquals(RideStatus.SEARCHING, result.status)
		verify { rideEventsPublisher.rideOffered(driverA, any()) }
		val offerSlot = slot<RideOffer>()
		verify { rideOfferRepository.save(capture(offerSlot)) }
		assertEquals(driverA, offerSlot.captured.driverId)
		assertEquals(RideOfferStatus.PENDING, offerSlot.captured.status)
	}

	@Test
	fun `marks ride NO_DRIVERS_FOUND when no candidates are available`() {
		val ride = newRide()
		every { rideOfferRepository.findByRideId(ride.id) } returns emptyList()
		every { driverGeoIndex.findNearby(pickup, 5.0) } returns emptyList()

		val result = dispatchService.startDispatch(ride)

		assertEquals(RideStatus.NO_DRIVERS_FOUND, result.status)
		verify { rideEventsPublisher.rideStatusChanged(match { it.status == RideStatus.NO_DRIVERS_FOUND }) }
	}

	@Test
	fun `reassigns to the next nearest candidate when a driver rejects`() {
		val ride = newRide(status = RideStatus.SEARCHING)
		val pendingOffer = RideOffer(
			id = UUID.randomUUID(),
			rideId = ride.id,
			driverId = driverA,
			status = RideOfferStatus.PENDING,
			offeredAt = Instant.now(),
			expiresAt = Instant.now().plusSeconds(15),
			respondedAt = null,
		)

		every { rideOfferRepository.findByRideIdAndStatus(ride.id, RideOfferStatus.PENDING) } returns listOf(pendingOffer)
		every { rideRepository.findById(ride.id) } returns ride
		// After the reject is recorded, driverA now shows up in the ride's offer history.
		every { rideOfferRepository.findByRideId(ride.id) } returns listOf(
			pendingOffer.copy(status = RideOfferStatus.REJECTED),
		)
		every { driverGeoIndex.findNearby(pickup, 5.0) } returns listOf(
			DriverCandidate(driverA, 1.0),
			DriverCandidate(driverB, 2.0),
		)

		dispatchService.handleReject(ride.id, driverA)

		verify { rideEventsPublisher.rideOffered(driverB, any()) }
		verify(exactly = 0) { rideEventsPublisher.rideOffered(driverA, any()) }
	}

	@Test
	fun `reassigns to the next nearest candidate when an offer times out unanswered`() {
		val ride = newRide(status = RideStatus.SEARCHING)
		val expiredOffer = RideOffer(
			id = UUID.randomUUID(),
			rideId = ride.id,
			driverId = driverA,
			status = RideOfferStatus.PENDING,
			offeredAt = Instant.now().minusSeconds(30),
			expiresAt = Instant.now().minusSeconds(15),
			respondedAt = null,
		)

		every { rideRepository.findById(ride.id) } returns ride
		every { rideOfferRepository.findByRideId(ride.id) } returns listOf(
			expiredOffer.copy(status = RideOfferStatus.EXPIRED),
		)
		every { driverGeoIndex.findNearby(pickup, 5.0) } returns listOf(
			DriverCandidate(driverA, 1.0),
			DriverCandidate(driverB, 2.0),
		)

		dispatchService.expireOffer(expiredOffer)

		verify { rideEventsPublisher.rideOffered(driverB, any()) }
	}

	@Test
	fun `accepting a ride assigns the driver, marks them busy and cancels other pending offers`() {
		val ride = newRide(status = RideStatus.SEARCHING)
		val acceptedOffer = RideOffer(
			id = UUID.randomUUID(),
			rideId = ride.id,
			driverId = driverA,
			status = RideOfferStatus.PENDING,
			offeredAt = Instant.now(),
			expiresAt = Instant.now().plusSeconds(15),
			respondedAt = null,
		)
		val otherPendingOffer = acceptedOffer.copy(id = UUID.randomUUID(), driverId = driverB)

		every { rideOfferRepository.findByRideIdAndStatus(ride.id, RideOfferStatus.PENDING) } returns
			listOf(acceptedOffer, otherPendingOffer)
		every { rideRepository.findById(ride.id) } returns ride
		every { driverProfileRepository.findByUserId(driverA) } returns DriverProfile(
			userId = driverA,
			status = DriverStatus.ONLINE,
			vehicleMake = "Toyota",
			vehicleModel = "Camry",
			plateNumber = "AB123CD",
			rating = BigDecimal("5.00"),
			updatedAt = Instant.now(),
		)
		every { driverProfileRepository.save(any()) } answers { firstArg() }
		every { driverGeoIndex.removeDriver(driverA) } returns Unit

		val result = dispatchService.handleAccept(ride.id, driverA)

		assertEquals(RideStatus.ACCEPTED, result.status)
		assertEquals(driverA, result.driverId)
		assertNotNull(result.acceptedAt)
		verify { driverGeoIndex.removeDriver(driverA) }
		verify { driverProfileRepository.save(match { it.status == DriverStatus.BUSY }) }
		verify { rideOfferRepository.save(match { it.id == otherPendingOffer.id && it.status == RideOfferStatus.CANCELLED }) }
	}
}
