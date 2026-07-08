package com.taxiplatform.driver.domain.usecase

import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
	private val authRepository: AuthRepository,
) {
	suspend operator fun invoke(email: String, password: String): Result<AuthSession> =
		authRepository.login(email, password)
}

class RegisterUseCase @Inject constructor(
	private val authRepository: AuthRepository,
) {
	suspend operator fun invoke(email: String, password: String, fullName: String, phone: String?): Result<AuthSession> =
		authRepository.register(email, password, fullName, phone)
}
