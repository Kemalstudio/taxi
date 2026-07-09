package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.CancelRideRequest
import com.taxiplatform.api.dto.RequestRideRequest
import com.taxiplatform.api.dto.RideResponse
import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.ride.CancelRideUseCase
import com.taxiplatform.application.ride.DriverRideLifecycleUseCase
import com.taxiplatform.application.ride.GetRideUseCase
import com.taxiplatform.application.ride.RequestRideCommand
import com.taxiplatform.application.ride.RequestRideUseCase
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/rides")
class RideController(
	private val requestRideUseCase: RequestRideUseCase,
	private val getRideUseCase: GetRideUseCase,
	private val cancelRideUseCase: CancelRideUseCase,
	private val dispatchService: DispatchService,
	private val driverRideLifecycleUseCase: DriverRideLifecycleUseCase,
) {

	@PostMapping
	@PreAuthorize("hasRole('PASSENGER')")
	fun requestRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: RequestRideRequest,
	): ResponseEntity<RideResponse> {
		val ride = requestRideUseCase.execute(
			RequestRideCommand(
				passengerId = principal.userId,
				pickup = GeoPoint(request.pickup.lat, request.pickup.lng),
				dropoff = GeoPoint(request.dropoff.lat, request.dropoff.lng),
				scheduledAt = request.scheduledAt,
			),
		)
		return ResponseEntity.status(HttpStatus.CREATED).body(RideResponse.from(ride))
	}

	@GetMapping("/{rideId}")
	fun getRide(@PathVariable rideId: UUID): ResponseEntity<RideResponse> =
		ResponseEntity.ok(RideResponse.from(getRideUseCase.execute(rideId)))

	@PostMapping("/{rideId}/cancel")
	fun cancelRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@RequestBody(required = false) request: CancelRideRequest?,
	): ResponseEntity<RideResponse> {
		val ride = cancelRideUseCase.execute(rideId, principal.userId, request?.reason)
		return ResponseEntity.ok(RideResponse.from(ride))
	}

	@PostMapping("/{rideId}/accept")
	@PreAuthorize("hasRole('DRIVER')")
	fun acceptRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<RideResponse> {
		val ride = dispatchService.handleAccept(rideId, principal.userId)
		return ResponseEntity.ok(RideResponse.from(ride))
	}

	@PostMapping("/{rideId}/reject")
	@PreAuthorize("hasRole('DRIVER')")
	fun rejectRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<Void> {
		dispatchService.handleReject(rideId, principal.userId)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/{rideId}/arrive")
	@PreAuthorize("hasRole('DRIVER')")
	fun markArrived(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<RideResponse> =
		ResponseEntity.ok(RideResponse.from(driverRideLifecycleUseCase.markArrived(rideId, principal.userId)))

	@PostMapping("/{rideId}/start")
	@PreAuthorize("hasRole('DRIVER')")
	fun startRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<RideResponse> =
		ResponseEntity.ok(RideResponse.from(driverRideLifecycleUseCase.startRide(rideId, principal.userId)))

	@PostMapping("/{rideId}/complete")
	@PreAuthorize("hasRole('DRIVER')")
	fun completeRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<RideResponse> =
		ResponseEntity.ok(RideResponse.from(driverRideLifecycleUseCase.completeRide(rideId, principal.userId)))
}
