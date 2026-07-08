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
@Table(name = "users")
class UserEntity(
	@Id
	val id: UUID,

	@Column(nullable = false, unique = true)
	val email: String,

	@Column(name = "password_hash", nullable = false)
	val passwordHash: String,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	val role: RoleEntity,

	@Column(name = "full_name", nullable = false)
	val fullName: String,

	val phone: String?,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
)

enum class RoleEntity {
	PASSENGER,
	DRIVER,
}
