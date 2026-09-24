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

	@Column(name = "loyalty_points", nullable = false)
	val loyaltyPoints: Int = 0,

	@Column(nullable = false)
	val banned: Boolean = false,

	@Column(name = "banned_reason")
	val bannedReason: String? = null,

	@Column(name = "banned_at")
	val bannedAt: Instant? = null,

	@Column(name = "two_factor_enabled", nullable = false)
	val twoFactorEnabled: Boolean = false,

	@Column(name = "two_factor_secret")
	val twoFactorSecret: String? = null,

	@Column(name = "two_factor_verified_at")
	val twoFactorVerifiedAt: Instant? = null,

	@Column(name = "two_factor_last_used_step")
	val twoFactorLastUsedStep: Long? = null,
)

enum class RoleEntity {
	PASSENGER,
	DRIVER,
	OPERATOR,
	DISPATCHER,
	MODERATOR,
	ACCOUNTANT,
	ADMIN,
	SUPER_ADMIN,
}
