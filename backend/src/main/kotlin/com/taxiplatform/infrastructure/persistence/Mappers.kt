package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.promo.DiscountType
import com.taxiplatform.domain.promo.PromoCode
import com.taxiplatform.domain.promo.PromoRedemption
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideMessage
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.ride.RideRating
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.ride.RideTariff
import com.taxiplatform.domain.ride.SosIncident
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import com.taxiplatform.infrastructure.persistence.entity.DiscountTypeEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoCodeEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoRedemptionEntity
import com.taxiplatform.infrastructure.persistence.entity.RideEntity
import com.taxiplatform.infrastructure.persistence.entity.RideMessageEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideRatingEntity
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideTariffEntity
import com.taxiplatform.infrastructure.persistence.entity.RoleEntity
import com.taxiplatform.infrastructure.persistence.entity.SosIncidentEntity
import com.taxiplatform.infrastructure.persistence.entity.UserEntity

fun UserEntity.toDomain() = User(
	id = id,
	email = email,
	passwordHash = passwordHash,
	role = Role.valueOf(role.name),
	fullName = fullName,
	phone = phone,
	createdAt = createdAt,
	loyaltyPoints = loyaltyPoints,
)

fun User.toEntity() = UserEntity(
	id = id,
	email = email,
	passwordHash = passwordHash,
	role = RoleEntity.valueOf(role.name),
	fullName = fullName,
	phone = phone,
	createdAt = createdAt,
	loyaltyPoints = loyaltyPoints,
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
	tariff = RideTariff.valueOf(tariff.name),
	fare = fare,
	promoCode = promoCode,
	discountApplied = discountApplied,
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
	tariff = RideTariffEntity.valueOf(tariff.name),
	fare = fare,
	promoCode = promoCode,
	discountApplied = discountApplied,
)

fun RideRatingEntity.toDomain() = RideRating(
	id = id,
	rideId = rideId,
	raterId = raterId,
	rateeId = rateeId,
	stars = stars,
	comment = comment,
	createdAt = createdAt,
)

fun RideRating.toEntity() = RideRatingEntity(
	id = id,
	rideId = rideId,
	raterId = raterId,
	rateeId = rateeId,
	stars = stars,
	comment = comment,
	createdAt = createdAt,
)

fun RideMessageEntity.toDomain() = RideMessage(
	id = id,
	rideId = rideId,
	senderId = senderId,
	senderRole = Role.valueOf(senderRole.name),
	body = body,
	createdAt = createdAt,
)

fun RideMessage.toEntity() = RideMessageEntity(
	id = id,
	rideId = rideId,
	senderId = senderId,
	senderRole = RoleEntity.valueOf(senderRole.name),
	body = body,
	createdAt = createdAt,
)

fun SosIncidentEntity.toDomain() = SosIncident(
	id = id,
	rideId = rideId,
	userId = userId,
	point = GeoPoint(lat, lng),
	note = note,
	createdAt = createdAt,
)

fun SosIncident.toEntity() = SosIncidentEntity(
	id = id,
	rideId = rideId,
	userId = userId,
	lat = point.lat,
	lng = point.lng,
	note = note,
	createdAt = createdAt,
)

fun PromoCodeEntity.toDomain() = PromoCode(
	id = id,
	code = code,
	discountType = DiscountType.valueOf(discountType.name),
	discountValue = discountValue,
	maxUses = maxUses,
	usedCount = usedCount,
	active = active,
	expiresAt = expiresAt,
)

fun PromoCode.toEntity() = PromoCodeEntity(
	id = id,
	code = code,
	discountType = DiscountTypeEntity.valueOf(discountType.name),
	discountValue = discountValue,
	maxUses = maxUses,
	usedCount = usedCount,
	active = active,
	expiresAt = expiresAt,
)

fun PromoRedemptionEntity.toDomain() = PromoRedemption(
	id = id,
	promoId = promoId,
	userId = userId,
	rideId = rideId,
	createdAt = createdAt,
)

fun PromoRedemption.toEntity() = PromoRedemptionEntity(
	id = id,
	promoId = promoId,
	userId = userId,
	rideId = rideId,
	createdAt = createdAt,
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
