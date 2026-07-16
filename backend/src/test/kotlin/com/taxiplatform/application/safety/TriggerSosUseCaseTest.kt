package com.taxiplatform.application.safety

import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.SosIncidentRepository
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TriggerSosUseCaseTest {

	private val rideRepository = mockk<RideRepository>()
	private val sosIncidentRepository = mockk<SosIncidentRepository>()
	private val rideEventsPublisher = mockk<RideEventsPublisher>(relaxed = true)
	private val useCase = TriggerSosUseCase(rideRepository, sosIncidentRepository, rideEventsPublisher)

	private val passengerId = UUID.randomUUID()
	private val driverId = UUID.randomUUID()

	private fun activeRide() = Ride(
		id = UUID.randomUUID(),
		passengerId = passengerId,
		driverId = driverId,
		pickup = GeoPoint(37.94, 58.38),
		dropoff = GeoPoint(37.98, 58.36),
		status = RideStatus.IN_PROGRESS,
		requestedAt = Instant.now(),
		scheduledAt = null,
		acceptedAt = Instant.now(),
		arrivedAt = Instant.now(),
		startedAt = Instant.now(),
		completedAt = null,
		cancelledAt = null,
		cancelledReason = null,
	)

	@Test
	fun `logs the incident and alerts the admin topic`() {
		val ride = activeRide()
		every { rideRepository.findById(ride.id) } returns ride
		every { sosIncidentRepository.save(any()) } answers { firstArg() }

		val incident = useCase.execute(TriggerSosCommand(ride.id, passengerId, GeoPoint(37.95, 58.39), "help"))

		verify { sosIncidentRepository.save(incident) }
		verify { rideEventsPublisher.sosTriggered(incident) }
	}

	@Test
	fun `rejects a caller who isn't part of the ride`() {
		val ride = activeRide()
		every { rideRepository.findById(ride.id) } returns ride

		assertThrows(RideAccessDeniedException::class.java) {
			useCase.execute(TriggerSosCommand(ride.id, UUID.randomUUID(), GeoPoint(0.0, 0.0), null))
		}
	}
}
