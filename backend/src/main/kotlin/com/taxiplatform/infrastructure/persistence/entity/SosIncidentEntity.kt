package com.taxiplatform.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sos_incidents")
class SosIncidentEntity(
	@Id
	val id: UUID,

	@Column(name = "ride_id", nullable = false)
	val rideId: UUID,

	@Column(name = "user_id", nullable = false)
	val userId: UUID,

	@Column(nullable = false)
	val lat: Double,

	@Column(nullable = false)
	val lng: Double,

	val note: String?,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
)
