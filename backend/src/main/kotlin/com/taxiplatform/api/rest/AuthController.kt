package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.AuthResponse
import com.taxiplatform.api.dto.AdminLoginResponse
import com.taxiplatform.api.dto.AdminTwoFactorSetupRequest
import com.taxiplatform.api.dto.AdminTwoFactorSetupResponse
import com.taxiplatform.api.dto.AdminTwoFactorVerifyRequest
import com.taxiplatform.api.dto.LoginRequest
import com.taxiplatform.api.dto.RegisterRequest
import com.taxiplatform.api.dto.RequestOtpRequest
import com.taxiplatform.api.dto.VerifyOtpRequest
import com.taxiplatform.application.auth.LoginCommand
import com.taxiplatform.application.auth.AdminLoginUseCase
import com.taxiplatform.application.auth.BeginAdminTwoFactorSetupUseCase
import com.taxiplatform.application.auth.VerifyAdminTwoFactorUseCase
import com.taxiplatform.application.auth.LoginUseCase
import com.taxiplatform.application.auth.RegisterCommand
import com.taxiplatform.application.auth.RegisterUseCase
import com.taxiplatform.application.auth.RequestOtpUseCase
import com.taxiplatform.application.auth.VerifyOtpUseCase
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
	private val requestOtpUseCase: RequestOtpUseCase,
	private val verifyOtpUseCase: VerifyOtpUseCase,
	private val adminLoginUseCase: AdminLoginUseCase,
	private val beginAdminTwoFactorSetupUseCase: BeginAdminTwoFactorSetupUseCase,
	private val verifyAdminTwoFactorUseCase: VerifyAdminTwoFactorUseCase,
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

	@PostMapping("/admin/login")
	fun adminLogin(@Valid @RequestBody request: LoginRequest): ResponseEntity<AdminLoginResponse> {
		val result = adminLoginUseCase.execute(LoginCommand(request.email, request.password))
		return ResponseEntity.ok(AdminLoginResponse(result.challengeId, result.setupRequired, result.expiresAt))
	}

	@PostMapping("/admin/2fa/setup")
	fun adminTwoFactorSetup(
		@Valid @RequestBody request: AdminTwoFactorSetupRequest,
	): ResponseEntity<AdminTwoFactorSetupResponse> {
		val result = beginAdminTwoFactorSetupUseCase.execute(request.challengeId)
		return ResponseEntity.ok(AdminTwoFactorSetupResponse(result.secret, result.provisioningUri))
	}

	@PostMapping("/admin/2fa/verify")
	fun adminTwoFactorVerify(
		@Valid @RequestBody request: AdminTwoFactorVerifyRequest,
	): ResponseEntity<AuthResponse> {
		val result = verifyAdminTwoFactorUseCase.execute(request.challengeId, request.code)
		return ResponseEntity.ok(AuthResponse(result.userId, result.role, result.token))
	}

	@PostMapping("/otp/request")
	fun requestOtp(@Valid @RequestBody request: RequestOtpRequest): ResponseEntity<Void> {
		requestOtpUseCase.execute(request.phone)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/otp/verify")
	fun verifyOtp(@Valid @RequestBody request: VerifyOtpRequest): ResponseEntity<AuthResponse> {
		val result = verifyOtpUseCase.execute(request.phone, request.code)
		return ResponseEntity.ok(AuthResponse(result.userId, result.role, result.token))
	}
}
