package com.taxiplatform.application.ride

import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class RequestRideCommand(
	val passengerId: UUID,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
)

@Service
class RequestRideUseCase(
	private val rideRepository: RideRepository,
	private val dispatchService: DispatchService,
) {
	@Transactional
	fun execute(command: RequestRideCommand): Ride {
		val ride = rideRepository.save(
			Ride(
				id = UUID.randomUUID(),
				passengerId = command.passengerId,
				driverId = null,
				pickup = command.pickup,
				dropoff = command.dropoff,
				status = RideStatus.REQUESTED,
				requestedAt = Instant.now(),
				acceptedAt = null,
				arrivedAt = null,
				startedAt = null,
				completedAt = null,
				cancelledAt = null,
				cancelledReason = null,
			),
		)
		return dispatchService.startDispatch(ride)
	}
}

@Service
class GetRideUseCase(
	private val rideRepository: RideRepository,
) {
	fun execute(rideId: UUID): Ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
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
	fun completeRide(rideId: UUID, driverId: UUID): Ride =
		transitionOrThrow(rideId, driverId, from = RideStatus.IN_PROGRESS, to = RideStatus.COMPLETED) {
			it.copy(status = RideStatus.COMPLETED, completedAt = Instant.now())
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
