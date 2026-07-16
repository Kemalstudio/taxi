package com.taxiplatform.application.ride

import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.ports.PromoCodeRepository
import com.taxiplatform.application.ports.PromoRedemptionRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.RideStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class RequestRideUseCaseTest {

	private val rideRepository = mockk<RideRepository>()
	private val dispatchService = mockk<DispatchService>(relaxed = true)
	private val promoCodeRepository = mockk<PromoCodeRepository>()
	private val promoRedemptionRepository = mockk<PromoRedemptionRepository>()
	private val useCase = RequestRideUseCase(rideRepository, dispatchService, promoCodeRepository, promoRedemptionRepository)

	private val command = RequestRideCommand(
		passengerId = UUID.randomUUID(),
		pickup = GeoPoint(37.94, 58.38),
		dropoff = GeoPoint(37.98, 58.36),
	)

	@Test
	fun `an immediate ride is REQUESTED and dispatched right away`() {
		every { rideRepository.save(any()) } answers { firstArg() }

		useCase.execute(command)

		val saved = slot<com.taxiplatform.domain.ride.Ride>()
		verify { rideRepository.save(capture(saved)) }
		assertEquals(RideStatus.REQUESTED, saved.captured.status)
		verify(exactly = 1) { dispatchService.startDispatch(any()) }
	}

	@Test
	fun `a future scheduledAt books the ride as SCHEDULED without dispatching`() {
		every { rideRepository.save(any()) } answers { firstArg() }
		val future = Instant.now().plusSeconds(3600)

		val ride = useCase.execute(command.copy(scheduledAt = future))

		assertEquals(RideStatus.SCHEDULED, ride.status)
		assertNotNull(ride.scheduledAt)
		verify(exactly = 0) { dispatchService.startDispatch(any()) }
	}

	@Test
	fun `a past scheduledAt is treated as immediate`() {
		every { rideRepository.save(any()) } answers { firstArg() }

		useCase.execute(command.copy(scheduledAt = Instant.now().minusSeconds(60)))

		val saved = slot<com.taxiplatform.domain.ride.Ride>()
		verify { rideRepository.save(capture(saved)) }
		assertEquals(RideStatus.REQUESTED, saved.captured.status)
		verify(exactly = 1) { dispatchService.startDispatch(any()) }
	}
}
