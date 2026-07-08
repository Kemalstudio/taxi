package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.AuthResponse
import com.taxiplatform.api.dto.LoginRequest
import com.taxiplatform.api.dto.RegisterRequest
import com.taxiplatform.application.auth.LoginCommand
import com.taxiplatform.application.auth.LoginUseCase
import com.taxiplatform.application.auth.RegisterCommand
import com.taxiplatform.application.auth.RegisterUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
	private val registerUseCase: RegisterUseCase,
	private val loginUseCase: LoginUseCase,
) {

	@PostMapping("/register")
	fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
		val result = registerUseCase.execute(
			RegisterCommand(
				email = request.email,
				rawPassword = request.password,
				role = request.role,
				fullName = request.fullName,
				phone = request.phone,
			),
		)
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(AuthResponse(result.userId, result.role, result.token))
	}

	@PostMapping("/login")
	fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
		val result = loginUseCase.execute(LoginCommand(request.email, request.password))
		return ResponseEntity.ok(AuthResponse(result.userId, result.role, result.token))
	}
}
