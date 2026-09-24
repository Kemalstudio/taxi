package com.taxiplatform.application.driver

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.application.ride.DriverProfileNotFoundException
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.driver.VehiclePlate
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

class InvalidPlateNumberException(plateNumber: String) :
	RuntimeException("'$plateNumber' is not a valid plate number — expected format AB 1234 AG")

class PlateNumberTakenException(plateNumber: String) :
	RuntimeException("Plate number '$plateNumber' is already registered to another driver")

@Service
class UpdateDriverStatusUseCase(
	private val driverProfileRepository: DriverProfileRepository,
	private val driverGeoIndex: DriverGeoIndex,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(driverId: UUID, status: DriverStatus): DriverProfile {
		val profile = driverProfileRepository.findByUserId(driverId) ?: throw DriverProfileNotFoundException(driverId)
		val updated = driverProfileRepository.save(profile.copy(status = status, updatedAt = Instant.now()))
		if (status != DriverStatus.ONLINE) {
			driverGeoIndex.removeDriver(driverId)
		}
		rideEventsPublisher.adminDriverStatusChanged(driverId, status)
		return updated
	}
}

@Service
class UpdateDriverLocationUseCase(
	private val driverProfileRepository: DriverProfileRepository,
	private val driverGeoIndex: DriverGeoIndex,
	private val rideRepository: RideRepository,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	fun execute(driverId: UUID, point: GeoPoint) {
		val profile = driverProfileRepository.findByUserId(driverId) ?: throw DriverProfileNotFoundException(driverId)
		// Only ONLINE (idle) drivers belong in the dispatch geo-index.
		if (profile.status == DriverStatus.ONLINE) {
			driverGeoIndex.updateLocation(driverId, point)
		}
		// If the driver is on a ride, stream their position to the passenger's ride topic
		// for live tracking (works while BUSY, which the geo-index skips).
		rideRepository.findActiveByDriver(driverId)?.let { ride ->
			rideEventsPublisher.driverLocation(ride.id, driverId, point)
		}
		// Unconditional — the admin live map wants every driver's position, not just those on a ride.
		rideEventsPublisher.adminDriverLocation(driverId, point)
	}
}

@Service
class UpdateDriverVehicleUseCase(
	private val driverProfileRepository: DriverProfileRepository,
) {
	@Transactional
	fun execute(driverId: UUID, vehicleMake: String, vehicleModel: String, plateNumber: String): DriverProfile {
		val profile = driverProfileRepository.findByUserId(driverId) ?: throw DriverProfileNotFoundException(driverId)
		val normalizedPlate = VehiclePlate.normalize(plateNumber)
		if (!VehiclePlate.isValid(normalizedPlate)) {
			throw InvalidPlateNumberException(normalizedPlate)
		}
		driverProfileRepository.findByPlateNumber(normalizedPlate)
			?.takeIf { it.userId != driverId }
			?.let { throw PlateNumberTakenException(normalizedPlate) }
		return driverProfileRepository.save(
			profile.copy(
				vehicleMake = vehicleMake.trim(),
				vehicleModel = vehicleModel.trim(),
				plateNumber = normalizedPlate,
				updatedAt = Instant.now(),
			),
		)
	}
}

/** A driver's own account + vehicle profile — backs the driver web cabinet's profile card. */
data class DriverMe(
	val user: User,
	val profile: DriverProfile,
)

@Service
class GetDriverMeUseCase(
	private val userRepository: UserRepository,
	private val driverProfileRepository: DriverProfileRepository,
) {
	fun execute(driverId: UUID): DriverMe {
		val user = userRepository.findById(driverId) ?: throw DriverProfileNotFoundException(driverId)
		val profile = driverProfileRepository.findByUserId(driverId) ?: throw DriverProfileNotFoundException(driverId)
		return DriverMe(user = user, profile = profile)
	}
}

/** The signed-in driver's own ride history, newest first — mirrors ListMyRidesUseCase for passengers. */
@Service
class ListMyDriverRidesUseCase(
	private val rideRepository: RideRepository,
) {
	fun execute(driverId: UUID, limit: Int): List<Ride> = rideRepository.findByDriverId(driverId, limit.coerceIn(1, 100))
}
