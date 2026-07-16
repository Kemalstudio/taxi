package com.taxiplatform.application.promo

import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.domain.promo.DiscountType
import com.taxiplatform.domain.promo.PromoCode
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ValidatePromoUseCaseTest {

	private val promoCodeRepository = mockk<PromoCodeRepository>()
	private val useCase = ValidatePromoUseCase(promoCodeRepository)

	private fun promo(
		discountType: DiscountType = DiscountType.PERCENT,
		discountValue: Int = 10,
		active: Boolean = true,
		maxUses: Int? = null,
		usedCount: Int = 0,
		expiresAt: Instant? = null,
	) = PromoCode(
		id = UUID.randomUUID(),
		code = "WELCOME10",
		discountType = discountType,
		discountValue = discountValue,
		maxUses = maxUses,
		usedCount = usedCount,
		active = active,
		expiresAt = expiresAt,
	)

	@Test
	fun `applies a percentage discount and reports the final fare`() {
		every { promoCodeRepository.findByCode("WELCOME10") } returns promo()

		val preview = useCase.execute("welcome10", 100)

		assertEquals(10, preview.discountAmount)
		assertEquals(90, preview.finalFare)
	}

	@Test
	fun `a fixed discount never exceeds the fare itself`() {
		every { promoCodeRepository.findByCode("TAXI5") } returns
			promo(discountType = DiscountType.FIXED, discountValue = 5).copy(code = "TAXI5")

		val preview = useCase.execute("TAXI5", 3)

		assertEquals(3, preview.discountAmount)
		assertEquals(0, preview.finalFare)
	}

	@Test
	fun `rejects an unknown code`() {
		every { promoCodeRepository.findByCode("NOPE") } returns null

		assertThrows(IllegalArgumentException::class.java) { useCase.execute("NOPE", 100) }
	}

	@Test
	fun `rejects an inactive code`() {
		every { promoCodeRepository.findByCode("WELCOME10") } returns promo(active = false)

		assertThrows(IllegalArgumentException::class.java) { useCase.execute("WELCOME10", 100) }
	}

	@Test
	fun `rejects a code that already hit its usage cap`() {
		every { promoCodeRepository.findByCode("WELCOME10") } returns promo(maxUses = 1, usedCount = 1)

		assertThrows(IllegalArgumentException::class.java) { useCase.execute("WELCOME10", 100) }
	}

	@Test
	fun `rejects an expired code`() {
		every { promoCodeRepository.findByCode("WELCOME10") } returns
			promo(expiresAt = Instant.now().minusSeconds(60))

		assertThrows(IllegalArgumentException::class.java) { useCase.execute("WELCOME10", 100) }
	}
}
