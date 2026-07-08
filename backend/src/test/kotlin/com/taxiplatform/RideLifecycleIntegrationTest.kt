package com.taxiplatform

import com.taxiplatform.api.dto.AuthResponse
import com.taxiplatform.api.dto.DriverLocationRequest
import com.taxiplatform.api.dto.DriverStatusRequest
import com.taxiplatform.api.dto.GeoPointRequest
import com.taxiplatform.api.dto.RegisterRequest
import com.taxiplatform.api.dto.RequestRideRequest
import com.taxiplatform.api.dto.RideResponse
import com.taxiplatform.domain.driver.DriverStatus
import com.taxiplatform.domain.user.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RideLifecycleIntegrationTest {

	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
			.withDatabaseName("taxi")
			.withUsername("taxi")
			.withPassword("taxi")

		@Container
		@JvmStatic
		val redis = GenericContainer(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379)

		@DynamicPropertySource
		@JvmStatic
		fun properties(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url", postgres::getJdbcUrl)
			registry.add("spring.datasource.username", postgres::getUsername)
			registry.add("spring.datasource.password", postgres::getPassword)
			registry.add("spring.data.redis.host") { redis.host }
			registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
		}
	}

	@LocalServerPort
	private var port: Int = 0

	@Autowired
	private lateinit var restTemplate: TestRestTemplate

	private fun url(path: String) = "http://localhost:$port$path"

	private fun authHeaders(token: String) = HttpHeaders().apply {
		setBearerAuth(token)
		set(HttpHeaders.CONTENT_TYPE, "application/json")
	}

	private fun register(role: Role, email: String): AuthResponse {
		val request = RegisterRequest(
			email = email,
			password = "SuperSecret123",
			role = role,
			fullName = "Test $role",
			phone = null,
		)
		val response = restTemplate.postForEntity(url("/auth/register"), request, AuthResponse::class.java)
		assertEquals(HttpStatus.CREATED, response.statusCode)
		return response.body!!
	}

	private fun setDriverOnlineAt(token: String, lat: Double, lng: Double) {
		restTemplate.exchange(
			url("/driver/status"),
			HttpMethod.POST,
			HttpEntity(DriverStatusRequest(DriverStatus.ONLINE), authHeaders(token)),
			Void::class.java,
		)
		restTemplate.exchange(
			url("/driver/location"),
			HttpMethod.POST,
			HttpEntity(DriverLocationRequest(lat, lng), authHeaders(token)),
			Void::class.java,
		)
	}

	@Test
	fun `full ride lifecycle - reject cascades to next driver, then accept, start and complete`() {
		val passenger = register(Role.PASSENGER, "passenger-${UUID.randomUUID()}@example.com")
		val driverA = register(Role.DRIVER, "driver-a-${UUID.randomUUID()}@example.com")
		val driverB = register(Role.DRIVER, "driver-b-${UUID.randomUUID()}@example.com")

		val pickupLat = 52.5200
		val pickupLng = 13.4050

		// Driver A is right at the pickup point (nearest), driver B is a bit further away.
		setDriverOnlineAt(driverA.token, pickupLat, pickupLng)
		setDriverOnlineAt(driverB.token, pickupLat + 0.01, pickupLng + 0.01)

		val rideRequest = RequestRideRequest(
			pickup = GeoPointRequest(pickupLat, pickupLng),
			dropoff = GeoPointRequest(pickupLat + 0.05, pickupLng + 0.05),
		)
		val requestResponse = restTemplate.exchange(
			url("/rides"),
			HttpMethod.POST,
			HttpEntity(rideRequest, authHeaders(passenger.token)),
			RideResponse::class.java,
		)
		assertEquals(HttpStatus.CREATED, requestResponse.statusCode)
		val ride = requestResponse.body!!
		assertEquals("SEARCHING", ride.status)

		// Driver A (nearest) rejects; dispatch must cascade immediately to driver B.
		val rejectResponse = restTemplate.exchange(
			url("/rides/${ride.id}/reject"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverA.token)),
			Void::class.java,
		)
		assertEquals(HttpStatus.NO_CONTENT, rejectResponse.statusCode)

		// Driver A no longer has a pending offer, so a second reject must fail.
		val secondRejectResponse = restTemplate.exchange(
			url("/rides/${ride.id}/reject"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverA.token)),
			String::class.java,
		)
		assertEquals(HttpStatus.CONFLICT, secondRejectResponse.statusCode)

		// Driver B should now hold the (cascaded) pending offer and can accept it.
		val acceptResponse = restTemplate.exchange(
			url("/rides/${ride.id}/accept"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverB.token)),
			RideResponse::class.java,
		)
		assertEquals(HttpStatus.OK, acceptResponse.statusCode)
		val acceptedRide = acceptResponse.body!!
		assertEquals("ACCEPTED", acceptedRide.status)
		assertEquals(driverB.userId, acceptedRide.driverId)

		val arriveResponse = restTemplate.exchange(
			url("/rides/${ride.id}/arrive"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverB.token)),
			RideResponse::class.java,
		)
		assertEquals("DRIVER_ARRIVED", arriveResponse.body!!.status)

		val startResponse = restTemplate.exchange(
			url("/rides/${ride.id}/start"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverB.token)),
			RideResponse::class.java,
		)
		assertEquals("IN_PROGRESS", startResponse.body!!.status)

		val completeResponse = restTemplate.exchange(
			url("/rides/${ride.id}/complete"),
			HttpMethod.POST,
			HttpEntity<Void>(authHeaders(driverB.token)),
			RideResponse::class.java,
		)
		val completedRide = completeResponse.body!!
		assertEquals("COMPLETED", completedRide.status)
		assertNotNull(completedRide.completedAt)
	}
}
