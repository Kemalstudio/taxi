package com.taxiplatform.application.auth

import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.JwtService
import com.taxiplatform.application.ports.PasswordHasher
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class RegisterCommand(
	val email: String,
	val rawPassword: String,
	val role: Role,
	val fullName: String,
	val phone: String?,
)

data class AuthResult(
	val userId: UUID,
	val role: Role,
	val token: String,
)

@Service
class RegisterUseCase(
	private val userRepository: UserRepository,
	private val driverProfileRepository: DriverProfileRepository,
	private val passwordHasher: PasswordHasher,
	private val jwtService: JwtService,
) {
	@Transactional
	fun execute(command: RegisterCommand): AuthResult {
		if (userRepository.findByEmail(command.email) != null) {
			throw EmailAlreadyRegisteredException(command.email)
		}

		val now = Instant.now()
		val user = userRepository.save(
			User(
				id = UUID.randomUUID(),
				email = command.email,
				passwordHash = passwordHasher.hash(command.rawPassword),
				role = command.role,
				fullName = command.fullName,
				phone = command.phone,
				createdAt = now,
			),
		)

		if (user.role == Role.DRIVER) {
			driverProfileRepository.save(
				DriverProfile(
					userId = user.id,
					status = DriverStatus.OFFLINE,
					vehicleMake = null,
					vehicleModel = null,
					plateNumber = null,
					rating = BigDecimal("5.00"),
					updatedAt = now,
				),
			)
		}

		val token = jwtService.generateToken(user.id, user.role)
		return AuthResult(userId = user.id, role = user.role, token = token)
	}
}
