package com.taxiplatform.application.ports

import com.taxiplatform.domain.audit.AuditLogEntry
import com.taxiplatform.domain.auth.OtpChallenge
import com.taxiplatform.domain.auth.AdminTwoFactorChallenge
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
import com.taxiplatform.domain.user.User
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

interface UserRepository {
	fun findByEmail(email: String): User?
	fun findById(id: UUID): User?
	fun findByPhone(phone: String): User?
	fun save(user: User): User
}

interface DriverProfileRepository {
	fun findByUserId(userId: UUID): DriverProfile?
	fun save(profile: DriverProfile): DriverProfile

	/** Used to reject a plate number already claimed by a different driver. */
	fun findByPlateNumber(plateNumber: String): DriverProfile?
}

interface RideRepository {
	fun findById(id: UUID): Ride?
	fun save(ride: Ride): Ride

	/** Scheduled rides whose time has come (status SCHEDULED and scheduledAt <= now). */
	fun findDueScheduled(now: Instant): List<Ride>

	/** The driver's currently in-progress ride (ACCEPTED / DRIVER_ARRIVED / IN_PROGRESS), if any. */
	fun findActiveByDriver(driverId: UUID): Ride?

	/** A passenger's own rides, newest first — the ride-history list. */
	fun findByPassengerId(passengerId: UUID, limit: Int): List<Ride>

	/** A driver's own rides, newest first — backs the driver web cabinet's ride history. */
	fun findByDriverId(driverId: UUID, limit: Int): List<Ride>
}

interface RideOfferRepository {
	fun findById(id: UUID): RideOffer?
	fun findByRideId(rideId: UUID): List<RideOffer>
	fun findByRideIdAndStatus(rideId: UUID, status: RideOfferStatus): List<RideOffer>
	fun findExpiredPending(now: Instant): List<RideOffer>
	fun save(offer: RideOffer): RideOffer
}

interface RideRatingRepository {
	fun existsByRideIdAndRaterId(rideId: UUID, raterId: UUID): Boolean
	fun save(rating: RideRating): RideRating

	/** Null when the ratee has no ratings yet. */
	fun averageForRatee(rateeId: UUID): BigDecimal?
}

interface RideMessageRepository {
	fun save(message: RideMessage): RideMessage
	fun findByRideId(rideId: UUID): List<RideMessage>
}

interface SosIncidentRepository {
	fun save(incident: SosIncident): SosIncident
	fun findRecent(limit: Int): List<SosIncident>
}

interface PromoCodeRepository {
	fun findByCode(code: String): PromoCode?
	fun findById(id: UUID): PromoCode?
	fun findAll(): List<PromoCode>
	fun save(promo: PromoCode): PromoCode
}

interface PromoRedemptionRepository {
	fun existsByPromoIdAndUserId(promoId: UUID, userId: UUID): Boolean
	fun save(redemption: PromoRedemption): PromoRedemption
}

interface PushSubscriptionRepository {
	fun save(subscription: PushSubscription): PushSubscription
	fun findByUserId(userId: UUID): List<PushSubscription>
	fun deleteByUserIdAndEndpoint(userId: UUID, endpoint: String)

	/** Drops a subscription the push service reported as gone (410/404) — self-healing on send. */
	fun deleteByEndpoint(endpoint: String)
}

interface OtpChallengeRepository {
	fun save(challenge: OtpChallenge): OtpChallenge
	fun countRecentByPhone(phone: String, since: Instant): Long
	fun findLatestUnconsumedByPhone(phone: String): OtpChallenge?
	fun markConsumed(id: UUID, at: Instant)

	/** Recent codes (any state) — backs the admin visibility screen while there's no SMS gateway. */
	fun findRecent(limit: Int): List<OtpChallenge>
}

interface AdminTwoFactorChallengeRepository {
	fun findByIdForUpdate(id: UUID): AdminTwoFactorChallenge?
	fun save(challenge: AdminTwoFactorChallenge): AdminTwoFactorChallenge
	fun countCreatedSince(userId: UUID, since: Instant): Long
}

interface AuditLogRepository {
	fun save(entry: AuditLogEntry): AuditLogEntry
	fun findRecent(limit: Int): List<AuditLogEntry>
}

interface PayoutRepository {
	fun save(payout: Payout): Payout
	fun findById(id: UUID): Payout?
	fun findRecent(limit: Int): List<Payout>
	fun findByDriverId(driverId: UUID, limit: Int): List<Payout>
}

interface BroadcastLogRepository {
	fun save(entry: BroadcastLog): BroadcastLog
	fun findRecent(limit: Int): List<BroadcastLog>
}
