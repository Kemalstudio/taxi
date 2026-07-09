package com.taxiplatform.application.ports

import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.user.User
import java.time.Instant
import java.util.UUID

interface UserRepository {
	fun findByEmail(email: String): User?
	fun findById(id: UUID): User?
	fun save(user: User): User
}

interface DriverProfileRepository {
	fun findByUserId(userId: UUID): DriverProfile?
	fun save(profile: DriverProfile): DriverProfile
}

interface RideRepository {
	fun findById(id: UUID): Ride?
	fun save(ride: Ride): Ride

	/** Scheduled rides whose time has come (status SCHEDULED and scheduledAt <= now). */
	fun findDueScheduled(now: Instant): List<Ride>

	/** The driver's currently in-progress ride (ACCEPTED / DRIVER_ARRIVED / IN_PROGRESS), if any. */
	fun findActiveByDriver(driverId: UUID): Ride?
}

interface RideOfferRepository {
	fun findById(id: UUID): RideOffer?
	fun findByRideId(rideId: UUID): List<RideOffer>
	fun findByRideIdAndStatus(rideId: UUID, status: RideOfferStatus): List<RideOffer>
	fun findExpiredPending(now: Instant): List<RideOffer>
	fun save(offer: RideOffer): RideOffer
}
