package com.taxiplatform.domain.ride

import com.taxiplatform.domain.geo.GeoPoint
import java.time.Instant
import java.util.UUID

enum class RideStatus {
	SCHEDULED,
	REQUESTED,
	SEARCHING,
	ACCEPTED,
	DRIVER_ARRIVED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED,
	NO_DRIVERS_FOUND,
}

enum class RideTariff {
	ECONOMY,
	COMFORT,
	BUSINESS,
	ELECTRO,
}

enum class PaymentMethod {
	CASH,
	CARD,
}

enum class PaymentStatus {
	NOT_APPLICABLE,
	PENDING,
	CONFIRMED,
	FAILED,
}

data class Ride(
	val id: UUID,
	val passengerId: UUID,
	val driverId: UUID?,
	val pickup: GeoPoint,
	val dropoff: GeoPoint,
	val pickupLabel: String? = null,
	val dropoffLabel: String? = null,
	val status: RideStatus,
	val requestedAt: Instant,
	val scheduledAt: Instant?,
	val acceptedAt: Instant?,
	val arrivedAt: Instant?,
	val startedAt: Instant?,
	val completedAt: Instant?,
	val cancelledAt: Instant?,
	val cancelledReason: String?,
	val tariff: RideTariff = RideTariff.ECONOMY,
	val fare: Int? = null,
	val promoCode: String? = null,
	val discountApplied: Int? = null,
	val surgeMultiplier: Double = 1.0,
	val cancellationFee: Int? = null,
	val paymentMethod: PaymentMethod = PaymentMethod.CASH,
	val paymentStatus: PaymentStatus = PaymentStatus.NOT_APPLICABLE,
)
