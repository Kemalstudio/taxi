package com.taxiplatform.application.admin

import com.taxiplatform.application.ports.AdminReadRepository
import com.taxiplatform.application.ports.DriverEarningsPoint
import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.application.ports.DriverProfileRepository
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.application.ports.PayoutRepository
import com.taxiplatform.application.ports.RideOfferRepository
import com.taxiplatform.application.ports.RideMessageRepository
import com.taxiplatform.application.ports.RideRatingRepository
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.TariffRevenuePoint
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.application.pricing.CancellationFeePolicy
import com.taxiplatform.application.ride.RideNotFoundException
import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.geo.DriverLocation
import com.taxiplatform.domain.payout.Payout
import com.taxiplatform.domain.payout.PayoutStatus
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideOffer
import com.taxiplatform.domain.ride.RideMessage
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PlatformStats(
	val totalUsers: Long,
	val totalDrivers: Long,
	val totalPassengers: Long,
	val totalRides: Long,
	val ridesByStatus: Map<RideStatus, Long>,
	val driversOnline: Int,
	val activeRides: Long,
)

@Service
class AdminListRideMessagesUseCase(
	private val rideRepository: RideRepository,
	private val rideMessageRepository: RideMessageRepository,
) {
	fun execute(rideId: UUID): List<RideMessage> {
		rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		return rideMessageRepository.findByRideId(rideId)
	}
}

@Service
class GetPlatformStatsUseCase(
	private val adminReadRepository: AdminReadRepository,
	private val driverGeoIndex: DriverGeoIndex,
) {
	fun execute(): PlatformStats {
		val ridesByStatus = RideStatus.entries.associateWith { adminReadRepository.countRidesByStatus(it) }
		val activeRides = ACTIVE_STATUSES.sumOf { ridesByStatus[it] ?: 0L }
		return PlatformStats(
			totalUsers = adminReadRepository.countUsers(),
			totalDrivers = adminReadRepository.countByRole(Role.DRIVER),
			totalPassengers = adminReadRepository.countByRole(Role.PASSENGER),
			totalRides = adminReadRepository.totalRides(),
			ridesByStatus = ridesByStatus,
			driversOnline = driverGeoIndex.findAllOnline().size,
			activeRides = activeRides,
		)
	}

	companion object {
		private val ACTIVE_STATUSES = listOf(
			RideStatus.SEARCHING,
			RideStatus.ACCEPTED,
			RideStatus.DRIVER_ARRIVED,
			RideStatus.IN_PROGRESS,
		)
	}
}

@Service
class ListRidesUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(status: RideStatus?, driverId: UUID?, limit: Int): List<Ride> =
		adminReadRepository.listRides(status, driverId, limit.coerceIn(1, 200))
}

/** The full ops view of one ride: the passenger (never exposed on the passenger-facing
 * `GET /rides/{id}`, which has no ownership check), the assigned driver if any, and the
 * complete dispatch cascade — every driver who was offered this ride and how they responded. */
data class AdminRideDetail(
	val ride: Ride,
	val passenger: User?,
	val driverUser: User?,
	val driverProfile: DriverProfile?,
	val offers: List<RideOffer>,
	val offerDriverNames: Map<UUID, String>,
	val estimatedCancellationFee: Int?,
)

@Service
class AdminGetRideDetailUseCase(
	private val rideRepository: RideRepository,
	private val userRepository: UserRepository,
	private val driverProfileRepository: DriverProfileRepository,
	private val rideOfferRepository: RideOfferRepository,
	private val cancellationFeePolicy: CancellationFeePolicy,
) {
	fun execute(rideId: UUID): AdminRideDetail {
		val ride = rideRepository.findById(rideId) ?: throw RideNotFoundException(rideId)
		val driverId = ride.driverId
		val offers = rideOfferRepository.findByRideId(rideId).sortedBy { it.offeredAt }
		val offerDriverNames = offers.map { it.driverId }.distinct()
			.associateWith { userRepository.findById(it)?.fullName ?: "Unknown driver" }
		return AdminRideDetail(
			ride = ride,
			passenger = userRepository.findById(ride.passengerId),
			driverUser = driverId?.let { userRepository.findById(it) },
			driverProfile = driverId?.let { driverProfileRepository.findByUserId(it) },
			offers = offers,
			offerDriverNames = offerDriverNames,
			estimatedCancellationFee = cancellationFeePolicy.feeFor(ride),
		)
	}
}

@Service
class ListDriversUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(): List<DriverWithUser> = adminReadRepository.listDrivers()
}

@Service
class ListOnlineDriversUseCase(
	private val driverGeoIndex: DriverGeoIndex,
) {
	fun execute(): List<DriverLocation> = driverGeoIndex.findAllOnline()
}

data class DriverEarningsSummary(
	val driverId: UUID,
	val totalFare: Int,
	val rideCount: Int,
	val points: List<DriverEarningsPoint>,
)

@Service
class GetDriverEarningsUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(driverId: UUID, from: Instant, to: Instant): DriverEarningsSummary {
		val points = adminReadRepository.driverEarnings(driverId, from, to)
		return DriverEarningsSummary(
			driverId = driverId,
			totalFare = points.sumOf { it.totalFare },
			rideCount = points.sumOf { it.rideCount },
			points = points,
		)
	}
}

data class DriverActivityStats(
	val driverId: UUID,
	val completedRides: Long,
	val cancelledRides: Long,
	val averageRating: BigDecimal?,
)

@Service
class GetDriverActivityStatsUseCase(
	private val adminReadRepository: AdminReadRepository,
	private val rideRatingRepository: RideRatingRepository,
) {
	fun execute(driverId: UUID): DriverActivityStats = DriverActivityStats(
		driverId = driverId,
		completedRides = adminReadRepository.countRidesByDriverAndStatus(driverId, RideStatus.COMPLETED),
		cancelledRides = adminReadRepository.countRidesByDriverAndStatus(driverId, RideStatus.CANCELLED),
		averageRating = rideRatingRepository.averageForRatee(driverId),
	)
}

data class PlatformRevenue(
	val totalFare: Int,
	val rideCount: Int,
	val points: List<DriverEarningsPoint>,
)

@Service
class GetPlatformRevenueUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(from: Instant, to: Instant): PlatformRevenue {
		val points = adminReadRepository.platformRevenue(from, to)
		return PlatformRevenue(
			totalFare = points.sumOf { it.totalFare },
			rideCount = points.sumOf { it.rideCount },
			points = points,
		)
	}
}

@Service
class GetRevenueByTariffUseCase(
	private val adminReadRepository: AdminReadRepository,
) {
	fun execute(from: Instant, to: Instant): List<TariffRevenuePoint> = adminReadRepository.platformRevenueByTariff(from, to)
}

class NoEarningsInPeriodException(driverId: UUID) :
	RuntimeException("Driver $driverId has no completed rides in the requested period")

/** Generates a payout from a driver's completed-ride fares in [periodFrom, periodTo) — the
 * amount is fixed at generation time, so later corrections to historical rides won't silently
 * change an already-generated (or paid) payout. Doesn't track which specific rides were paid
 * out, so it's on the admin not to generate overlapping periods for the same driver. */
@Service
class GeneratePayoutUseCase(
	private val adminReadRepository: AdminReadRepository,
	private val payoutRepository: PayoutRepository,
) {
	fun execute(driverId: UUID, periodFrom: Instant, periodTo: Instant): Payout {
		val points = adminReadRepository.driverEarnings(driverId, periodFrom, periodTo)
		val amount = points.sumOf { it.totalFare }
		val rideCount = points.sumOf { it.rideCount }
		if (rideCount == 0) throw NoEarningsInPeriodException(driverId)
		return payoutRepository.save(
			Payout(
				id = UUID.randomUUID(),
				driverId = driverId,
				periodFrom = periodFrom,
				periodTo = periodTo,
				amount = amount,
				rideCount = rideCount,
				status = PayoutStatus.PENDING,
				createdAt = Instant.now(),
				paidAt = null,
			),
		)
	}
}

class PayoutNotFoundException(id: UUID) : RuntimeException("Payout $id not found")
class PayoutAlreadyPaidException(id: UUID) : RuntimeException("Payout $id was already marked paid")

@Service
class MarkPayoutPaidUseCase(
	private val payoutRepository: PayoutRepository,
) {
	fun execute(payoutId: UUID): Payout {
		val payout = payoutRepository.findById(payoutId) ?: throw PayoutNotFoundException(payoutId)
		if (payout.status == PayoutStatus.PAID) throw PayoutAlreadyPaidException(payoutId)
		return payoutRepository.save(payout.copy(status = PayoutStatus.PAID, paidAt = Instant.now()))
	}
}

@Service
class ListPayoutsUseCase(
	private val payoutRepository: PayoutRepository,
) {
	fun execute(driverId: UUID?, limit: Int): List<Payout> {
		val bounded = limit.coerceIn(1, 200)
		return if (driverId != null) payoutRepository.findByDriverId(driverId, bounded) else payoutRepository.findRecent(bounded)
	}
}

/** Honest configuration flags — NOT a real uptime/health-check system, just "is a real
 * provider wired in yet" for the integrations that currently fall back to a stub. */
data class SystemStatus(
	val databaseReachable: Boolean,
	val dispatchReachable: Boolean,
	val smsGatewayConfigured: Boolean,
	val pushNotificationsConfigured: Boolean,
)

@Service
class GetSystemStatusUseCase(
	private val adminReadRepository: AdminReadRepository,
	private val driverGeoIndex: DriverGeoIndex,
	@Value("\${taxi.otp.sms-gateway-configured:false}")
	private val smsGatewayConfigured: Boolean,
	@Value("\${taxi.push.vapid-public-key:}")
	private val vapidPublicKey: String,
) {
	fun execute(): SystemStatus {
		val databaseReachable = runCatching { adminReadRepository.totalRides() }.isSuccess
		val dispatchReachable = runCatching { driverGeoIndex.findAllOnline() }.isSuccess
		return SystemStatus(
			databaseReachable = databaseReachable,
			dispatchReachable = dispatchReachable,
			smsGatewayConfigured = smsGatewayConfigured,
			pushNotificationsConfigured = vapidPublicKey.isNotBlank(),
		)
	}
}
