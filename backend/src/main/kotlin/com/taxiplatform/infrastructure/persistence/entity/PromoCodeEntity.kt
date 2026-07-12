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
@Table(name = "promo_codes")
class PromoCodeEntity(
	@Id
	val id: UUID,

	@Column(nullable = false, unique = true)
	val code: String,

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false)
	val discountType: DiscountTypeEntity,

	@Column(name = "discount_value", nullable = false)
	val discountValue: Int,

	@Column(name = "max_uses")
	val maxUses: Int?,

	@Column(name = "used_count", nullable = false)
	val usedCount: Int,

	@Column(nullable = false)
	val active: Boolean,

	@Column(name = "expires_at")
	val expiresAt: Instant?,
)

enum class DiscountTypeEntity { PERCENT, FIXED }

@Entity
@Table(name = "promo_redemptions")
class PromoRedemptionEntity(
	@Id
	val id: UUID,

	@Column(name = "promo_id", nullable = false)
	val promoId: UUID,

	@Column(name = "user_id", nullable = false)
	val userId: UUID,

	@Column(name = "ride_id", nullable = false)
	val rideId: UUID,

	@Column(name = "created_at", nullable = false)
	val createdAt: Instant,
)
