package com.taxiplatform.application.dispatch

import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ScheduledRideSweeperTest {

	private val rideRepository = mockk<RideRepository>()
	private val dispatchService = mockk<DispatchService>(relaxed = true)
	private val sweeper = ScheduledRideSweeper(rideRepository, dispatchService)

	private fun scheduledRide() = Ride(
		id = UUID.randomUUID(),
		passengerId = UUID.randomUUID(),
		driverId = null,
		pickup = GeoPoint(52.52, 13.40),
		dropoff = GeoPoint(52.53, 13.41),
		status = RideStatus.SCHEDULED,
		requestedAt = Instant.now().minusSeconds(3600),
		scheduledAt = Instant.now().minusSeconds(5),
		acceptedAt = null,
		arrivedAt = null,
		startedAt = null,
		completedAt = null,
		cancelledAt = null,
		cancelledReason = null,
	)

	@Test
	fun `dispatches every scheduled ride that is now due`() {
		val a = scheduledRide()
		val b = scheduledRide()
		every { rideRepository.findDueScheduled(any()) } returns listOf(a, b)

		sweeper.dispatchDueScheduledRides()

		verify(exactly = 1) { dispatchService.startDispatch(a) }
		verify(exactly = 1) { dispatchService.startDispatch(b) }
	}

	@Test
	fun `does nothing when no scheduled ride is due`() {
		every { rideRepository.findDueScheduled(any()) } returns emptyList()

		sweeper.dispatchDueScheduledRides()

		verify(exactly = 0) { dispatchService.startDispatch(any()) }
	}
}
