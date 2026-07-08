package com.taxiplatform.application.auth

import com.taxiplatform.application.ports.JwtService
import com.taxiplatform.application.ports.PasswordHasher
import com.taxiplatform.application.ports.UserRepository
import org.springframework.stereotype.Service

data class LoginCommand(
	val email: String,
	val rawPassword: String,
)

@Service
class LoginUseCase(
	private val userRepository: UserRepository,
	private val passwordHasher: PasswordHasher,
	private val jwtService: JwtService,
) {
	fun execute(command: LoginCommand): AuthResult {
		val user = userRepository.findByEmail(command.email) ?: throw InvalidCredentialsException()
		if (!passwordHasher.matches(command.rawPassword, user.passwordHash)) {
			throw InvalidCredentialsException()
		}
		val token = jwtService.generateToken(user.id, user.role)
		return AuthResult(userId = user.id, role = user.role, token = token)
	}
}
