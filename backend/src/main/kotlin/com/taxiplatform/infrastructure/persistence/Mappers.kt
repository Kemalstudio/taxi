package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.domain.audit.AuditLogEntry
import com.taxiplatform.domain.auth.AdminTwoFactorChallenge
import com.taxiplatform.domain.auth.AdminTwoFactorPurpose
import com.taxiplatform.domain.auth.OtpChallenge
import com.taxiplatform.domain.broadcast.BroadcastLog
import com.taxiplatform.domain.broadcast.BroadcastSegment
import com.taxiplatform.domain.payout.Payout
import com.taxiplatform.domain.payout.PayoutStatus
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.driver.VerificationStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.promo.DiscountType
import com.taxiplatform.domain.promo.PromoCode
import com.taxiplatform.domain.promo.PromoRedemption
import com.taxiplatform.domain.push.PushSubscription
import com.taxiplatform.domain.settings.PlatformSetting
import com.taxiplatform.domain.ride.PaymentMethod
import com.taxiplatform.domain.ride.PaymentStatus
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
import com.taxiplatform.infrastructure.persistence.entity.AuditLogEntryEntity
import com.taxiplatform.infrastructure.persistence.entity.AdminTwoFactorChallengeEntity
import com.taxiplatform.infrastructure.persistence.entity.AdminTwoFactorPurposeEntity
import com.taxiplatform.infrastructure.persistence.entity.BroadcastLogEntity
import com.taxiplatform.infrastructure.persistence.entity.BroadcastSegmentEntity
import com.taxiplatform.infrastructure.persistence.entity.DiscountTypeEntity
import com.taxiplatform.infrastructure.persistence.entity.PayoutEntity
import com.taxiplatform.infrastructure.persistence.entity.PayoutStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.OtpChallengeEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.PaymentMethodEntity
import com.taxiplatform.infrastructure.persistence.entity.PaymentStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.PlatformSettingEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoCodeEntity
import com.taxiplatform.infrastructure.persistence.entity.PushSubscriptionEntity
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
import com.taxiplatform.infrastructure.persistence.entity.VerificationStatusEntity

fun UserEntity.toDomain() = User(
	id = id,
	email = email,
	passwordHash = passwordHash,
	role = Role.valueOf(role.name),
	fullName = fullName,
	phone = phone,
	createdAt = createdAt,
	loyaltyPoints = loyaltyPoints,
	banned = banned,
	bannedReason = bannedReason,
	bannedAt = bannedAt,
	twoFactorEnabled = twoFactorEnabled,
	twoFactorSecret = twoFactorSecret,
	twoFactorVerifiedAt = twoFactorVerifiedAt,
	twoFactorLastUsedStep = twoFactorLastUsedStep,
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
	banned = banned,
	bannedReason = bannedReason,
	bannedAt = bannedAt,
	twoFactorEnabled = twoFactorEnabled,
	twoFactorSecret = twoFactorSecret,
	twoFactorVerifiedAt = twoFactorVerifiedAt,
	twoFactorLastUsedStep = twoFactorLastUsedStep,
)

fun AdminTwoFactorChallengeEntity.toDomain() = AdminTwoFactorChallenge(
	id = id,
	userId = userId,
	purpose = AdminTwoFactorPurpose.valueOf(purpose.name),
	pendingSecret = pendingSecret,
	expiresAt = expiresAt,
	consumedAt = consumedAt,
	failedAttempts = failedAttempts,
	createdAt = createdAt,
)

fun AdminTwoFactorChallenge.toEntity() = AdminTwoFactorChallengeEntity(
	id = id,
	userId = userId,
	purpose = AdminTwoFactorPurposeEntity.valueOf(purpose.name),
	pendingSecret = pendingSecret,
	expiresAt = expiresAt,
	consumedAt = consumedAt,
	failedAttempts = failedAttempts,
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
	verificationStatus = VerificationStatus.valueOf(verificationStatus.name),
	rejectionReason = rejectionReason,
	licenseDocPath = licenseDocPath,
	vehicleDocPath = vehicleDocPath,
)

fun DriverProfile.toEntity() = DriverProfileEntity(
	userId = userId,
	status = DriverStatusEntity.valueOf(status.name),
	vehicleMake = vehicleMake,
	vehicleModel = vehicleModel,
	plateNumber = plateNumber,
	rating = rating,
	updatedAt = updatedAt,
	verificationStatus = VerificationStatusEntity.valueOf(verificationStatus.name),
	rejectionReason = rejectionReason,
	licenseDocPath = licenseDocPath,
	vehicleDocPath = vehicleDocPath,
)

fun RideEntity.toDomain() = Ride(
	id = id,
	passengerId = passengerId,
	driverId = driverId,
	pickup = GeoPoint(pickupLat, pickupLng),
	dropoff = GeoPoint(dropoffLat, dropoffLng),
	pickupLabel = pickupLabel,
	dropoffLabel = dropoffLabel,
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
	surgeMultiplier = surgeMultiplier,
	cancellationFee = cancellationFee,
	paymentMethod = PaymentMethod.valueOf(paymentMethod.name),
	paymentStatus = PaymentStatus.valueOf(paymentStatus.name),
)

fun Ride.toEntity() = RideEntity(
	id = id,
	passengerId = passengerId,
	driverId = driverId,
	pickupLat = pickup.lat,
	pickupLng = pickup.lng,
	dropoffLat = dropoff.lat,
	dropoffLng = dropoff.lng,
	pickupLabel = pickupLabel,
	dropoffLabel = dropoffLabel,
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
	surgeMultiplier = surgeMultiplier,
	cancellationFee = cancellationFee,
	paymentMethod = PaymentMethodEntity.valueOf(paymentMethod.name),
	paymentStatus = PaymentStatusEntity.valueOf(paymentStatus.name),
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

fun PushSubscriptionEntity.toDomain() = PushSubscription(
	id = id,
	userId = userId,
	endpoint = endpoint,
	p256dh = p256dh,
	auth = auth,
	createdAt = createdAt,
)

fun PushSubscription.toEntity() = PushSubscriptionEntity(
	id = id,
	userId = userId,
	endpoint = endpoint,
	p256dh = p256dh,
	auth = auth,
	createdAt = createdAt,
)

fun OtpChallengeEntity.toDomain() = OtpChallenge(
	id = id,
	phone = phone,
	code = code,
	expiresAt = expiresAt,
	consumedAt = consumedAt,
	createdAt = createdAt,
)

fun OtpChallenge.toEntity() = OtpChallengeEntity(
	id = id,
	phone = phone,
	code = code,
	expiresAt = expiresAt,
	consumedAt = consumedAt,
	createdAt = createdAt,
)

fun PlatformSettingEntity.toDomain() = PlatformSetting(key = key, value = value, updatedAt = updatedAt)

fun AuditLogEntryEntity.toDomain() = AuditLogEntry(
	id = id,
	actorId = actorId,
	actorName = actorName,
	action = action,
	targetType = targetType,
	targetId = targetId,
	details = details,
	createdAt = createdAt,
)

fun AuditLogEntry.toEntity() = AuditLogEntryEntity(
	id = id,
	actorId = actorId,
	actorName = actorName,
	action = action,
	targetType = targetType,
	targetId = targetId,
	details = details,
	createdAt = createdAt,
)

fun PayoutEntity.toDomain() = Payout(
	id = id,
	driverId = driverId,
	periodFrom = periodFrom,
	periodTo = periodTo,
	amount = amount,
	rideCount = rideCount,
	status = PayoutStatus.valueOf(status.name),
	createdAt = createdAt,
	paidAt = paidAt,
)

fun Payout.toEntity() = PayoutEntity(
	id = id,
	driverId = driverId,
	periodFrom = periodFrom,
	periodTo = periodTo,
	amount = amount,
	rideCount = rideCount,
	status = PayoutStatusEntity.valueOf(status.name),
	createdAt = createdAt,
	paidAt = paidAt,
)

fun BroadcastLogEntity.toDomain() = BroadcastLog(
	id = id,
	actorId = actorId,
	actorName = actorName,
	segment = BroadcastSegment.valueOf(segment.name),
	title = title,
	body = body,
	recipientCount = recipientCount,
	createdAt = createdAt,
)

fun BroadcastLog.toEntity() = BroadcastLogEntity(
	id = id,
	actorId = actorId,
	actorName = actorName,
	segment = BroadcastSegmentEntity.valueOf(segment.name),
	title = title,
	body = body,
	recipientCount = recipientCount,
	createdAt = createdAt,
)

fun PlatformSetting.toEntity() = PlatformSettingEntity(key = key, value = value, updatedAt = updatedAt)
