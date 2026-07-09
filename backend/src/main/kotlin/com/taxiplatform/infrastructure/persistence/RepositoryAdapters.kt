package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideOfferRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.user.User
import com.taxiplatform.infrastructure.persistence.entity.RideOfferStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataDriverProfileRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideOfferRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataUserRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class JpaUserRepositoryAdapter(
	private val delegate: SpringDataUserRepository,
) : UserRepository {
	override fun findByEmail(email: String): User? = delegate.findByEmail(email)?.toDomain()
	override fun findById(id: UUID): User? = delegate.findById(id).orElse(null)?.toDomain()
	override fun save(user: User): User = delegate.save(user.toEntity()).toDomain()
}

@Repository
class JpaDriverProfileRepositoryAdapter(
	private val delegate: SpringDataDriverProfileRepository,
) : DriverProfileRepository {
	override fun findByUserId(userId: UUID): DriverProfile? = delegate.findById(userId).orElse(null)?.toDomain()
	override fun save(profile: DriverProfile): DriverProfile = delegate.save(profile.toEntity()).toDomain()
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
