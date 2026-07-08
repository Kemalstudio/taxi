package com.taxiplatform.infrastructure.geo

import com.taxiplatform.application.ports.DriverGeoIndex
import com.taxiplatform.domain.geo.DriverCandidate
import com.taxiplatform.domain.geo.GeoPoint
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.domain.geo.GeoReference
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RedisDriverGeoIndex(
	private val redisTemplate: StringRedisTemplate,
) : DriverGeoIndex {

	private val geoKey = "drivers:geo"

	override fun updateLocation(driverId: UUID, point: GeoPoint) {
		redisTemplate.opsForGeo().add(geoKey, Point(point.lng, point.lat), driverId.toString())
	}

	override fun removeDriver(driverId: UUID) {
		redisTemplate.opsForZSet().remove(geoKey, driverId.toString())
	}

	override fun findNearby(point: GeoPoint, radiusKm: Double): List<DriverCandidate> {
		val args = GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().sortAscending()
		val results = redisTemplate.opsForGeo().search(
			geoKey,
			GeoReference.fromCoordinate(point.lng, point.lat),
			Distance(radiusKm, Metrics.KILOMETERS),
			args,
		) ?: return emptyList()

		return results.content.mapNotNull { result ->
			val driverId = result.content.name.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: return@mapNotNull null
			DriverCandidate(driverId = driverId, distanceKm = result.distance.value)
		}
	}
}
