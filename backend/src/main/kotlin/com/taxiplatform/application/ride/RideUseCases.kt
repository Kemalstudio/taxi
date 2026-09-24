package com.taxiplatform.application.ride

import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.payment.PaymentPort
import com.taxiplatform.application.pricing.CancellationFeePolicy
import com.taxiplatform.application.pricing.PricingService
import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.application.ports.PromoRedemptionRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.promo.PromoRedemption
import com.taxiplatform.domain.ride.PaymentMethod
import com.taxiplatform.domain.ride.PaymentStatus
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
	val pickupLabel: String? = null,
	val dropoffLabel: String? = null,
	/** When set (and in the future), the ride is booked for later instead of dispatched now. */
	val scheduledAt: Instant? = null,
	val tariff: RideTariff = RideTariff.ECONOMY,
	/** Route distance from the client's own routing (OSRM) — the backend has no routing engine. */
	val km: Double? = null,
	/** Fallback fare when [km] isn't supplied; ignored otherwise — the backend prices from [km] when it can. */
	val fare: Int? = null,
	val promoCode: String? = null,
	val paymentMethod: PaymentMethod = PaymentMethod.CASH,
)

@Service
class RequestRideUseCase(
	private val rideRepository: RideRepository,
	private val dispatchService: DispatchService,
	private val promoCodeRepository: PromoCodeRepository,
	private val promoRedemptionRepository: PromoRedemptionRepository,
	private val pricingService: PricingService,
	private val paymentPort: PaymentPort,
) {
	@Transactional
	fun execute(command: RequestRideCommand): Ride {
		val now = Instant.now()
		val scheduleForLater = command.scheduledAt != null && command.scheduledAt.isAfter(now)

		// The backend owns the fare *formula* (tariff + surge); it just can't independently
		// verify `km` itself since it has no routing engine of its own.
		val quote = command.km?.let { pricingService.quote(it, command.tariff, now) }
		val surgeMultiplier = quote?.surgeMultiplier ?: 1.0
		var finalFare = quote?.finalFare ?: command.fare
		var discountApplied: Int? = null
		var redeemedPromoId: UUID? = null
		var redeemedPromoCode: String? = null

		if (command.promoCode != null && finalFare != null) {
			val promo = promoCodeRepository.findByCode(command.promoCode.trim().uppercase())
				?: throw IllegalArgumentException("Promo code is invalid or expired")
			if (!promo.isUsable(now)) throw IllegalArgumentException("Promo code is invalid or expired")
			if (promoRedemptionRepository.existsByPromoIdAndUserId(promo.id, command.passengerId)) {
				throw IllegalArgumentException("Promo code was already used")
			}
			val discount = promo.discountFor(finalFare)
			finalFare -= discount
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
				pickupLabel = command.pickupLabel,
				dropoffLabel = command.dropoffLabel,
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
				surgeMultiplier = surgeMultiplier,
				paymentMethod = command.paymentMethod,
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

		// No gateway to call yet — a CARD order is simply marked PENDING for manual admin reconciliation.
		val paidRide = if (command.paymentMethod == PaymentMethod.CARD) {
			rideRepository.save(ride.copy(paymentStatus = paymentPort.initiate(ride)))
		} else {
			ride
		}

		// A scheduled ride waits for OfferTimeoutScheduler's sibling sweeper; dispatch only immediate ones.
		return if (scheduleForLater) paidRide else dispatchService.startDispatch(paidRide)
	}
}

/** A ride plus its assigned driver's public profile, for the passenger-facing ride details view. */
data class RideDetails(
	val ride: Ride,
	val driverUser: User?,
	val driverProfile: DriverProfile?,
	/** What cancelling *right now* would cost — computed on read, not persisted. Null once the ride is terminal. */
	val estimatedCancellationFee: Int? = null,
)

@Service
class GetRideUseCase(
	private val rideRepository: RideRepository,
	private val userRepository: UserRepository,
	private val driverProfileRepository: DriverProfileRepository,
	private val cancellationFeePolicy: CancellationFeePolicy,
) {
	fun execute(rideId: UUID): RideDetails {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		val driverId = ride.driverId
		return RideDetails(
			ride = ride,
			driverUser = driverId?.let { userRepository.findById(it) },
			driverProfile = driverId?.let { driverProfileRepository.findByUserId(it) },
			estimatedCancellationFee = cancellationFeePolicy.feeFor(ride),
		)
	}
}

/** The authenticated passenger's own ride history, newest first. */
@Service
class ListMyRidesUseCase(
	private val rideRepository: RideRepository,
) {
	fun execute(passengerId: UUID, limit: Int): List<Ride> =
		rideRepository.findByPassengerId(passengerId, limit.coerceIn(1, 100))
}

@Service
class CancelRideUseCase(
	private val rideRepository: RideRepository,
	private val rideEventsPublisher: RideEventsPublisher,
	private val cancellationFeePolicy: CancellationFeePolicy,
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
		// Only the passenger backing out late is charged — a driver-initiated cancellation isn't the rider's fault.
		val fee = if (requestedBy == ride.passengerId) cancellationFeePolicy.feeFor(ride) else null
		val updated = rideRepository.save(
			ride.copy(
				status = RideStatus.CANCELLED,
				cancelledAt = Instant.now(),
				cancelledReason = reason,
				cancellationFee = fee,
			),
		)
		rideEventsPublisher.rideStatusChanged(updated)
		return updated
	}

	companion object {
		private val TERMINAL_STATUSES = setOf(RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVERS_FOUND)
	}
}

/** ADMIN/OPERATOR-only — cancels any active ride regardless of who requested it, with no fee
 * (unlike [CancelRideUseCase], which only lets the passenger or assigned driver cancel). */
@Service
class AdminForceCancelRideUseCase(
	private val rideRepository: RideRepository,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(rideId: UUID, reason: String?): Ride {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		if (ride.status in TERMINAL_STATUSES) {
			throw InvalidRideStateException("Ride $rideId is already in terminal status ${ride.status}")
		}
		val updated = rideRepository.save(
			ride.copy(status = RideStatus.CANCELLED, cancelledAt = Instant.now(), cancelledReason = reason, cancellationFee = null),
		)
		rideEventsPublisher.rideStatusChanged(updated)
		return updated
	}

	private companion object {
		val TERMINAL_STATUSES = setOf(RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVERS_FOUND)
	}
}

/** ADMIN/OPERATOR-only — manually assigns a ride to a specific online driver, or (when
 * [newDriverId] is omitted) unassigns the current driver and lets [DispatchService] redispatch
 * to the next-nearest candidate. Either way, the previously-assigned driver (if any) is freed
 * back to ONLINE so they can be offered other rides. */
@Service
class AdminReassignRideUseCase(
	private val rideRepository: RideRepository,
	private val driverProfileRepository: DriverProfileRepository,
	private val driverGeoIndex: DriverGeoIndex,
	private val dispatchService: DispatchService,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(rideId: UUID, newDriverId: UUID?): Ride {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		if (ride.status in TERMINAL_STATUSES) {
			throw InvalidRideStateException("Ride $rideId is already in terminal status ${ride.status}")
		}

		ride.driverId?.let { freeDriver(it) }

		if (newDriverId == null) {
			val cleared = rideRepository.save(ride.copy(driverId = null, status = RideStatus.SEARCHING))
			return dispatchService.startDispatch(cleared)
		}

		val targetProfile = driverProfileRepository.findByUserId(newDriverId) ?: throw DriverProfileNotFoundException(newDriverId)
		if (targetProfile.status != DriverStatus.ONLINE) {
			throw InvalidRideStateException("Driver $newDriverId is not online")
		}
		val assigned = rideRepository.save(ride.copy(driverId = newDriverId, status = RideStatus.ACCEPTED, acceptedAt = Instant.now()))
		driverProfileRepository.save(targetProfile.copy(status = DriverStatus.BUSY, updatedAt = Instant.now()))
		driverGeoIndex.removeDriver(newDriverId)
		rideEventsPublisher.rideStatusChanged(assigned)
		return assigned
	}

	private fun freeDriver(driverId: UUID) {
		val profile = driverProfileRepository.findByUserId(driverId) ?: return
		driverProfileRepository.save(profile.copy(status = DriverStatus.ONLINE, updatedAt = Instant.now()))
	}

	private companion object {
		val TERMINAL_STATUSES = setOf(RideStatus.COMPLETED, RideStatus.CANCELLED, RideStatus.NO_DRIVERS_FOUND)
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
