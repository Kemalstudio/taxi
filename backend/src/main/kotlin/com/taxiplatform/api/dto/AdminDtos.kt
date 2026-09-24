package com.taxiplatform.api.dto

import com.taxiplatform.application.admin.AdminRideDetail
import com.taxiplatform.domain.audit.AuditLogEntry
import com.taxiplatform.domain.broadcast.BroadcastLog
import com.taxiplatform.domain.broadcast.BroadcastSegment
import com.taxiplatform.application.admin.DriverActivityStats
import com.taxiplatform.application.admin.DriverEarningsSummary
import com.taxiplatform.application.admin.PlatformRevenue
import com.taxiplatform.application.admin.PlatformStats
import com.taxiplatform.application.admin.SystemStatus
import com.taxiplatform.application.ports.DriverEarningsPoint
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.application.ports.TariffRevenuePoint
import com.taxiplatform.domain.auth.OtpChallenge
import com.taxiplatform.domain.geo.DriverLocation
import com.taxiplatform.domain.payout.Payout
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class PlatformStatsResponse(
	val totalUsers: Long,
	val totalDrivers: Long,
	val totalPassengers: Long,
	val totalRides: Long,
	val ridesByStatus: Map<String, Long>,
	val driversOnline: Int,
	val activeRides: Long,
) {
	companion object {
		fun from(stats: PlatformStats) = PlatformStatsResponse(
			totalUsers = stats.totalUsers,
			totalDrivers = stats.totalDrivers,
			totalPassengers = stats.totalPassengers,
			totalRides = stats.totalRides,
			ridesByStatus = stats.ridesByStatus.mapKeys { it.key.name },
			driversOnline = stats.driversOnline,
			activeRides = stats.activeRides,
		)
	}
}

data class AdminDriverResponse(
	val userId: UUID,
	val fullName: String,
	val email: String,
	val phone: String?,
	val status: String,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: String,
	val verificationStatus: String,
	val rejectionReason: String?,
	val hasLicenseDoc: Boolean,
	val hasVehicleDoc: Boolean,
	val banned: Boolean,
	val bannedReason: String?,
	val createdAt: Instant,
) {
	companion object {
		fun from(driver: DriverWithUser) = AdminDriverResponse(
			userId = driver.user.id,
			fullName = driver.user.fullName,
			email = driver.user.email,
			phone = driver.user.phone,
			status = driver.profile.status.name,
			vehicleMake = driver.profile.vehicleMake,
			vehicleModel = driver.profile.vehicleModel,
			plateNumber = driver.profile.plateNumber,
			rating = driver.profile.rating.toPlainString(),
			verificationStatus = driver.profile.verificationStatus.name,
			rejectionReason = driver.profile.rejectionReason,
			hasLicenseDoc = driver.profile.licenseDocPath != null,
			hasVehicleDoc = driver.profile.vehicleDocPath != null,
			banned = driver.user.banned,
			bannedReason = driver.user.bannedReason,
			createdAt = driver.user.createdAt,
		)
	}
}

/** Never exposed on the passenger-facing ride endpoint — admin-only visibility into who booked. */
data class PassengerInfoDto(
	val userId: UUID,
	val fullName: String,
	val phone: String?,
	val email: String,
) {
	companion object {
		fun from(user: User) = PassengerInfoDto(
			userId = user.id,
			fullName = user.fullName,
			phone = user.phone,
			email = user.email,
		)
	}
}

/** One entry in the dispatch cascade for a ride — who was offered it, in what order, and how
 * they responded. Lets ops actually see why a ride ended up NO_DRIVERS_FOUND instead of guessing. */
data class RideOfferSummaryDto(
	val driverId: UUID,
	val driverName: String,
	val status: String,
	val offeredAt: Instant,
	val expiresAt: Instant,
	val respondedAt: Instant?,
)

data class AdminRideDetailResponse(
	val ride: RideResponse,
	val passenger: PassengerInfoDto?,
	val offers: List<RideOfferSummaryDto>,
) {
	companion object {
		fun from(detail: AdminRideDetail): AdminRideDetailResponse {
			val rideResponse = RideResponse.from(detail.ride).let { base ->
				if (detail.driverUser == null || detail.driverProfile == null) {
					base.copy(estimatedCancellationFee = detail.estimatedCancellationFee)
				} else {
					base.copy(
						estimatedCancellationFee = detail.estimatedCancellationFee,
						driver = DriverInfoDto(
							userId = detail.driverUser.id,
							fullName = detail.driverUser.fullName,
							phone = detail.driverUser.phone,
							vehicleMake = detail.driverProfile.vehicleMake,
							vehicleModel = detail.driverProfile.vehicleModel,
							plateNumber = detail.driverProfile.plateNumber,
							rating = detail.driverProfile.rating.toPlainString(),
						),
					)
				}
			}
			return AdminRideDetailResponse(
				ride = rideResponse,
				passenger = detail.passenger?.let(PassengerInfoDto::from),
				offers = detail.offers.map { offer ->
					RideOfferSummaryDto(
						driverId = offer.driverId,
						driverName = detail.offerDriverNames[offer.driverId] ?: "Unknown driver",
						status = offer.status.name,
						offeredAt = offer.offeredAt,
						expiresAt = offer.expiresAt,
						respondedAt = offer.respondedAt,
					)
				},
			)
		}
	}
}

data class AdminUserResponse(
	val userId: UUID,
	val email: String,
	val fullName: String,
	val phone: String?,
	val role: String,
	val createdAt: Instant,
	val banned: Boolean,
	val bannedReason: String?,
) {
	companion object {
		fun from(user: User) = AdminUserResponse(
			userId = user.id,
			email = user.email,
			fullName = user.fullName,
			phone = user.phone,
			role = user.role.name,
			createdAt = user.createdAt,
			banned = user.banned,
			bannedReason = user.bannedReason,
		)
	}
}

data class ChangeRoleRequest(
	val role: Role,
)

data class VerifyDriverRequest(
	/** true = approve, false = reject (with [reason]). */
	val approve: Boolean,
	val reason: String? = null,
)

data class BanUserRequest(
	val reason: String? = null,
)

/** Only surfaced while there's no real SMS gateway — see LoggingOtpAdapter. */
data class OtpChallengeResponse(
	val phone: String,
	val code: String,
	val expiresAt: Instant,
	val consumedAt: Instant?,
	val createdAt: Instant,
) {
	companion object {
		fun from(challenge: OtpChallenge) = OtpChallengeResponse(
			phone = challenge.phone,
			code = challenge.code,
			expiresAt = challenge.expiresAt,
			consumedAt = challenge.consumedAt,
			createdAt = challenge.createdAt,
		)
	}
}

data class DriverEarningsPointResponse(
	val date: LocalDate,
	val totalFare: Int,
	val rideCount: Int,
) {
	companion object {
		fun from(point: DriverEarningsPoint) = DriverEarningsPointResponse(
			date = point.date,
			totalFare = point.totalFare,
			rideCount = point.rideCount,
		)
	}
}

data class DriverEarningsResponse(
	val driverId: UUID,
	val totalFare: Int,
	val rideCount: Int,
	val points: List<DriverEarningsPointResponse>,
) {
	companion object {
		fun from(summary: DriverEarningsSummary) = DriverEarningsResponse(
			driverId = summary.driverId,
			totalFare = summary.totalFare,
			rideCount = summary.rideCount,
			points = summary.points.map(DriverEarningsPointResponse::from),
		)
	}
}

data class DriverActivityStatsResponse(
	val driverId: UUID,
	val completedRides: Long,
	val cancelledRides: Long,
	val averageRating: String?,
) {
	companion object {
		fun from(stats: DriverActivityStats) = DriverActivityStatsResponse(
			driverId = stats.driverId,
			completedRides = stats.completedRides,
			cancelledRides = stats.cancelledRides,
			averageRating = stats.averageRating?.toPlainString(),
		)
	}
}

data class PlatformRevenueResponse(
	val totalFare: Int,
	val rideCount: Int,
	val points: List<DriverEarningsPointResponse>,
) {
	companion object {
		fun from(revenue: PlatformRevenue) = PlatformRevenueResponse(
			totalFare = revenue.totalFare,
			rideCount = revenue.rideCount,
			points = revenue.points.map(DriverEarningsPointResponse::from),
		)
	}
}

/** Honest configuration flags, not a real uptime monitor — see GetSystemStatusUseCase. */
data class SystemStatusResponse(
	val databaseReachable: Boolean,
	val dispatchReachable: Boolean,
	val smsGatewayConfigured: Boolean,
	val pushNotificationsConfigured: Boolean,
) {
	companion object {
		fun from(status: SystemStatus) = SystemStatusResponse(
			databaseReachable = status.databaseReachable,
			dispatchReachable = status.dispatchReachable,
			smsGatewayConfigured = status.smsGatewayConfigured,
			pushNotificationsConfigured = status.pushNotificationsConfigured,
		)
	}
}

data class TariffRevenuePointResponse(
	val tariff: String,
	val totalFare: Int,
	val rideCount: Int,
) {
	companion object {
		fun from(point: TariffRevenuePoint) = TariffRevenuePointResponse(
			tariff = point.tariff,
			totalFare = point.totalFare,
			rideCount = point.rideCount,
		)
	}
}

data class GeneratePayoutRequest(
	val driverId: UUID,
	val periodFrom: Instant,
	val periodTo: Instant,
)

data class PayoutResponse(
	val id: UUID,
	val driverId: UUID,
	val periodFrom: Instant,
	val periodTo: Instant,
	val amount: Int,
	val rideCount: Int,
	val status: String,
	val createdAt: Instant,
	val paidAt: Instant?,
) {
	companion object {
		fun from(payout: Payout) = PayoutResponse(
			id = payout.id,
			driverId = payout.driverId,
			periodFrom = payout.periodFrom,
			periodTo = payout.periodTo,
			amount = payout.amount,
			rideCount = payout.rideCount,
			status = payout.status.name,
			createdAt = payout.createdAt,
			paidAt = payout.paidAt,
		)
	}
}

data class SendBroadcastRequest(
	val segment: BroadcastSegment,
	@field:jakarta.validation.constraints.NotBlank
	val title: String,
	@field:jakarta.validation.constraints.NotBlank
	val body: String,
)

data class BroadcastLogResponse(
	val id: UUID,
	val actorName: String,
	val segment: String,
	val title: String,
	val body: String,
	val recipientCount: Int,
	val createdAt: Instant,
) {
	companion object {
		fun from(log: BroadcastLog) = BroadcastLogResponse(
			id = log.id,
			actorName = log.actorName,
			segment = log.segment.name,
			title = log.title,
			body = log.body,
			recipientCount = log.recipientCount,
			createdAt = log.createdAt,
		)
	}
}

data class AuditLogEntryResponse(
	val id: UUID,
	val actorId: UUID,
	val actorName: String,
	val action: String,
	val targetType: String,
	val targetId: String?,
	val details: String?,
	val createdAt: Instant,
) {
	companion object {
		fun from(entry: AuditLogEntry) = AuditLogEntryResponse(
			id = entry.id,
			actorId = entry.actorId,
			actorName = entry.actorName,
			action = entry.action,
			targetType = entry.targetType,
			targetId = entry.targetId,
			details = entry.details,
			createdAt = entry.createdAt,
		)
	}
}

data class OnlineDriverResponse(
	val driverId: UUID,
	val lat: Double,
	val lng: Double,
) {
	companion object {
		fun from(location: DriverLocation) = OnlineDriverResponse(
			driverId = location.driverId,
			lat = location.point.lat,
			lng = location.point.lng,
		)
	}
}
