package com.taxiplatform.infrastructure.persistence.jpa

import com.taxiplatform.infrastructure.persistence.entity.AuditLogEntryEntity
import com.taxiplatform.infrastructure.persistence.entity.AdminTwoFactorChallengeEntity
import com.taxiplatform.infrastructure.persistence.entity.BroadcastLogEntity
import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.PayoutEntity
import com.taxiplatform.infrastructure.persistence.entity.OtpChallengeEntity
import com.taxiplatform.infrastructure.persistence.entity.PlatformSettingEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoCodeEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoRedemptionEntity
import com.taxiplatform.infrastructure.persistence.entity.PushSubscriptionEntity
import com.taxiplatform.infrastructure.persistence.entity.RideEntity
import com.taxiplatform.infrastructure.persistence.entity.RideMessageEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferEntity
import com.taxiplatform.infrastructure.persistence.entity.RideRatingEntity
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RoleEntity
import com.taxiplatform.infrastructure.persistence.entity.SosIncidentEntity
import com.taxiplatform.infrastructure.persistence.entity.UserEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID
import jakarta.persistence.LockModeType

interface SpringDataUserRepository : JpaRepository<UserEntity, UUID> {
	fun findByEmail(email: String): UserEntity?
	fun findByPhone(phone: String): UserEntity?
	fun countByRole(role: RoleEntity): Long
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<UserEntity>
	fun findByRoleOrderByCreatedAtDesc(role: RoleEntity, pageable: Pageable): List<UserEntity>
}

interface SpringDataDriverProfileRepository : JpaRepository<DriverProfileEntity, UUID> {
	fun findByPlateNumber(plateNumber: String): DriverProfileEntity?
}

interface SpringDataRideRepository : JpaRepository<RideEntity, UUID> {
	fun countByStatus(status: RideStatusEntity): Long
	fun findAllByOrderByRequestedAtDesc(pageable: Pageable): List<RideEntity>
	fun findByStatusOrderByRequestedAtDesc(status: RideStatusEntity, pageable: Pageable): List<RideEntity>
	fun findByStatusAndScheduledAtLessThanEqual(status: RideStatusEntity, ts: Instant): List<RideEntity>
	fun findFirstByDriverIdAndStatusInOrderByRequestedAtDesc(driverId: UUID, statuses: Collection<RideStatusEntity>): RideEntity?
	fun findByPassengerIdOrderByRequestedAtDesc(passengerId: UUID, pageable: Pageable): List<RideEntity>
	fun findByDriverIdOrderByRequestedAtDesc(driverId: UUID, pageable: Pageable): List<RideEntity>
	fun countByDriverIdAndStatus(driverId: UUID, status: RideStatusEntity): Long

	@Query(
		value = """
			select date_trunc('day', completed_at) as day, coalesce(sum(fare), 0) as total, count(*) as cnt
			from rides
			where driver_id = :driverId and status = 'COMPLETED' and completed_at between :from and :to
			group by date_trunc('day', completed_at)
			order by day
		""",
		nativeQuery = true,
	)
	fun earningsByDay(
		@Param("driverId") driverId: UUID,
		@Param("from") from: Instant,
		@Param("to") to: Instant,
	): List<DailyEarningsRow>

	@Query(
		value = """
			select date_trunc('day', completed_at) as day, coalesce(sum(fare), 0) as total, count(*) as cnt
			from rides
			where status = 'COMPLETED' and completed_at between :from and :to
			group by date_trunc('day', completed_at)
			order by day
		""",
		nativeQuery = true,
	)
	fun platformRevenueByDay(@Param("from") from: Instant, @Param("to") to: Instant): List<DailyEarningsRow>

	@Query(
		value = """
			select tariff, coalesce(sum(fare), 0) as total, count(*) as cnt
			from rides
			where status = 'COMPLETED' and completed_at between :from and :to
			group by tariff
			order by total desc
		""",
		nativeQuery = true,
	)
	fun platformRevenueByTariff(@Param("from") from: Instant, @Param("to") to: Instant): List<TariffRevenueRow>
}

/** Native-query projection — column aliases (day/total/cnt) map to these getters. */
interface DailyEarningsRow {
	fun getDay(): java.sql.Timestamp
	fun getTotal(): Long
	fun getCnt(): Long
}

/** Native-query projection — column aliases (tariff/total/cnt) map to these getters. */
interface TariffRevenueRow {
	fun getTariff(): String
	fun getTotal(): Long
	fun getCnt(): Long
}

interface SpringDataRideOfferRepository : JpaRepository<RideOfferEntity, UUID> {
	fun findByRideId(rideId: UUID): List<RideOfferEntity>

	fun findByStatusAndExpiresAtBefore(status: RideOfferStatusEntity, expiresAt: Instant): List<RideOfferEntity>

	fun findByRideIdAndStatus(rideId: UUID, status: RideOfferStatusEntity): List<RideOfferEntity>
}

interface SpringDataRideRatingRepository : JpaRepository<RideRatingEntity, UUID> {
	fun existsByRideIdAndRaterId(rideId: UUID, raterId: UUID): Boolean

	@Query("select avg(r.stars) from RideRatingEntity r where r.rateeId = :rateeId")
	fun averageStarsForRatee(@Param("rateeId") rateeId: UUID): Double?
}

interface SpringDataRideMessageRepository : JpaRepository<RideMessageEntity, UUID> {
	fun findByRideIdOrderByCreatedAtAsc(rideId: UUID): List<RideMessageEntity>
}

interface SpringDataSosIncidentRepository : JpaRepository<SosIncidentEntity, UUID> {
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<SosIncidentEntity>
}

interface SpringDataPromoCodeRepository : JpaRepository<PromoCodeEntity, UUID> {
	fun findByCode(code: String): PromoCodeEntity?
}

interface SpringDataPromoRedemptionRepository : JpaRepository<PromoRedemptionEntity, UUID> {
	fun existsByPromoIdAndUserId(promoId: UUID, userId: UUID): Boolean
}

interface SpringDataPushSubscriptionRepository : JpaRepository<PushSubscriptionEntity, UUID> {
	fun findByUserId(userId: UUID): List<PushSubscriptionEntity>
	fun deleteByUserIdAndEndpoint(userId: UUID, endpoint: String)
	fun deleteByEndpoint(endpoint: String)
}

interface SpringDataOtpChallengeRepository : JpaRepository<OtpChallengeEntity, UUID> {
	fun countByPhoneAndCreatedAtAfter(phone: String, since: Instant): Long
	fun findFirstByPhoneAndConsumedAtIsNullOrderByCreatedAtDesc(phone: String): OtpChallengeEntity?
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<OtpChallengeEntity>
}

interface SpringDataAdminTwoFactorChallengeRepository : JpaRepository<AdminTwoFactorChallengeEntity, UUID> {
	fun countByUserIdAndCreatedAtAfter(userId: UUID, since: Instant): Long

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select challenge from AdminTwoFactorChallengeEntity challenge where challenge.id = :id")
	fun findLockedById(@Param("id") id: UUID): AdminTwoFactorChallengeEntity?
}

interface SpringDataPlatformSettingRepository : JpaRepository<PlatformSettingEntity, String>

interface SpringDataAuditLogRepository : JpaRepository<AuditLogEntryEntity, UUID> {
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<AuditLogEntryEntity>
}

interface SpringDataPayoutRepository : JpaRepository<PayoutEntity, UUID> {
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<PayoutEntity>
	fun findByDriverIdOrderByCreatedAtDesc(driverId: UUID, pageable: Pageable): List<PayoutEntity>
}

interface SpringDataBroadcastLogRepository : JpaRepository<BroadcastLogEntity, UUID> {
	fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<BroadcastLogEntity>
}
