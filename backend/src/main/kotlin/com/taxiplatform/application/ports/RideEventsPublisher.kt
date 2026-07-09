package com.taxiplatform.application.ports

import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.domain.ride.Ride
import java.util.UUID

interface RideEventsPublisher {
	/** Pushed to the offered driver's private topic so they can accept/reject in real time. */
	fun rideOffered(driverId: UUID, ride: Ride)

	/** Pushed to the ride's topic (passenger + assigned driver) whenever ride status changes. */
	fun rideStatusChanged(ride: Ride)

	/** Pushed to the ride's topic so the passenger can watch the driver move on the map. */
	fun driverLocation(rideId: UUID, driverId: UUID, point: GeoPoint)
}
