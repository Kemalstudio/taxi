package com.taxiplatform.driver.data.remote

import com.taxiplatform.driver.data.remote.dto.DriverLocationRequestDto
import com.taxiplatform.driver.data.remote.dto.DriverStatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DriverApi {
	@POST("driver/status")
	suspend fun setStatus(@Body request: DriverStatusRequestDto): Response<Unit>

	@POST("driver/location")
	suspend fun updateLocation(@Body request: DriverLocationRequestDto): Response<Unit>
}
