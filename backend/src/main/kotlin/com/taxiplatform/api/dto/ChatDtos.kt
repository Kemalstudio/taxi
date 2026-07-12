package com.taxiplatform.api.dto

import com.taxiplatform.domain.ride.RideMessage
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class SendMessageRequest(
	@field:NotBlank
	val body: String,
)

data class MessageResponse(
	val id: UUID,
	val rideId: UUID,
	val senderId: UUID,
	val senderRole: String,
	val body: String,
	val createdAt: Instant,
) {
	companion object {
		fun from(message: RideMessage) = MessageResponse(
			id = message.id,
			rideId = message.rideId,
			senderId = message.senderId,
			senderRole = message.senderRole.name,
			body = message.body,
			createdAt = message.createdAt,
		)
	}
}
