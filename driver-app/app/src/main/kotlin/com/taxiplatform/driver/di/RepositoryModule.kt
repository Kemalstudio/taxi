package com.taxiplatform.driver.di

import com.taxiplatform.driver.data.remote.ws.StompClient
import com.taxiplatform.driver.data.repository.AuthRepositoryImpl
import com.taxiplatform.driver.data.repository.DriverRepositoryImpl
import com.taxiplatform.driver.data.repository.RideRepositoryImpl
import com.taxiplatform.driver.domain.repository.AuthRepository
import com.taxiplatform.driver.domain.repository.DriverRepository
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import com.taxiplatform.driver.domain.repository.RideRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

	@Binds
	abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

	@Binds
	abstract fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository

	@Binds
	abstract fun bindRideRepository(impl: RideRepositoryImpl): RideRepository

	@Binds
	abstract fun bindRideEventsRepository(impl: StompClient): RideEventsRepository
}
