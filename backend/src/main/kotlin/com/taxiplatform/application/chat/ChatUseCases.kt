package com.taxiplatform.application.chat

import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideMessageRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.application.ride.RideNotFoundException
import com.taxiplatform.domain.ride.RideMessage
import com.taxiplatform.domain.ride.RideStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val TERMINAL_STATUSES = setOf(RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVERS_FOUND)

data class SendRideMessageCommand(
	val rideId: UUID,
	val senderId: UUID,
	val body: String,
)

@Service
class SendRideMessageUseCase(
	private val rideRepository: RideRepository,
	private val userRepository: UserRepository,
	private val rideMessageRepository: RideMessageRepository,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(command: SendRideMessageCommand): RideMessage {
		val body = command.body.trim()
		require(body.isNotEmpty()) { "message body must not be blank" }
		val ride = rideRepository.findById(command.rideId) ?: throw RideNotFoundException(command.rideId)
		if (command.senderId != ride.passengerId && command.senderId != ride.driverId) {
			throw RideAccessDeniedException("User ${command.senderId} is not part of ride ${ride.id}")
		}
		if (ride.status in TERMINAL_STATUSES) {
			throw InvalidRideStateException("Ride ${ride.id} is no longer active")
		}
		val sender = userRepository.findById(command.senderId) ?: throw RideNotFoundException(command.rideId)

		val message = rideMessageRepository.save(
			RideMessage(
				id = UUID.randomUUID(),
				rideId = ride.id,
				senderId = command.senderId,
				senderRole = sender.role,
				body = body.take(1000),
				createdAt = Instant.now(),
			),
		)
		rideEventsPublisher.rideMessage(message)
		return message
	}
}

@Service
class ListRideMessagesUseCase(
	private val rideRepository: RideRepository,
	private val rideMessageRepository: RideMessageRepository,
) {
	fun execute(rideId: UUID, requesterId: UUID): List<RideMessage> {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		if (requesterId != ride.passengerId && requesterId != ride.driverId) {
			throw RideAccessDeniedException("User $requesterId is not part of ride $rideId")
		}
		return rideMessageRepository.findByRideId(rideId)
	}
}
