package com.taxiplatform.api.dto

import com.taxiplatform.application.ride.RideDetails
import com.taxiplatform.domain.ride.PaymentMethod
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideTariff
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.time.Instant
import java.util.UUID

data class GeoPointRequest(
	@field:DecimalMin("-90.0") @field:DecimalMax("90.0")
	val lat: Double,

	@field:DecimalMin("-180.0") @field:DecimalMax("180.0")
	val lng: Double,
)

data class RequestRideRequest(
	val pickup: GeoPointRequest,
	val dropoff: GeoPointRequest,
	/** Human-readable addresses, for display in ride history / admin — purely cosmetic. */
	val pickupLabel: String? = null,
	val dropoffLabel: String? = null,
	/** Optional ISO-8601 instant to book the ride for later. */
	val scheduledAt: Instant? = null,
	val tariff: RideTariff = RideTariff.ECONOMY,
	/** Route distance from the client's routing — the backend prices from this when present. */
	val km: Double? = null,
	/** Fallback fare, used only when [km] isn't supplied. */
	val fare: Int? = null,
	val promoCode: String? = null,
	val paymentMethod: PaymentMethod = PaymentMethod.CASH,
)

data class CancelRideRequest(
	val reason: String?,
)

/** Admin-only — omit [driverId] to unassign and let dispatch redispatch automatically. */
data class ReassignRideRequest(
	val driverId: UUID? = null,
)

data class PriceQuoteRequest(
	val km: Double,
	val tariff: RideTariff = RideTariff.ECONOMY,
)

data class PriceQuoteResponse(
	val baseFare: Int,
	val surgeMultiplier: Double,
	val finalFare: Int,
)

data class DriverInfoDto(
	val userId: UUID,
	val fullName: String,
	val phone: String?,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: String,
)

data class RideResponse(
	val id: UUID,
	val passengerId: UUID,
	val driverId: UUID?,
	val pickup: GeoPointDto,
	val dropoff: GeoPointDto,
	val pickupLabel: String?,
	val dropoffLabel: String?,
	val status: String,
	val requestedAt: Instant,
	val scheduledAt: Instant?,
	val acceptedAt: Instant?,
	val arrivedAt: Instant?,
	val startedAt: Instant?,
	val completedAt: Instant?,
	val cancelledAt: Instant?,
	val cancelledReason: String?,
	val tariff: String,
	val fare: Int?,
	val promoCode: String?,
	val discountApplied: Int?,
	val surgeMultiplier: Double,
	val cancellationFee: Int?,
	/** What cancelling *right now* would cost — only populated on `GET /rides/{id}`, null elsewhere. */
	val estimatedCancellationFee: Int? = null,
	val paymentMethod: String,
	val paymentStatus: String,
	val driver: DriverInfoDto? = null,
) {
	companion object {
		fun from(ride: Ride) = RideResponse(
			id = ride.id,
			passengerId = ride.passengerId,
			driverId = ride.driverId,
			pickup = GeoPointDto(ride.pickup.lat, ride.pickup.lng),
			dropoff = GeoPointDto(ride.dropoff.lat, ride.dropoff.lng),
			pickupLabel = ride.pickupLabel,
			dropoffLabel = ride.dropoffLabel,
			status = ride.status.name,
			requestedAt = ride.requestedAt,
			scheduledAt = ride.scheduledAt,
			acceptedAt = ride.acceptedAt,
			arrivedAt = ride.arrivedAt,
			startedAt = ride.startedAt,
			completedAt = ride.completedAt,
			cancelledAt = ride.cancelledAt,
			cancelledReason = ride.cancelledReason,
			tariff = ride.tariff.name,
			fare = ride.fare,
			promoCode = ride.promoCode,
			discountApplied = ride.discountApplied,
			surgeMultiplier = ride.surgeMultiplier,
			cancellationFee = ride.cancellationFee,
			paymentMethod = ride.paymentMethod.name,
			paymentStatus = ride.paymentStatus.name,
		)

		fun from(details: RideDetails): RideResponse {
			val base = from(details.ride).copy(estimatedCancellationFee = details.estimatedCancellationFee)
			val driverUser = details.driverUser
			val driverProfile = details.driverProfile
			if (driverUser == null || driverProfile == null) return base
			return base.copy(
				driver = DriverInfoDto(
					userId = driverUser.id,
					fullName = driverUser.fullName,
					phone = driverUser.phone,
					vehicleMake = driverProfile.vehicleMake,
					vehicleModel = driverProfile.vehicleModel,
					plateNumber = driverProfile.plateNumber,
					rating = driverProfile.rating.toPlainString(),
				),
			)
		}
	}
}

/** One row of the passenger's ride-history list — lighter than [RideResponse], no driver lookup. */
data class RideSummaryResponse(
	val id: UUID,
	val requestedAt: Instant,
	val pickupLabel: String?,
	val dropoffLabel: String?,
	val status: String,
	val tariff: String,
	val fare: Int?,
	val paymentMethod: String,
	val paymentStatus: String,
) {
	companion object {
		fun from(ride: Ride) = RideSummaryResponse(
			id = ride.id,
			requestedAt = ride.requestedAt,
			pickupLabel = ride.pickupLabel,
			dropoffLabel = ride.dropoffLabel,
			status = ride.status.name,
			tariff = ride.tariff.name,
			fare = ride.fare,
			paymentMethod = ride.paymentMethod.name,
			paymentStatus = ride.paymentStatus.name,
		)
	}
}
