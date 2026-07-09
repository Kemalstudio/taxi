package com.taxiplatform.application.driver

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ride.DriverProfileNotFoundException
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.geo.GeoPoint
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UpdateDriverStatusUseCase(
	private val driverProfileRepository: DriverProfileRepository,
	private val driverGeoIndex: DriverGeoIndex,
) {
	@Transactional
	fun execute(driverId: UUID, status: DriverStatus): DriverProfile {
		val profile = driverProfileRepository.findByUserId(driverId) ?: throw DriverProfileNotFoundException(driverId)
		val updated = driverProfileRepository.save(profile.copy(status = status, updatedAt = Instant.now()))
		if (status != DriverStatus.ONLINE) {
			driverGeoIndex.removeDriver(driverId)
		}
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
	}
}
