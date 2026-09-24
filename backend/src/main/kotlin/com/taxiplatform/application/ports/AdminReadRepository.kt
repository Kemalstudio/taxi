package com.taxiplatform.application.ports

import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/** A driver profile joined with its owning user, for admin listings. */
data class DriverWithUser(
	val user: User,
	val profile: DriverProfile,
)

/** One day's worth of a driver's completed-ride earnings — a bar in the admin earnings chart. */
data class DriverEarningsPoint(
	val date: LocalDate,
	val totalFare: Int,
	val rideCount: Int,
)

/** Read-only aggregate queries backing the admin dashboard. */
interface AdminReadRepository {
	fun countUsers(): Long
	fun countByRole(role: Role): Long
	fun countRidesByStatus(status: RideStatus): Long
	fun totalRides(): Long
	/** [driverId], when given, takes precedence over [status] — used for "this driver's rides". */
	fun listRides(status: RideStatus?, driverId: UUID?, limit: Int): List<Ride>
	fun listDrivers(): List<DriverWithUser>

	/** All users (any role), newest first — backs the admin user/role management screen. */
	fun listUsers(role: Role?, limit: Int): List<User>

	/** Completed-ride fares for one driver, grouped by day, within [from, to]. */
	fun driverEarnings(driverId: UUID, from: Instant, to: Instant): List<DriverEarningsPoint>

	fun countRidesByDriverAndStatus(driverId: UUID, status: RideStatus): Long

	/** Completed-ride fares platform-wide, grouped by day, within [from, to]. */
	fun platformRevenue(from: Instant, to: Instant): List<DriverEarningsPoint>

	/** Completed-ride fares platform-wide, grouped by tariff, within [from, to]. */
	fun platformRevenueByTariff(from: Instant, to: Instant): List<TariffRevenuePoint>
}

/** One tariff's worth of completed-ride revenue in a window — a slice of the revenue-by-tariff report. */
data class TariffRevenuePoint(
	val tariff: String,
	val totalFare: Int,
	val rideCount: Int,
)
