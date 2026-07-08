package com.taxiplatform.api.dto

import com.taxiplatform.domain.user.Role
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
)
