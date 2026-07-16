package com.taxiplatform.infrastructure.persistence.jpa

import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoCodeEntity
import com.taxiplatform.infrastructure.persistence.entity.PromoRedemptionEntity
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
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SpringDataUserRepository : JpaRepository<UserEntity, UUID> {
	fun findByEmail(email: String): UserEntity?
	fun countByRole(role: RoleEntity): Long
}

interface SpringDataDriverProfileRepository : JpaRepository<DriverProfileEntity, UUID>

interface SpringDataRideRepository : JpaRepository<RideEntity, UUID> {
	fun countByStatus(status: RideStatusEntity): Long
	fun findAllByOrderByRequestedAtDesc(pageable: Pageable): List<RideEntity>
	fun findByStatusOrderByRequestedAtDesc(status: RideStatusEntity, pageable: Pageable): List<RideEntity>
	fun findByStatusAndScheduledAtLessThanEqual(status: RideStatusEntity, ts: Instant): List<RideEntity>
	fun findFirstByDriverIdAndStatusInOrderByRequestedAtDesc(driverId: UUID, statuses: Collection<RideStatusEntity>): RideEntity?
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
