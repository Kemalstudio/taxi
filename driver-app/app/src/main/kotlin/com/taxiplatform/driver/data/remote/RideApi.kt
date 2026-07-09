package com.taxiplatform.driver.data.remote

import com.taxiplatform.driver.data.remote.dto.RideResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST

interface RideApi {
	@GET("rides/{id}")
	suspend fun getRide(@Path("id") rideId: String): RideResponseDto

	@POST("rides/{id}/accept")
	suspend fun accept(@Path("id") rideId: String): RideResponseDto

	@POST("rides/{id}/reject")
	suspend fun reject(@Path("id") rideId: String): Response<Unit>

	@POST("rides/{id}/arrive")
	suspend fun arrive(@Path("id") rideId: String): RideResponseDto

	@POST("rides/{id}/start")
	suspend fun start(@Path("id") rideId: String): RideResponseDto

	@POST("rides/{id}/complete")
	suspend fun complete(@Path("id") rideId: String): RideResponseDto
}
