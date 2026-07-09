package com.taxiplatform.application.dispatch

import com.taxiplatform.application.ports.RideRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Turns "book for later" reservations into live ride requests: when a SCHEDULED
 * ride's time has arrived, hand it to [DispatchService] so it goes through the
 * normal nearest-driver dispatch cascade.
 */
@Component
class ScheduledRideSweeper(
	private val rideRepository: RideRepository,
	private val dispatchService: DispatchService,
) {
	private val log = LoggerFactory.getLogger(ScheduledRideSweeper::class.java)

	@Scheduled(fixedDelayString = "\${taxi.dispatch.scheduled-sweep-interval-ms:15000}")
	fun dispatchDueScheduledRides() {
		val due = rideRepository.findDueScheduled(Instant.now())
		due.forEach { ride ->
			log.info("Scheduled ride {} is due (was booked for {}), dispatching now", ride.id, ride.scheduledAt)
			dispatchService.startDispatch(ride)
		}
	}
}
