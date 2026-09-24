package com.taxiplatform.api.dto

import com.taxiplatform.application.promo.PromoPreview
import com.taxiplatform.domain.promo.DiscountType
import com.taxiplatform.domain.promo.PromoCode
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class ValidatePromoRequest(
	@field:NotBlank
	val code: String,

	@field:Min(0)
	val fare: Int,
)

data class PromoPreviewResponse(
	val code: String,
	val discountAmount: Int,
	val finalFare: Int,
) {
	companion object {
		fun from(preview: PromoPreview) = PromoPreviewResponse(
			code = preview.code,
			discountAmount = preview.discountAmount,
			finalFare = preview.finalFare,
		)
	}
}

data class AdminPromoCodeResponse(
	val id: UUID,
	val code: String,
	val discountType: String,
	val discountValue: Int,
	val maxUses: Int?,
	val usedCount: Int,
	val active: Boolean,
	val expiresAt: Instant?,
) {
	companion object {
		fun from(promo: PromoCode) = AdminPromoCodeResponse(
			id = promo.id,
			code = promo.code,
			discountType = promo.discountType.name,
			discountValue = promo.discountValue,
			maxUses = promo.maxUses,
			usedCount = promo.usedCount,
			active = promo.active,
			expiresAt = promo.expiresAt,
		)
	}
}

data class CreatePromoCodeRequest(
	@field:NotBlank
	val code: String,
	val discountType: DiscountType,
	@field:Min(1)
	val discountValue: Int,
	val maxUses: Int? = null,
	val expiresAt: Instant? = null,
)

/** Admin-only partial patch — omitted fields keep their current value. */
data class UpdatePromoCodeRequest(
	val discountType: DiscountType? = null,
	val discountValue: Int? = null,
	val maxUses: Int? = null,
	val expiresAt: Instant? = null,
	val active: Boolean? = null,
)
