package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RoleEntity
import com.taxiplatform.infrastructure.persistence.entity.UserEntity

fun UserEntity.toDomain() = User(
	id = id,
	email = email,
	passwordHash = passwordHash,
	role = Role.valueOf(role.name),
	fullName = fullName,
	phone = phone,
	createdAt = createdAt,
)

fun User.toEntity() = UserEntity(
	id = id,
	email = email,
	passwordHash = passwordHash,
	role = RoleEntity.valueOf(role.name),
	fullName = fullName,
	phone = phone,
	createdAt = createdAt,
)

fun DriverProfileEntity.toDomain() = DriverProfile(
	userId = userId,
	status = DriverStatus.valueOf(status.name),
	vehicleMake = vehicleMake,
	vehicleModel = vehicleModel,
	plateNumber = plateNumber,
	rating = rating,
	updatedAt = updatedAt,
)

fun DriverProfile.toEntity() = DriverProfileEntity(
	userId = userId,
	status = DriverStatusEntity.valueOf(status.name),
	vehicleMake = vehicleMake,
	vehicleModel = vehicleModel,
	plateNumber = plateNumber,
	rating = rating,
	updatedAt = updatedAt,
)

fun RideEntity.toDomain() = Ride(
	id = id,
	passengerId = passengerId,
	driverId = driverId,
	pickup = GeoPoint(pickupLat, pickupLng),
	dropoff = GeoPoint(dropoffLat, dropoffLng),
	status = RideStatus.valueOf(status.name),
	requestedAt = requestedAt,
	scheduledAt = scheduledAt,
	acceptedAt = acceptedAt,
	arrivedAt = arrivedAt,
	startedAt = startedAt,
	completedAt = completedAt,
	cancelledAt = cancelledAt,
	cancelledReason = cancelledReason,
)

fun Ride.toEntity() = RideEntity(
	id = id,
	passengerId = passengerId,
	driverId = driverId,
	pickupLat = pickup.lat,
	pickupLng = pickup.lng,
	dropoffLat = dropoff.lat,
	dropoffLng = dropoff.lng,
	status = RideStatusEntity.valueOf(status.name),
	requestedAt = requestedAt,
	scheduledAt = scheduledAt,
	acceptedAt = acceptedAt,
	arrivedAt = arrivedAt,
	startedAt = startedAt,
	completedAt = completedAt,
	cancelledAt = cancelledAt,
	cancelledReason = cancelledReason,
)

fun RideOfferEntity.toDomain() = RideOffer(
	id = id,
	rideId = rideId,
	driverId = driverId,
	status = RideOfferStatus.valueOf(status.name),
	offeredAt = offeredAt,
	expiresAt = expiresAt,
	respondedAt = respondedAt,
)

fun RideOffer.toEntity() = RideOfferEntity(
	id = id,
	rideId = rideId,
	driverId = driverId,
	status = RideOfferStatusEntity.valueOf(status.name),
	offeredAt = offeredAt,
	expiresAt = expiresAt,
	respondedAt = respondedAt,
)
