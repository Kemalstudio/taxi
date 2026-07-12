package com.taxiplatform.domain.promo

import java.time.Instant
import java.util.UUID

enum class DiscountType { PERCENT, FIXED }

data class PromoCode(
	val id: UUID,
	val code: String,
	val discountType: DiscountType,
	val discountValue: Int,
	val maxUses: Int?,
	val usedCount: Int,
	val active: Boolean,
	val expiresAt: Instant?,
) {
	fun isUsable(now: Instant): Boolean {
		if (!active) return false
		if (expiresAt != null && expiresAt.isBefore(now)) return false
		if (maxUses != null && usedCount >= maxUses) return false
		return true
	}

	/** Discount amount in the same currency unit as [fare], never more than the fare itself. */
	fun discountFor(fare: Int): Int {
		val raw = when (discountType) {
			DiscountType.PERCENT -> (fare * discountValue) / 100
			DiscountType.FIXED -> discountValue
		}
		return raw.coerceIn(0, fare)
	}
}

data class PromoRedemption(
	val id: UUID,
	val promoId: UUID,
	val userId: UUID,
	val rideId: UUID,
	val createdAt: Instant,
)
