package com.taxiplatform.driver.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.taxiplatform.driver.BuildConfig
import com.taxiplatform.driver.data.remote.AuthApi
import com.taxiplatform.driver.data.remote.AuthInterceptor
import com.taxiplatform.driver.data.remote.DriverApi
import com.taxiplatform.driver.data.remote.RideApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

	@Provides
	@Singleton
	fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

	@Provides
	@Singleton
	fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
		val logging = HttpLoggingInterceptor().apply {
			level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
		}
		return OkHttpClient.Builder()
			.addInterceptor(authInterceptor)
			.addInterceptor(logging)
			.build()
	}

	@Provides
	@Singleton
	fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
		Retrofit.Builder()
			.baseUrl(BuildConfig.BASE_URL)
			.client(okHttpClient)
			.addConverterFactory(MoshiConverterFactory.create(moshi))
			.build()

	@Provides
	@Singleton
	fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

	@Provides
	@Singleton
	fun provideRideApi(retrofit: Retrofit): RideApi = retrofit.create(RideApi::class.java)

	@Provides
	@Singleton
	fun provideDriverApi(retrofit: Retrofit): DriverApi = retrofit.create(DriverApi::class.java)
}
