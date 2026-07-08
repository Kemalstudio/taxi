package com.taxiplatform.application.ride

import java.util.UUID

class RideNotFoundException(rideId: UUID) : RuntimeException("Ride not found: $rideId")

class NoPendingOfferException(rideId: UUID, driverId: UUID) :
	RuntimeException("No pending offer for ride $rideId and driver $driverId")

class InvalidRideStateException(message: String) : RuntimeException(message)

class DriverProfileNotFoundException(driverId: UUID) : RuntimeException("Driver profile not found: $driverId")
