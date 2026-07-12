package com.taxiplatform.application.chat

import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideMessageRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SendRideMessageUseCaseTest {

	private val rideRepository = mockk<RideRepository>()
	private val userRepository = mockk<UserRepository>()
	private val rideMessageRepository = mockk<RideMessageRepository>()
	private val rideEventsPublisher = mockk<RideEventsPublisher>(relaxed = true)
	private val useCase = SendRideMessageUseCase(rideRepository, userRepository, rideMessageRepository, rideEventsPublisher)

	private val passengerId = UUID.randomUUID()
	private val driverId = UUID.randomUUID()

	private fun activeRide(status: RideStatus = RideStatus.ACCEPTED) = Ride(
		id = UUID.randomUUID(),
		passengerId = passengerId,
		driverId = driverId,
		pickup = GeoPoint(37.94, 58.38),
		dropoff = GeoPoint(37.98, 58.36),
		status = status,
		requestedAt = Instant.now(),
		scheduledAt = null,
		acceptedAt = Instant.now(),
		arrivedAt = null,
		startedAt = null,
		completedAt = null,
		cancelledAt = null,
		cancelledReason = null,
	)

	@Test
	fun `passenger can send a message on an active ride and it's broadcast`() {
		val ride = activeRide()
		every { rideRepository.findById(ride.id) } returns ride
		every { userRepository.findById(passengerId) } returns User(
			id = passengerId,
			email = "p@example.com",
			passwordHash = "x",
			role = Role.PASSENGER,
			fullName = "Passenger",
			phone = null,
			createdAt = Instant.now(),
		)
		every { rideMessageRepository.save(any()) } answers { firstArg() }

		val message = useCase.execute(SendRideMessageCommand(ride.id, passengerId, "  On my way  "))

		assertEquals("On my way", message.body)
		verify { rideEventsPublisher.rideMessage(message) }
	}

	@Test
	fun `rejects a sender who isn't part of the ride`() {
		val ride = activeRide()
		every { rideRepository.findById(ride.id) } returns ride

		assertThrows(RideAccessDeniedException::class.java) {
			useCase.execute(SendRideMessageCommand(ride.id, UUID.randomUUID(), "hi"))
		}
	}

	@Test
	fun `rejects messages on a completed ride`() {
		val ride = activeRide(status = RideStatus.COMPLETED)
		every { rideRepository.findById(ride.id) } returns ride

		assertThrows(InvalidRideStateException::class.java) {
			useCase.execute(SendRideMessageCommand(ride.id, passengerId, "hi"))
		}
	}
}
