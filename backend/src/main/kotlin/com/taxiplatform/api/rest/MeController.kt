package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.MeResponse
import com.taxiplatform.api.dto.PushSubscriptionRequest
import com.taxiplatform.api.dto.UnsubscribePushRequest
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.application.push.RegisterPushSubscriptionCommand
import com.taxiplatform.application.push.RegisterPushSubscriptionUseCase
import com.taxiplatform.application.push.UnregisterPushSubscriptionUseCase
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class MeController(
	private val userRepository: UserRepository,
	private val registerPushSubscriptionUseCase: RegisterPushSubscriptionUseCase,
	private val unregisterPushSubscriptionUseCase: UnregisterPushSubscriptionUseCase,
) {
	@GetMapping
	fun me(@AuthenticationPrincipal principal: AuthenticatedPrincipal): MeResponse {
		val user = userRepository.findById(principal.userId) ?: throw IllegalStateException("Authenticated user not found")
		return MeResponse.from(user)
	}

	@PostMapping("/push-subscription")
	fun subscribe(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: PushSubscriptionRequest,
	): ResponseEntity<Void> {
		registerPushSubscriptionUseCase.execute(
			RegisterPushSubscriptionCommand(
				userId = principal.userId,
				endpoint = request.endpoint,
				p256dh = request.keys.p256dh,
				auth = request.keys.auth,
			),
		)
		return ResponseEntity.status(HttpStatus.CREATED).build()
	}

	@DeleteMapping("/push-subscription")
	fun unsubscribe(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@RequestBody request: UnsubscribePushRequest,
	): ResponseEntity<Void> {
		unregisterPushSubscriptionUseCase.execute(principal.userId, request.endpoint)
		return ResponseEntity.noContent().build()
	}
}
