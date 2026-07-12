package com.taxiplatform.application.safety

import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.SosIncidentRepository
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.application.ride.RideNotFoundException
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.SosIncident
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class TriggerSosCommand(
	val rideId: UUID,
	val userId: UUID,
	val point: GeoPoint,
	val note: String?,
)

@Service
class TriggerSosUseCase(
	private val rideRepository: RideRepository,
	private val sosIncidentRepository: SosIncidentRepository,
	private val rideEventsPublisher: RideEventsPublisher,
) {
	@Transactional
	fun execute(command: TriggerSosCommand): SosIncident {
		val ride = rideRepository.findById(command.rideId) ?: throw RideNotFoundException(command.rideId)
		if (command.userId != ride.passengerId && command.userId != ride.driverId) {
			throw RideAccessDeniedException("User ${command.userId} is not part of ride ${ride.id}")
		}
		val incident = sosIncidentRepository.save(
			SosIncident(
				id = UUID.randomUUID(),
				rideId = ride.id,
				userId = command.userId,
				point = command.point,
				note = command.note?.trim()?.takeIf { it.isNotEmpty() },
				createdAt = Instant.now(),
			),
		)
		rideEventsPublisher.sosTriggered(incident)
		return incident
	}
}

@Service
class ListRecentSosUseCase(
	private val sosIncidentRepository: SosIncidentRepository,
) {
	fun execute(limit: Int): List<SosIncident> = sosIncidentRepository.findRecent(limit.coerceIn(1, 200))
}
