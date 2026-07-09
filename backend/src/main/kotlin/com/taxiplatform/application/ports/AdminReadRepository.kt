package com.taxiplatform.application.ports

import com.taxiplatform.domain.driver.DriverProfile
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User

/** A driver profile joined with its owning user, for admin listings. */
data class DriverWithUser(
	val user: User,
	val profile: DriverProfile,
)

/** Read-only aggregate queries backing the admin dashboard. */
interface AdminReadRepository {
	fun countUsers(): Long
	fun countByRole(role: Role): Long
	fun countRidesByStatus(status: RideStatus): Long
	fun totalRides(): Long
	fun listRides(status: RideStatus?, limit: Int): List<Ride>
	fun listDrivers(): List<DriverWithUser>
}
