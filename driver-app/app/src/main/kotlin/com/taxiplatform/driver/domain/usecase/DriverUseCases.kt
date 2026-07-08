package com.taxiplatform.driver.domain.usecase

import com.taxiplatform.driver.domain.model.AuthSession
import com.taxiplatform.driver.domain.model.DriverStatus
import com.taxiplatform.driver.domain.model.GeoPoint
import com.taxiplatform.driver.domain.repository.DriverRepository
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import javax.inject.Inject

/**
 * Flips online/offline and, in the same step, opens/closes the WebSocket
 * connection that offers arrive on — a driver that's OFFLINE has no reason
 * to hold a live socket open.
 */
class ToggleOnlineUseCase @Inject constructor(
	private val driverRepository: DriverRepository,
	private val rideEventsRepository: RideEventsRepository,
) {
	suspend operator fun invoke(session: AuthSession, goOnline: Boolean): Result<Unit> {
		val status = if (goOnline) DriverStatus.ONLINE else DriverStatus.OFFLINE
		return driverRepository.setStatus(status).onSuccess {
			if (goOnline) {
				rideEventsRepository.connect(session.userId)
			} else {
				rideEventsRepository.disconnect()
			}
		}
	}
}

class UpdateLocationUseCase @Inject constructor(
	private val driverRepository: DriverRepository,
) {
	suspend operator fun invoke(point: GeoPoint): Result<Unit> = driverRepository.updateLocation(point)
}
