package com.taxiplatform.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_messages")
class RideMessageEntity(
	@Id
	val id: UUID,

	@Column(name = "ride_id", nullable = false)
	val rideId: UUID,

	@Column(name = "sender_id", nullable = false)
	val senderId: UUID,

	@Enumerated(EnumType.STRING)
	@Column(name = "sender_role", nullable = false)
	val senderRole: RoleEntity,

	@Column(nullable = false)
	val body: String,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
)
