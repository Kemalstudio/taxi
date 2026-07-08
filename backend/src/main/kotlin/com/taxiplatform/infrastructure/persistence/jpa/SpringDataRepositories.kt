package com.taxiplatform.infrastructure.persistence.jpa

import com.taxiplatform.infrastructure.persistence.entity.DriverProfileEntity
import com.taxiplatform.infrastructure.persistence.entity.RideEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferEntity
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface SpringDataUserRepository : JpaRepository<UserEntity, UUID> {
	fun findByEmail(email: String): UserEntity?
}

interface SpringDataDriverProfileRepository : JpaRepository<DriverProfileEntity, UUID>

interface SpringDataRideRepository : JpaRepository<RideEntity, UUID>

interface SpringDataRideOfferRepository : JpaRepository<RideOfferEntity, UUID> {
	fun findByRideId(rideId: UUID): List<RideOfferEntity>

	fun findByStatusAndExpiresAtBefore(status: RideOfferStatusEntity, expiresAt: Instant): List<RideOfferEntity>

	fun findByRideIdAndStatus(rideId: UUID, status: RideOfferStatusEntity): List<RideOfferEntity>
}
