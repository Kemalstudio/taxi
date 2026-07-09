package com.taxiplatform.infrastructure.websocket

import com.taxiplatform.api.dto.GeoPointDto
import com.taxiplatform.api.dto.RideLocationMessage
import com.taxiplatform.api.dto.RideOfferMessage
import com.taxiplatform.api.dto.RideStatusMessage
import com.taxiplatform.application.ports.RideEventsPublisher
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SimpMessagingRideEventsPublisher(
	private val messagingTemplate: SimpMessagingTemplate,
) : RideEventsPublisher {

	override fun rideOffered(driverId: UUID, ride: Ride) {
		messagingTemplate.convertAndSend(
			"/topic/driver/$driverId",
			RideOfferMessage(
				rideId = ride.id,
				pickup = GeoPointDto(ride.pickup.lat, ride.pickup.lng),
				dropoff = GeoPointDto(ride.dropoff.lat, ride.dropoff.lng),
			),
		)
	}

	override fun rideStatusChanged(ride: Ride) {
		messagingTemplate.convertAndSend(
			"/topic/ride/${ride.id}",
			RideStatusMessage(
				rideId = ride.id,
				status = ride.status.name,
				driverId = ride.driverId,
			),
		)
	}

	override fun driverLocation(rideId: UUID, driverId: UUID, point: GeoPoint) {
		messagingTemplate.convertAndSend(
			"/topic/ride/$rideId",
			RideLocationMessage(rideId = rideId, driverId = driverId, lat = point.lat, lng = point.lng),
		)
	}
}
