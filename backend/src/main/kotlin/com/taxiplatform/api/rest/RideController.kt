package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.CancelRideRequest
import com.taxiplatform.api.dto.MessageResponse
import com.taxiplatform.api.dto.RateRideRequest
import com.taxiplatform.api.dto.RatingResponse
import com.taxiplatform.api.dto.RequestRideRequest
import com.taxiplatform.api.dto.RideResponse
import com.taxiplatform.api.dto.SendMessageRequest
import com.taxiplatform.api.dto.SosIncidentResponse
import com.taxiplatform.api.dto.TriggerSosRequest
import com.taxiplatform.application.chat.ListRideMessagesUseCase
import com.taxiplatform.application.chat.SendRideMessageCommand
import com.taxiplatform.application.chat.SendRideMessageUseCase
import com.taxiplatform.application.dispatch.DispatchService
import com.taxiplatform.application.rating.RateRideCommand
import com.taxiplatform.application.rating.RateRideUseCase
import com.taxiplatform.application.ride.CancelRideUseCase
import com.taxiplatform.application.ride.DriverRideLifecycleUseCase
import com.taxiplatform.application.ride.GetRideUseCase
import com.taxiplatform.application.ride.RequestRideCommand
import com.taxiplatform.application.ride.RequestRideUseCase
import com.taxiplatform.application.safety.TriggerSosCommand
import com.taxiplatform.application.safety.TriggerSosUseCase
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
	private val rateRideUseCase: RateRideUseCase,
	private val sendRideMessageUseCase: SendRideMessageUseCase,
	private val listRideMessagesUseCase: ListRideMessagesUseCase,
	private val triggerSosUseCase: TriggerSosUseCase,
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
				tariff = request.tariff,
				fare = request.fare,
				promoCode = request.promoCode,
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

	@PostMapping("/{rideId}/rating")
	@PreAuthorize("hasRole('PASSENGER')")
	fun rateRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@Valid @RequestBody request: RateRideRequest,
	): ResponseEntity<RatingResponse> {
		val rating = rateRideUseCase.execute(
			RateRideCommand(rideId = rideId, raterId = principal.userId, stars = request.stars, comment = request.comment),
		)
		return ResponseEntity.status(HttpStatus.CREATED).body(RatingResponse.from(rating))
	}

	@GetMapping("/{rideId}/messages")
	fun getMessages(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
	): ResponseEntity<List<MessageResponse>> =
		ResponseEntity.ok(listRideMessagesUseCase.execute(rideId, principal.userId).map(MessageResponse::from))

	@PostMapping("/{rideId}/messages")
	fun sendMessage(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@Valid @RequestBody request: SendMessageRequest,
	): ResponseEntity<MessageResponse> {
		val message = sendRideMessageUseCase.execute(
			SendRideMessageCommand(rideId = rideId, senderId = principal.userId, body = request.body),
		)
		return ResponseEntity.status(HttpStatus.CREATED).body(MessageResponse.from(message))
	}

	@PostMapping("/{rideId}/sos")
	fun triggerSos(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@Valid @RequestBody request: TriggerSosRequest,
	): ResponseEntity<SosIncidentResponse> {
		val incident = triggerSosUseCase.execute(
			TriggerSosCommand(
				rideId = rideId,
				userId = principal.userId,
				point = GeoPoint(request.point.lat, request.point.lng),
				note = request.note,
			),
		)
		return ResponseEntity.status(HttpStatus.CREATED).body(SosIncidentResponse.from(incident))
	}
}
