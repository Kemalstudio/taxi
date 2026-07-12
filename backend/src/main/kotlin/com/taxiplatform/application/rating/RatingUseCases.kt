package com.taxiplatform.application.rating

import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideRatingRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ride.AlreadyRatedException
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.application.ride.RideNotFoundException
import com.taxiplatform.domain.ride.RideRating
import com.taxiplatform.domain.ride.RideStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode
import java.time.Instant
import java.util.UUID

data class RateRideCommand(
	val rideId: UUID,
	val raterId: UUID,
	val stars: Int,
	val comment: String?,
)

@Service
class RateRideUseCase(
	private val rideRepository: RideRepository,
	private val rideRatingRepository: RideRatingRepository,
	private val driverProfileRepository: DriverProfileRepository,
) {
	@Transactional
	fun execute(command: RateRideCommand): RideRating {
		require(command.stars in 1..5) { "stars must be between 1 and 5" }
		val ride = rideRepository.findById(command.rideId) ?: throw RideNotFoundException(command.rideId)
		if (ride.passengerId != command.raterId) {
			throw RideAccessDeniedException("User ${command.raterId} is not the passenger of ride ${ride.id}")
		}
		if (ride.status != RideStatus.COMPLETED) {
			throw InvalidRideStateException("Ride ${ride.id} is not completed yet")
		}
		val driverId = ride.driverId ?: throw InvalidRideStateException("Ride ${ride.id} has no driver to rate")
		if (rideRatingRepository.existsByRideIdAndRaterId(ride.id, command.raterId)) {
			throw AlreadyRatedException(ride.id)
		}

		val rating = rideRatingRepository.save(
			RideRating(
				id = UUID.randomUUID(),
				rideId = ride.id,
				raterId = command.raterId,
				rateeId = driverId,
				stars = command.stars,
				comment = command.comment?.trim()?.takeIf { it.isNotEmpty() },
				createdAt = Instant.now(),
			),
		)

		rideRatingRepository.averageForRatee(driverId)?.let { average ->
			driverProfileRepository.findByUserId(driverId)?.let { profile ->
				driverProfileRepository.save(
					profile.copy(rating = average.setScale(2, RoundingMode.HALF_UP), updatedAt = Instant.now()),
				)
			}
		}

		return rating
	}
}
