package com.taxiplatform.api.dto

import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.permissions
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
	@field:Email @field:NotBlank
	val email: String,

	@field:Size(min = 8, max = 100)
	val password: String,

	val role: Role,

	@field:NotBlank
	val fullName: String,

	val phone: String?,
)

data class LoginRequest(
	@field:Email @field:NotBlank
	val email: String,

	@field:NotBlank
	val password: String,
)

data class AuthResponse(
	val userId: UUID,
	val role: Role,
	val token: String,
	val permissions: Set<String> = role.permissions().mapTo(linkedSetOf()) { it.authority },
)

data class AdminLoginResponse(
	val challengeId: UUID,
	val setupRequired: Boolean,
	val expiresAt: java.time.Instant,
)

data class AdminTwoFactorSetupRequest(
	val challengeId: UUID,
)

data class AdminTwoFactorSetupResponse(
	val secret: String,
	val provisioningUri: String,
)

data class AdminTwoFactorVerifyRequest(
	val challengeId: UUID,
	@field:jakarta.validation.constraints.Pattern(regexp = "\\d{6}")
	val code: String,
)

data class RequestOtpRequest(
	@field:NotBlank
	val phone: String,
)

data class VerifyOtpRequest(
	@field:NotBlank
	val phone: String,

	@field:NotBlank
	val code: String,
)
