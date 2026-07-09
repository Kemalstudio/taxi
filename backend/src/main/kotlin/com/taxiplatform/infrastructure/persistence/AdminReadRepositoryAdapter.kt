package com.taxiplatform.infrastructure.persistence

import com.taxiplatform.application.ports.AdminReadRepository
import com.taxiplatform.application.ports.DriverWithUser
import com.taxiplatform.domain.ride.Ride
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.infrastructure.persistence.entity.RideStatusEntity
import com.taxiplatform.infrastructure.persistence.entity.RoleEntity
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataDriverProfileRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataRideRepository
import com.taxiplatform.infrastructure.persistence.jpa.SpringDataUserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

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

	override fun listRides(status: RideStatus?, limit: Int): List<Ride> {
		val pageable = PageRequest.of(0, limit)
		val entities = if (status == null) {
			rideRepository.findAllByOrderByRequestedAtDesc(pageable)
		} else {
			rideRepository.findByStatusOrderByRequestedAtDesc(RideStatusEntity.valueOf(status.name), pageable)
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
}
