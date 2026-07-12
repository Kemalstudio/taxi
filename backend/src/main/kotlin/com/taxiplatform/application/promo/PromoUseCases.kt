package com.taxiplatform.application.promo

import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.domain.promo.PromoCode
import org.springframework.stereotype.Service
import java.time.Instant

data class PromoPreview(
	val code: String,
	val discountAmount: Int,
	val finalFare: Int,
)

@Service
class ValidatePromoUseCase(
	private val promoCodeRepository: PromoCodeRepository,
) {
	fun execute(code: String, fare: Int): PromoPreview {
		require(fare >= 0) { "fare must not be negative" }
		val promo = findUsable(code) ?: throw IllegalArgumentException("Promo code is invalid or expired")
		val discount = promo.discountFor(fare)
		return PromoPreview(code = promo.code, discountAmount = discount, finalFare = fare - discount)
	}

	/** Looks up a code and returns it only if it's still usable right now (shared with ride creation). */
	fun findUsable(code: String): PromoCode? {
		val promo = promoCodeRepository.findByCode(code.trim().uppercase()) ?: return null
		return promo.takeIf { it.isUsable(Instant.now()) }
	}
}
