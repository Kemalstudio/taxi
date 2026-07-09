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
@Table(name = "rides")
class RideEntity(
	@Id
	val id: UUID,

	@Column(name = "passenger_id", nullable = false)
	val passengerId: UUID,

	@Column(name = "driver_id")
	val driverId: UUID?,

	@Column(name = "pickup_lat", nullable = false)
	val pickupLat: Double,

	@Column(name = "pickup_lng", nullable = false)
	val pickupLng: Double,

	@Column(name = "dropoff_lat", nullable = false)
	val dropoffLat: Double,

	@Column(name = "dropoff_lng", nullable = false)
	val dropoffLng: Double,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	val status: RideStatusEntity,

	@Column(name = "requested_at", nullable = false)
	val requestedAt: Instant,

	@Column(name = "scheduled_at")
	val scheduledAt: Instant?,

	@Column(name = "accepted_at")
	val acceptedAt: Instant?,

	@Column(name = "arrived_at")
	val arrivedAt: Instant?,

	@Column(name = "started_at")
	val startedAt: Instant?,

	@Column(name = "completed_at")
	val completedAt: Instant?,

	@Column(name = "cancelled_at")
	val cancelledAt: Instant?,

	@Column(name = "cancelled_reason")
	val cancelledReason: String?,
)

enum class RideStatusEntity {
	SCHEDULED,
	REQUESTED,
	SEARCHING,
	ACCEPTED,
	DRIVER_ARRIVED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED,
	NO_DRIVERS_FOUND,
}
