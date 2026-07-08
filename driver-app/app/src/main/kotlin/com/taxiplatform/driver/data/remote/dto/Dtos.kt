package com.taxiplatform.driver.data.remote.dto

import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.model.GeoPoint
import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.model.RideStatus
import com.taxiplatform.driver.domain.model.RideStatusUpdate

data class GeoPointDto(
	val lat: Double,
	val lng: Double,
)

data class RegisterRequestDto(
	val email: String,
	val password: String,
	val role: String,
	val fullName: String,
	val phone: String?,
)

data class LoginRequestDto(
	val email: String,
	val password: String,
)

data class AuthResponseDto(
	val userId: String,
	val role: String,
	val token: String,
)

data class DriverStatusRequestDto(
	val status: String,
)

data class DriverLocationRequestDto(
	val lat: Double,
	val lng: Double,
)

data class RideResponseDto(
	val id: String,
	val passengerId: String,
	val driverId: String?,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
	val status: String,
)

data class RideOfferMessageDto(
	val rideId: String,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
)

data class RideStatusMessageDto(
	val rideId: String,
	val status: String,
	val driverId: String?,
)

fun GeoPointDto.toDomain() = GeoPoint(lat, lng)

fun AuthResponseDto.toDomain() = AuthSession(userId = userId, token = token)

fun RideResponseDto.toDomain() = Ride(
	id = id,
	passengerId = passengerId,
	driverId = driverId,
	pickup = pickup.toDomain(),
	dropoff = dropoff.toDomain(),
	status = RideStatus.valueOf(status),
)

fun RideOfferMessageDto.toDomain() = RideOffer(
	rideId = rideId,
	pickup = pickup.toDomain(),
	dropoff = dropoff.toDomain(),
)

fun RideStatusMessageDto.toDomain() = RideStatusUpdate(
	rideId = rideId,
	status = RideStatus.valueOf(status),
	driverId = driverId,
)
