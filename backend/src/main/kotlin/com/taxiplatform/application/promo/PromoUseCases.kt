package com.taxiplatform.application.promo

import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.domain.promo.DiscountType
import com.taxiplatform.domain.promo.PromoCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

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

class PromoCodeNotFoundException(id: UUID) : RuntimeException("Promo code not found: $id")
class PromoCodeAlreadyExistsException(code: String) : RuntimeException("Promo code already exists: $code")

@Service
class ListPromoCodesUseCase(
	private val promoCodeRepository: PromoCodeRepository,
) {
	fun execute(): List<PromoCode> = promoCodeRepository.findAll()
}

@Service
class CreatePromoCodeUseCase(
	private val promoCodeRepository: PromoCodeRepository,
) {
	@Transactional
	fun execute(
		code: String,
		discountType: DiscountType,
		discountValue: Int,
		maxUses: Int?,
		expiresAt: Instant?,
	): PromoCode {
		val normalizedCode = code.trim().uppercase()
		if (promoCodeRepository.findByCode(normalizedCode) != null) throw PromoCodeAlreadyExistsException(normalizedCode)
		return promoCodeRepository.save(
			PromoCode(
				id = UUID.randomUUID(),
				code = normalizedCode,
				discountType = discountType,
				discountValue = discountValue,
				maxUses = maxUses,
				usedCount = 0,
				active = true,
				expiresAt = expiresAt,
			),
		)
	}
}

data class PromoCodePatch(
	val discountType: DiscountType? = null,
	val discountValue: Int? = null,
	val maxUses: Int? = null,
	val expiresAt: Instant? = null,
	val active: Boolean? = null,
)

/** ADMIN-only partial update — covers editing terms as well as activating/deactivating a code. */
@Service
class UpdatePromoCodeUseCase(
	private val promoCodeRepository: PromoCodeRepository,
) {
	@Transactional
	fun execute(id: UUID, patch: PromoCodePatch): PromoCode {
		val promo = promoCodeRepository.findById(id) ?: throw PromoCodeNotFoundException(id)
		return promoCodeRepository.save(
			promo.copy(
				discountType = patch.discountType ?: promo.discountType,
				discountValue = patch.discountValue ?: promo.discountValue,
				maxUses = patch.maxUses ?: promo.maxUses,
				expiresAt = patch.expiresAt ?: promo.expiresAt,
				active = patch.active ?: promo.active,
			),
		)
	}
}
