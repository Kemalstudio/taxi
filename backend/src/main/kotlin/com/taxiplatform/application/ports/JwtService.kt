package com.taxiplatform.application.ports

import com.taxiplatform.domain.user.Role
import java.util.UUID

data class JwtPrincipal(
	val userId: UUID,
	val role: Role,
)

interface JwtService {
	fun generateToken(userId: UUID, role: Role): String
	fun parse(token: String): JwtPrincipal?
}
