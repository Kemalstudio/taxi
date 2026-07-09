package com.taxiplatform.config

import com.taxiplatform.application.ports.PasswordHasher
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.util.UUID

/**
 * Creates the initial ADMIN account on startup if one doesn't already exist,
 * so the dashboard has something to log in with. The password is hashed with
 * the app's own [PasswordHasher] rather than baked into a migration.
 */
@Configuration
class AdminSeeder {

	private val log = LoggerFactory.getLogger(AdminSeeder::class.java)

	@Bean
	fun seedAdmin(
		userRepository: UserRepository,
		passwordHasher: PasswordHasher,
		@Value("\${taxi.admin.seed-enabled}") seedEnabled: Boolean,
		@Value("\${taxi.admin.email}") email: String,
		@Value("\${taxi.admin.password}") password: String,
		@Value("\${taxi.admin.full-name}") fullName: String,
	): ApplicationRunner = ApplicationRunner {
		if (!seedEnabled) return@ApplicationRunner
		if (userRepository.findByEmail(email) != null) {
			log.info("Admin user {} already exists, skipping seed", email)
			return@ApplicationRunner
		}
		userRepository.save(
			User(
				id = UUID.randomUUID(),
				email = email,
				passwordHash = passwordHasher.hash(password),
				role = Role.ADMIN,
				fullName = fullName,
				phone = null,
				createdAt = Instant.now(),
			),
		)
		log.info("Seeded admin user {}", email)
	}
}
