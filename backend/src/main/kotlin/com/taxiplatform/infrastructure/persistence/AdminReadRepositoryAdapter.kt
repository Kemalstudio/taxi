package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.application.ports.AdminReadRepository
import com.taxiplatform.application.ports.DriverEarningsPoint
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.application.ports.TariffRevenuePoint
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.domain.user.User
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RoleEntity
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataDriverProfileRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataUserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Repository
class AdminReadRepositoryAdapter(
	private val userRepository: SpringDataUserRepository,
	private val driverProfileRepository: SpringDataDriverProfileRepository,
	private val rideRepository: SpringDataRideRepository,
) : AdminReadRepository {

	override fun countUsers(): Long = userRepository.count()

	override fun countByRole(role: Role): Long = userRepository.countByRole(RoleEntity.valueOf(role.name))

	override fun countRidesByStatus(status: RideStatus): Long =
		rideRepository.countByStatus(RideStatusEntity.valueOf(status.name))

	override fun totalRides(): Long = rideRepository.count()

	override fun listRides(status: RideStatus?, driverId: UUID?, limit: Int): List<Ride> {
		val pageable = PageRequest.of(0, limit)
		val entities = when {
			driverId != null -> rideRepository.findByDriverIdOrderByRequestedAtDesc(driverId, pageable)
			status != null -> rideRepository.findByStatusOrderByRequestedAtDesc(RideStatusEntity.valueOf(status.name), pageable)
			else -> rideRepository.findAllByOrderByRequestedAtDesc(pageable)
		}
		return entities.map { it.toDomain() }
	}

	override fun listDrivers(): List<DriverWithUser> {
		val profiles = driverProfileRepository.findAll()
		if (profiles.isEmpty()) return emptyList()
		val usersById = userRepository.findAllById(profiles.map { it.userId }).associateBy { it.id }
		return profiles.mapNotNull { profile ->
			val user = usersById[profile.userId] ?: return@mapNotNull null
			DriverWithUser(user = user.toDomain(), profile = profile.toDomain())
		}
	}

	override fun listUsers(role: Role?, limit: Int): List<User> {
		val pageable = PageRequest.of(0, limit)
		val entities = if (role == null) {
			userRepository.findAllByOrderByCreatedAtDesc(pageable)
		} else {
			userRepository.findByRoleOrderByCreatedAtDesc(RoleEntity.valueOf(role.name), pageable)
		}
		return entities.map { it.toDomain() }
	}

	override fun driverEarnings(driverId: UUID, from: Instant, to: Instant): List<DriverEarningsPoint> =
		rideRepository.earningsByDay(driverId, from, to).map { row ->
			DriverEarningsPoint(
				date = row.getDay().toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
				totalFare = row.getTotal().toInt(),
				rideCount = row.getCnt().toInt(),
			)
		}

	override fun countRidesByDriverAndStatus(driverId: UUID, status: RideStatus): Long =
		rideRepository.countByDriverIdAndStatus(driverId, RideStatusEntity.valueOf(status.name))

	override fun platformRevenue(from: Instant, to: Instant): List<DriverEarningsPoint> =
		rideRepository.platformRevenueByDay(from, to).map { row ->
			DriverEarningsPoint(
				date = row.getDay().toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
				totalFare = row.getTotal().toInt(),
				rideCount = row.getCnt().toInt(),
			)
		}

	override fun platformRevenueByTariff(from: Instant, to: Instant): List<TariffRevenuePoint> =
		rideRepository.platformRevenueByTariff(from, to).map { row ->
			TariffRevenuePoint(tariff = row.getTariff(), totalFare = row.getTotal().toInt(), rideCount = row.getCnt().toInt())
		}
}
