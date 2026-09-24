package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.DriverActivityStatsResponse
import com.taxiplatform.api.dto.DriverEarningsResponse
import com.taxiplatform.api.dto.DriverLocationRequest
import com.taxiplatform.api.dto.DriverMeResponse
import com.taxiplatform.api.dto.DriverStatusRequest
import com.taxiplatform.api.dto.RideSummaryResponse
import com.taxiplatform.api.dto.UpdateVehicleRequest
import com.taxiplatform.application.admin.GetDriverActivityStatsUseCase
import com.taxiplatform.application.admin.GetDriverEarningsUseCase
import com.taxiplatform.application.admin.UploadDriverDocumentUseCase
import com.taxiplatform.application.driver.GetDriverMeUseCase
import com.taxiplatform.application.driver.ListMyDriverRidesUseCase
import com.taxiplatform.application.driver.UpdateDriverLocationUseCase
import com.taxiplatform.application.driver.UpdateDriverStatusUseCase
import com.taxiplatform.application.driver.UpdateDriverVehicleUseCase
import com.taxiplatform.application.ports.DocumentKind
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.Instant

/** Everything a driver needs about their OWN account — used by the driver web cabinet
 * (`/driver` on the main site) as well as the Android app's status/location pings. */
@RestController
@RequestMapping("/driver")
@PreAuthorize("hasRole('DRIVER')")
class DriverController(
	private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
	private val updateDriverLocationUseCase: UpdateDriverLocationUseCase,
	private val getDriverMeUseCase: GetDriverMeUseCase,
	private val listMyDriverRidesUseCase: ListMyDriverRidesUseCase,
	private val getDriverEarningsUseCase: GetDriverEarningsUseCase,
	private val getDriverActivityStatsUseCase: GetDriverActivityStatsUseCase,
	private val uploadDriverDocumentUseCase: UploadDriverDocumentUseCase,
	private val updateDriverVehicleUseCase: UpdateDriverVehicleUseCase,
) {

	@PostMapping("/status")
	fun updateStatus(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: DriverStatusRequest,
	): ResponseEntity<Void> {
		updateDriverStatusUseCase.execute(principal.userId, request.status)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/location")
	fun updateLocation(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: DriverLocationRequest,
	): ResponseEntity<Void> {
		updateDriverLocationUseCase.execute(principal.userId, GeoPoint(request.lat, request.lng))
		return ResponseEntity.noContent().build()
	}

	@GetMapping("/me")
	fun me(@AuthenticationPrincipal principal: AuthenticatedPrincipal): DriverMeResponse =
		DriverMeResponse.from(getDriverMeUseCase.execute(principal.userId))

	@PostMapping("/vehicle")
	fun updateVehicle(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: UpdateVehicleRequest,
	): DriverMeResponse {
		updateDriverVehicleUseCase.execute(principal.userId, request.vehicleMake, request.vehicleModel, request.plateNumber)
		return DriverMeResponse.from(getDriverMeUseCase.execute(principal.userId))
	}

	@GetMapping("/rides")
	fun myRides(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@RequestParam(defaultValue = "30") limit: Int,
	): List<RideSummaryResponse> =
		listMyDriverRidesUseCase.execute(principal.userId, limit).map(RideSummaryResponse::from)

	@GetMapping("/earnings")
	fun myEarnings(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@RequestParam(required = false) from: Instant?,
		@RequestParam(required = false) to: Instant?,
	): DriverEarningsResponse {
		val rangeTo = to ?: Instant.now()
		val rangeFrom = from ?: rangeTo.minus(Duration.ofDays(30))
		return DriverEarningsResponse.from(getDriverEarningsUseCase.execute(principal.userId, rangeFrom, rangeTo))
	}

	@GetMapping("/stats")
	fun myStats(@AuthenticationPrincipal principal: AuthenticatedPrincipal): DriverActivityStatsResponse =
		DriverActivityStatsResponse.from(getDriverActivityStatsUseCase.execute(principal.userId))

	/** Uploading a document resets verification to PENDING so ops re-reviews it. */
	@PostMapping("/documents/{kind}")
	fun uploadDocument(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable kind: DocumentKind,
		@RequestParam("file") file: MultipartFile,
	): ResponseEntity<Void> {
		uploadDriverDocumentUseCase.execute(principal.userId, kind, file.bytes, file.contentType ?: "application/octet-stream")
		return ResponseEntity.noContent().build()
	}
}
