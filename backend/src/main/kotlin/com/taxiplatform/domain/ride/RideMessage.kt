package com.taxiplatform.domain.ride

import com.taxiplatform.domain.user.Role
import java.time.Instant
import java.util.UUID

data class RideMessage(
	val id: UUID,
	val rideId: UUID,
	val senderId: UUID,
	val senderRole: Role,
	val body: String,
	val createdAt: Instant,
)
