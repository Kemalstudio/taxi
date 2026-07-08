package com.taxiplatform.driver.data.repository

import com.taxiplatform.driver.data.local.TokenStore
import com.taxiplatform.driver.data.remote.AuthApi
import com.taxiplatform.driver.data.remote.dto.LoginRequestDto
import com.taxiplatform.driver.data.remote.dto.RegisterRequestDto
import com.taxiplatform.driver.data.remote.dto.toDomain
import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
	private val authApi: AuthApi,
	private val tokenStore: TokenStore,
) : AuthRepository {

	override suspend fun register(
		email: String,
		password: String,
		fullName: String,
		phone: String?,
	): Result<AuthSession> = runCatching {
		val response = authApi.register(
			RegisterRequestDto(email = email, password = password, role = "DRIVER", fullName = fullName, phone = phone),
		)
		response.toDomain().also { tokenStore.save(it) }
	}

	override suspend fun login(email: String, password: String): Result<AuthSession> = runCatching {
		val response = authApi.login(LoginRequestDto(email = email, password = password))
		response.toDomain().also { tokenStore.save(it) }
	}

	override suspend fun currentSession(): AuthSession? = tokenStore.current()

	override suspend fun logout() = tokenStore.clear()
}
