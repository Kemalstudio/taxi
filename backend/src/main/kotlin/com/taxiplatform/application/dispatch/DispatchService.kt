package com.taxiplatform.application.dispatch

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideOfferRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ride.NoPendingOfferException
import com.taxiplatform.application.ride.RideNotFoundException
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideOfferStatus
import com.taxiplatform.domain.ride.RideStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Owns the ride-offer cascade: nearest-driver-first, auto-advancing to the next
 * candidate whenever a driver rejects or fails to respond within the offer window.
 */
@Service
class DispatchService(
	private val rideRepository: RideRepository,
	private val rideOfferRepository: RideOfferRepository,
	private val driverProfileRepository: DriverProfileRepository,
	private val driverGeoIndex: DriverGeoIndex,
	private val rideEventsPublisher: RideEventsPublisher,
	@Value("\${taxi.dispatch.search-radius-km}") private val searchRadiusKm: Double,
	@Value("\${taxi.dispatch.offer-timeout-seconds}") private val offerTimeoutSeconds: Long,
) {
	private val log = LoggerFactory.getLogger(DispatchService::class.java)

	@Transactional
	fun startDispatch(ride: Ride): Ride = offerNextCandidate(ride)

	@Transactional
	fun handleReject(rideId: UUID, driverId: UUID) {
		val offer = requirePendingOffer(rideId, driverId)
		rideOfferRepository.save(offer.copy(status = RideOfferStatus.REJECTED, respondedAt = Instant.now()))

		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		offerNextCandidate(ride)
	}

	@Transactional
	fun handleAccept(rideId: UUID, driverId: UUID): Ride {
		val offer = requirePendingOffer(rideId, driverId)
		rideOfferRepository.save(offer.copy(status = RideOfferStatus.ACCEPTED, respondedAt = Instant.now()))

		rideOfferRepository.findByRideIdAndStatus(rideId, RideOfferStatus.PENDING)
			.filter { it.id != offer.id }
			.forEach { rideOfferRepository.save(it.copy(status = RideOfferStatus.CANCELLED, respondedAt = Instant.now())) }

		var ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		ride = ride.copy(driverId = driverId, status = RideStatus.ACCEPTED, acceptedAt = Instant.now())
		ride = rideRepository.save(ride)

		val driverProfile = driverProfileRepository.findByUserId(driverId)
		if (driverProfile != null) {
			driverProfileRepository.save(driverProfile.copy(status = DriverStatus.BUSY, updatedAt = Instant.now()))
		}
		driverGeoIndex.removeDriver(driverId)

		rideEventsPublisher.rideStatusChanged(ride)
		return ride
	}

	/** Called periodically by [com.taxiplatform.application.dispatch.OfferTimeoutScheduler]. */
	@Transactional
	fun expireOffer(offer: RideOffer) {
		rideOfferRepository.save(offer.copy(status = RideOfferStatus.EXPIRED, respondedAt = Instant.now()))
		val ride = rideRepository.findById(offer.rideId) ?: return
		if (ride.status == RideStatus.SEARCHING) {
			offerNextCandidate(ride)
		}
	}

	private fun offerNextCandidate(ride: Ride): Ride {
		val alreadyTriedDriverIds = rideOfferRepository.findByRideId(ride.id).map { it.driverId }.toSet()

		val candidate = driverGeoIndex.findNearby(ride.pickup, searchRadiusKm)
			.firstOrNull { it.driverId !in alreadyTriedDriverIds }

		if (candidate == null) {
			val updated = rideRepository.save(ride.copy(status = RideStatus.NO_DRIVERS_FOUND))
			rideEventsPublisher.rideStatusChanged(updated)
			log.info("No available drivers for ride {}", ride.id)
			return updated
		}

		val now = Instant.now()
		val offer = rideOfferRepository.save(
			RideOffer(
				id = UUID.randomUUID(),
				rideId = ride.id,
				driverId = candidate.driverId,
				status = RideOfferStatus.PENDING,
				offeredAt = now,
				expiresAt = now.plus(Duration.ofSeconds(offerTimeoutSeconds)),
				respondedAt = null,
			),
		)

		val updatedRide = if (ride.status != RideStatus.SEARCHING) {
			rideRepository.save(ride.copy(status = RideStatus.SEARCHING))
		} else {
			ride
		}

		rideEventsPublisher.rideOffered(offer.driverId, updatedRide)
		log.info("Offered ride {} to driver {} (distance {} km)", ride.id, candidate.driverId, candidate.distanceKm)
		return updatedRide
	}

	private fun requirePendingOffer(rideId: UUID, driverId: UUID): RideOffer {
		return rideOfferRepository.findByRideIdAndStatus(rideId, RideOfferStatus.PENDING)
			.firstOrNull { it.driverId == driverId }
			?: throw NoPendingOfferException(rideId, driverId)
	}
}
