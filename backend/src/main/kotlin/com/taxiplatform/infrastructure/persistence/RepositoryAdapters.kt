package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.application.ports.AuditLogRepository
import com.taxiplatform.application.ports.AdminTwoFactorChallengeRepository
import com.taxiplatform.application.ports.BroadcastLogRepository
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.OtpChallengeRepository
import com.taxiplatform.application.ports.PayoutRepository
import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.application.ports.PromoRedemptionRepository
import com.taxiplatform.application.ports.PushSubscriptionRepository
import com.taxiplatform.application.ports.RideMessageRepository
import com.taxiplatform.application.ports.RideOfferRepository
import com.taxiplatform.application.ports.RideRatingRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.SettingsRepository
import com.taxiplatform.application.ports.SosIncidentRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.audit.AuditLogEntry
import com.taxiplatform.domain.auth.AdminTwoFactorChallenge
import com.taxiplatform.domain.auth.OtpChallenge
import com.taxiplatform.domain.broadcast.BroadcastLog
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.payout.Payout
import com.taxiplatform.domain.promo.PromoCode
import com.taxiplatform.domain.promo.PromoRedemption
import com.taxiplatform.domain.push.PushSubscription
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideMessage
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.ride.RideRating
import com.taxiplatform.domain.ride.SosIncident
import com.taxiplatform.domain.settings.PlatformSetting
import com.taxiplatform.domain.user.User
import com.taxiplatform.infrastructure.persistence.entity.OtpChallengeEntity
import com.taxiplatform.infrastructure.persistence.entity.AuditLogEntryEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataAuditLogRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataAdminTwoFactorChallengeRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataDriverProfileRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataBroadcastLogRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataOtpChallengeRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataPayoutRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataPlatformSettingRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataPromoCodeRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataPromoRedemptionRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataPushSubscriptionRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideMessageRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideOfferRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideRatingRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataSosIncidentRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataUserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID

@Repository
class JpaUserRepositoryAdapter(
	private val delegate: SpringDataUserRepository,
) : UserRepository {
	override fun findByEmail(email: String): User? = delegate.findByEmail(email)?.toDomain()
	override fun findById(id: UUID): User? = delegate.findById(id).orElse(null)?.toDomain()
	override fun findByPhone(phone: String): User? = delegate.findByPhone(phone)?.toDomain()
	override fun save(user: User): User = delegate.save(user.toEntity()).toDomain()
}

@Repository
class JpaDriverProfileRepositoryAdapter(
	private val delegate: SpringDataDriverProfileRepository,
) : DriverProfileRepository {
	override fun findByUserId(userId: UUID): DriverProfile? = delegate.findById(userId).orElse(null)?.toDomain()
	override fun save(profile: DriverProfile): DriverProfile = delegate.save(profile.toEntity()).toDomain()
	override fun findByPlateNumber(plateNumber: String): DriverProfile? = delegate.findByPlateNumber(plateNumber)?.toDomain()
}

@Repository
class JpaRideRepositoryAdapter(
	private val delegate: SpringDataRideRepository,
) : RideRepository {
	override fun findById(id: UUID): Ride? = delegate.findById(id).orElse(null)?.toDomain()
	override fun save(ride: Ride): Ride = delegate.save(ride.toEntity()).toDomain()

	override fun findDueScheduled(now: Instant): List<Ride> =
		delegate.findByStatusAndScheduledAtLessThanEqual(RideStatusEntity.SCHEDULED, now).map { it.toDomain() }

	override fun findActiveByDriver(driverId: UUID): Ride? =
		delegate.findFirstByDriverIdAndStatusInOrderByRequestedAtDesc(driverId, ACTIVE_STATUSES)?.toDomain()

	override fun findByPassengerId(passengerId: UUID, limit: Int): List<Ride> =
		delegate.findByPassengerIdOrderByRequestedAtDesc(passengerId, PageRequest.of(0, limit)).map { it.toDomain() }

	override fun findByDriverId(driverId: UUID, limit: Int): List<Ride> =
		delegate.findByDriverIdOrderByRequestedAtDesc(driverId, PageRequest.of(0, limit)).map { it.toDomain() }

	private companion object {
		val ACTIVE_STATUSES = listOf(
			RideStatusEntity.ACCEPTED,
			RideStatusEntity.DRIVER_ARRIVED,
			RideStatusEntity.IN_PROGRESS,
		)
	}
}

@Repository
class JpaRideOfferRepositoryAdapter(
	private val delegate: SpringDataRideOfferRepository,
) : RideOfferRepository {
	override fun findById(id: UUID): RideOffer? = delegate.findById(id).orElse(null)?.toDomain()

	override fun findByRideId(rideId: UUID): List<RideOffer> =
		delegate.findByRideId(rideId).map { it.toDomain() }

	override fun findByRideIdAndStatus(rideId: UUID, status: RideOfferStatus): List<RideOffer> =
		delegate.findByRideIdAndStatus(rideId, RideOfferStatusEntity.valueOf(status.name)).map { it.toDomain() }

	override fun findExpiredPending(now: Instant): List<RideOffer> =
		delegate.findByStatusAndExpiresAtBefore(RideOfferStatusEntity.PENDING, now).map { it.toDomain() }

	override fun save(offer: RideOffer): RideOffer = delegate.save(offer.toEntity()).toDomain()
}

@Repository
class JpaRideRatingRepositoryAdapter(
	private val delegate: SpringDataRideRatingRepository,
) : RideRatingRepository {
	override fun existsByRideIdAndRaterId(rideId: UUID, raterId: UUID): Boolean =
		delegate.existsByRideIdAndRaterId(rideId, raterId)

	override fun save(rating: RideRating): RideRating = delegate.save(rating.toEntity()).toDomain()

	override fun averageForRatee(rateeId: UUID): BigDecimal? =
		delegate.averageStarsForRatee(rateeId)?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }
}

@Repository
class JpaRideMessageRepositoryAdapter(
	private val delegate: SpringDataRideMessageRepository,
) : RideMessageRepository {
	override fun save(message: RideMessage): RideMessage = delegate.save(message.toEntity()).toDomain()

	override fun findByRideId(rideId: UUID): List<RideMessage> =
		delegate.findByRideIdOrderByCreatedAtAsc(rideId).map { it.toDomain() }
}

@Repository
class JpaSosIncidentRepositoryAdapter(
	private val delegate: SpringDataSosIncidentRepository,
) : SosIncidentRepository {
	override fun save(incident: SosIncident): SosIncident = delegate.save(incident.toEntity()).toDomain()

	override fun findRecent(limit: Int): List<SosIncident> =
		delegate.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }
}

@Repository
class JpaPromoCodeRepositoryAdapter(
	private val delegate: SpringDataPromoCodeRepository,
) : PromoCodeRepository {
	override fun findByCode(code: String): PromoCode? = delegate.findByCode(code)?.toDomain()

	override fun findById(id: UUID): PromoCode? = delegate.findById(id).orElse(null)?.toDomain()

	override fun findAll(): List<PromoCode> = delegate.findAll().map { it.toDomain() }

	override fun save(promo: PromoCode): PromoCode = delegate.save(promo.toEntity()).toDomain()
}

@Repository
class JpaPromoRedemptionRepositoryAdapter(
	private val delegate: SpringDataPromoRedemptionRepository,
) : PromoRedemptionRepository {
	override fun existsByPromoIdAndUserId(promoId: UUID, userId: UUID): Boolean =
		delegate.existsByPromoIdAndUserId(promoId, userId)

	override fun save(redemption: PromoRedemption): PromoRedemption = delegate.save(redemption.toEntity()).toDomain()
}

@Repository
class JpaPushSubscriptionRepositoryAdapter(
	private val delegate: SpringDataPushSubscriptionRepository,
) : PushSubscriptionRepository {
	override fun save(subscription: PushSubscription): PushSubscription = delegate.save(subscription.toEntity()).toDomain()

	override fun findByUserId(userId: UUID): List<PushSubscription> =
		delegate.findByUserId(userId).map { it.toDomain() }

	override fun deleteByUserIdAndEndpoint(userId: UUID, endpoint: String) =
		delegate.deleteByUserIdAndEndpoint(userId, endpoint)

	override fun deleteByEndpoint(endpoint: String) = delegate.deleteByEndpoint(endpoint)
}

@Repository
class JpaOtpChallengeRepositoryAdapter(
	private val delegate: SpringDataOtpChallengeRepository,
) : OtpChallengeRepository {
	override fun save(challenge: OtpChallenge): OtpChallenge = delegate.save(challenge.toEntity()).toDomain()

	override fun countRecentByPhone(phone: String, since: Instant): Long =
		delegate.countByPhoneAndCreatedAtAfter(phone, since)

	override fun findLatestUnconsumedByPhone(phone: String): OtpChallenge? =
		delegate.findFirstByPhoneAndConsumedAtIsNullOrderByCreatedAtDesc(phone)?.toDomain()

	override fun markConsumed(id: UUID, at: Instant) {
		val entity = delegate.findById(id).orElse(null) ?: return
		delegate.save(
			OtpChallengeEntity(
				id = entity.id,
				phone = entity.phone,
				code = entity.code,
				expiresAt = entity.expiresAt,
				consumedAt = at,
				createdAt = entity.createdAt,
			),
		)
	}

	override fun findRecent(limit: Int): List<OtpChallenge> =
		delegate.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }
}

@Repository
class JpaSettingsRepositoryAdapter(
	private val delegate: SpringDataPlatformSettingRepository,
) : SettingsRepository {
	override fun findAll(): List<PlatformSetting> = delegate.findAll().map { it.toDomain() }

	override fun upsert(key: String, value: String, at: Instant) {
		delegate.save(PlatformSetting(key = key, value = value, updatedAt = at).toEntity())
	}
}

@Repository
class JpaAuditLogRepositoryAdapter(
	private val delegate: SpringDataAuditLogRepository,
) : AuditLogRepository {
	override fun save(entry: AuditLogEntry): AuditLogEntry = delegate.save(entry.toEntity()).toDomain()

	override fun findRecent(limit: Int): List<AuditLogEntry> =
		delegate.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }
}

@Repository
class JpaAdminTwoFactorChallengeRepositoryAdapter(
	private val delegate: SpringDataAdminTwoFactorChallengeRepository,
) : AdminTwoFactorChallengeRepository {
	override fun findByIdForUpdate(id: UUID): AdminTwoFactorChallenge? = delegate.findLockedById(id)?.toDomain()
	override fun save(challenge: AdminTwoFactorChallenge): AdminTwoFactorChallenge =
		delegate.save(challenge.toEntity()).toDomain()
	override fun countCreatedSince(userId: UUID, since: Instant): Long =
		delegate.countByUserIdAndCreatedAtAfter(userId, since)
}

@Repository
class JpaPayoutRepositoryAdapter(
	private val delegate: SpringDataPayoutRepository,
) : PayoutRepository {
	override fun save(payout: Payout): Payout = delegate.save(payout.toEntity()).toDomain()

	override fun findById(id: UUID): Payout? = delegate.findById(id).orElse(null)?.toDomain()

	override fun findRecent(limit: Int): List<Payout> =
		delegate.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }

	override fun findByDriverId(driverId: UUID, limit: Int): List<Payout> =
		delegate.findByDriverIdOrderByCreatedAtDesc(driverId, PageRequest.of(0, limit)).map { it.toDomain() }
}

@Repository
class JpaBroadcastLogRepositoryAdapter(
	private val delegate: SpringDataBroadcastLogRepository,
) : BroadcastLogRepository {
	override fun save(entry: BroadcastLog): BroadcastLog = delegate.save(entry.toEntity()).toDomain()

	override fun findRecent(limit: Int): List<BroadcastLog> =
		delegate.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }
}
