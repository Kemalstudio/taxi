package com.taxiplatform.api.dto

import com.taxiplatform.application.promo.PromoPreview
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

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
