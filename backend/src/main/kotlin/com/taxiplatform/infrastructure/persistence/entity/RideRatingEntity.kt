package com.taxiplatform.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_ratings")
class RideRatingEntity(
	@Id
	val id: UUID,

	@Column(name = "ride_id", nullable = false)
	val rideId: UUID,

	@Column(name = "rater_id", nullable = false)
	val raterId: UUID,

	@Column(name = "ratee_id", nullable = false)
	val rateeId: UUID,

	@Column(nullable = false)
	@JdbcTypeCode(SqlTypes.SMALLINT)
	val stars: Int,

	val comment: String?,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
)
