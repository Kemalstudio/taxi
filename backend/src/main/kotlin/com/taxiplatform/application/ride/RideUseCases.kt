package com.taxiplatform.application.ride

import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.application.ports.PromoRedemptionRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.promo.PromoRedemption
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.ride.RideTariff
import com.taxiplatform.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class RequestRideCommand(
	val passengerId: UUID,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
	/** When set (and in the future), the ride is booked for later instead of dispatched now. */
	val scheduledAt: Instant? = null,
	val tariff: RideTariff = RideTariff.ECONOMY,
	/** Client-estimated fare (before any promo discount) — the backend doesn't compute routes/fares itself. */
	val fare: Int? = null,
	val promoCode: String? = null,
)

@Service
class RequestRideUseCase(
	private val rideRepository: RideRepository,
	private val dispatchService: DispatchService,
	private val promoCodeRepository: PromoCodeRepository,
	private val promoRedemptionRepository: PromoRedemptionRepository,
) {
	@Transactional
	fun execute(command: RequestRideCommand): Ride {
		val now = Instant.now()
		val scheduleForLater = command.scheduledAt != null && command.scheduledAt.isAfter(now)

		var finalFare = command.fare
		var discountApplied: Int? = null
		var redeemedPromoId: UUID? = null
		var redeemedPromoCode: String? = null

		if (command.promoCode != null && command.fare != null) {
			val promo = promoCodeRepository.findByCode(command.promoCode.trim().uppercase())
				?: throw IllegalArgumentException("Promo code is invalid or expired")
			if (!promo.isUsable(now)) throw IllegalArgumentException("Promo code is invalid or expired")
			if (promoRedemptionRepository.existsByPromoIdAndUserId(promo.id, command.passengerId)) {
				throw IllegalArgumentException("Promo code was already used")
			}
			val discount = promo.discountFor(command.fare)
			finalFare = command.fare - discount
			discountApplied = discount
			redeemedPromoId = promo.id
			redeemedPromoCode = promo.code
			promoCodeRepository.save(promo.copy(usedCount = promo.usedCount + 1))
		}

		val ride = rideRepository.save(
			Ride(
				id = UUID.randomUUID(),
				passengerId = command.passengerId,
				driverId = null,
				pickup = command.pickup,
				dropoff = command.dropoff,
				status = if (scheduleForLater) RideStatus.SCHEDULED else RideStatus.REQUESTED,
				requestedAt = now,
				scheduledAt = if (scheduleForLater) command.scheduledAt else null,
				acceptedAt = null,
				arrivedAt = null,
				startedAt = null,
				completedAt = null,
				cancelledAt = null,
				cancelledReason = null,
				tariff = command.tariff,
				fare = finalFare,
				promoCode = redeemedPromoCode,
				discountApplied = discountApplied,
			),
		)

		if (redeemedPromoId != null) {
			promoRedemptionRepository.save(
				PromoRedemption(
					id = UUID.randomUUID(),
					promoId = redeemedPromoId,
					userId = command.passengerId,
					rideId = ride.id,
					createdAt = now,
				),
			)
		}

		// A scheduled ride waits for OfferTimeoutScheduler's sibling sweeper; dispatch only immediate ones.
		return if (scheduleForLater) ride else dispatchService.startDispatch(ride)
	}
}

/** A ride plus its assigned driver's public profile, for the passenger-facing ride details view. */
data class RideDetails(
	val ride: Ride,
	val driverUser: User?,
	val driverProfile: DriverProfile?,
)

@Service
class GetRideUseCase(
	private val rideRepository: RideRepository,
	private val userRepository: UserRepository,
	private val driverProfileRepository: DriverProfileRepository,
) {
	fun execute(rideId: UUID): RideDetails {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		val driverId = ride.driverId
		return RideDetails(
			ride = ride,
			driverUser = driverId?.let { userRepository.findById(it) },
			driverProfile = driverId?.let { driverProfileRepository.findByUserId(it) },
		)
	}
}

@Service
class CancelRideUseCase(
	private val rideRepository: RideRepository,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(rideId: UUID, requestedBy: UUID, reason: String?): Ride {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		if (ride.passengerId != requestedBy && ride.driverId != requestedBy) {
			throw InvalidRideStateException("User $requestedBy is not part of ride $rideId")
		}
		if (ride.status in TERMINAL_STATUSES) {
			throw InvalidRideStateException("Ride $rideId is already in terminal status ${ride.status}")
		}
		val updated = rideRepository.save(
			ride.copy(status = RideStatus.CANCELLED, cancelledAt = Instant.now(), cancelledReason = reason),
		)
		rideEventsPublisher.rideStatusChanged(updated)
		return updated
	}

	companion object {
		private val TERMINAL_STATUSES = setOf(RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVERS_FOUND)
	}
}

/** Driver-side lifecycle transitions: arrived at pickup, ride started, ride completed. */
@Service
class DriverRideLifecycleUseCase(
	private val rideRepository: RideRepository,
	private val rideEventsPublisher: RideEventsPublisher,
	private val userRepository: UserRepository,
) {
	@Transactional
	fun markArrived(rideId: UUID, driverId: UUID): Ride =
		transitionOrThrow(rideId, driverId, from = RideStatus.ACCEPTED, to = RideStatus.DRIVER_ARRIVED) {
			it.copy(status = RideStatus.DRIVER_ARRIVED, arrivedAt = Instant.now())
		}

	@Transactional
	fun startRide(rideId: UUID, driverId: UUID): Ride =
		transitionOrThrow(rideId, driverId, from = RideStatus.DRIVER_ARRIVED, to = RideStatus.IN_PROGRESS) {
			it.copy(status = RideStatus.IN_PROGRESS, startedAt = Instant.now())
		}

	@Transactional
	fun completeRide(rideId: UUID, driverId: UUID): Ride {
		val ride = transitionOrThrow(rideId, driverId, from = RideStatus.IN_PROGRESS, to = RideStatus.COMPLETED) {
			it.copy(status = RideStatus.COMPLETED, completedAt = Instant.now())
		}
		awardLoyaltyPoints(ride)
		return ride
	}

	/** 1 point per 10 TMT of the final fare — a simple, non-monetary loyalty ledger. */
	private fun awardLoyaltyPoints(ride: Ride) {
		val points = (ride.fare ?: 0) / 10
		if (points <= 0) return
		val passenger = userRepository.findById(ride.passengerId) ?: return
		userRepository.save(passenger.copy(loyaltyPoints = passenger.loyaltyPoints + points))
	}

	private fun transitionOrThrow(
		rideId: UUID,
		driverId: UUID,
		from: RideStatus,
		to: RideStatus,
		mutate: (Ride) -> Ride,
	): Ride {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		if (ride.driverId != driverId) {
			throw InvalidRideStateException("Driver $driverId is not assigned to ride $rideId")
		}
		if (ride.status != from) {
			throw InvalidRideStateException("Cannot move ride $rideId from ${ride.status} to $to")
		}
		val updated = rideRepository.save(mutate(ride))
		rideEventsPublisher.rideStatusChanged(updated)
		return updated
	}
}
