package com.taxiplatform.domain.user

import java.time.Instant
import java.util.UUID

enum class Role {
	PASSENGER,
	DRIVER,
	OPERATOR,
	DISPATCHER,
	ADMIN,
	SUPER_ADMIN,
}

data class User(
	val id: UUID,
	val email: String,
	val passwordHash: String,
	val role: Role,
	val fullName: String,
	val phone: String?,
	val createdAt: Instant,
	val loyaltyPoints: Int = 0,
)
