package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.MeResponse
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class MeController(
	private val userRepository: UserRepository,
) {
	@GetMapping
	fun me(@AuthenticationPrincipal principal: AuthenticatedPrincipal): MeResponse {
		val user = userRepository.findById(principal.userId) ?: throw IllegalStateException("Authenticated user not found")
		return MeResponse.from(user)
	}
}
