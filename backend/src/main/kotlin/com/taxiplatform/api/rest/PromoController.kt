package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.PromoPreviewResponse
import com.taxiplatform.api.dto.ValidatePromoRequest
import com.taxiplatform.application.promo.ValidatePromoUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/promo")
class PromoController(
	private val validatePromoUseCase: ValidatePromoUseCase,
) {
	@PostMapping("/validate")
	fun validate(@Valid @RequestBody request: ValidatePromoRequest): ResponseEntity<PromoPreviewResponse> =
		ResponseEntity.ok(PromoPreviewResponse.from(validatePromoUseCase.execute(request.code, request.fare)))
}
