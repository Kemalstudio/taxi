package com.taxiplatform.driver.domain.usecase

import com.taxiplatform.driver.domain.model.Ride
import com.taxiplatform.driver.domain.model.RideStatus
import com.taxiplatform.driver.domain.repository.RideRepository
import javax.inject.Inject

class AcceptRideUseCase @Inject constructor(
	private val rideRepository: RideRepository,
) {
	suspend operator fun invoke(rideId: String): Result<Ride> = rideRepository.accept(rideId)
}

class RejectRideUseCase @Inject constructor(
	private val rideRepository: RideRepository,
) {
	suspend operator fun invoke(rideId: String): Result<Unit> = rideRepository.reject(rideId)
}

/** Drives ACCEPTED -> DRIVER_ARRIVED -> IN_PROGRESS -> COMPLETED, one call per state. */
class AdvanceRideUseCase @Inject constructor(
	private val rideRepository: RideRepository,
) {
	suspend operator fun invoke(ride: Ride): Result<Ride> = when (ride.status) {
		RideStatus.ACCEPTED -> rideRepository.arrive(ride.id)
		RideStatus.DRIVER_ARRIVED -> rideRepository.start(ride.id)
		RideStatus.IN_PROGRESS -> rideRepository.complete(ride.id)
		else -> Result.failure(IllegalStateException("Cannot advance ride in status ${ride.status}"))
	}
}
